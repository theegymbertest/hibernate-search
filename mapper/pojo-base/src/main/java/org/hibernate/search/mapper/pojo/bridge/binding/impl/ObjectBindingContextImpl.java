/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.bridge.binding.impl;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;

import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.engine.backend.types.dsl.IndexFieldTypeFactory;
import org.hibernate.search.engine.environment.bean.BeanHolder;
import org.hibernate.search.engine.environment.bean.BeanResolver;
import org.hibernate.search.engine.mapper.mapping.building.spi.IndexBindingContext;
import org.hibernate.search.mapper.pojo.bridge.ObjectBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.ObjectBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.ObjectBinder;
import org.hibernate.search.mapper.pojo.logging.impl.Log;
import org.hibernate.search.mapper.pojo.model.PojoModelCompositeElement;
import org.hibernate.search.mapper.pojo.model.dependency.PojoObjectIndexingDependencyConfigurationContext;
import org.hibernate.search.mapper.pojo.model.dependency.impl.PojoObjectIndexingDependencyConfigurationContextImpl;
import org.hibernate.search.mapper.pojo.model.impl.PojoModelObjectRootElement;
import org.hibernate.search.mapper.pojo.model.spi.PojoBootstrapIntrospector;
import org.hibernate.search.mapper.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.mapper.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.util.common.impl.AbstractCloser;
import org.hibernate.search.util.common.impl.Closer;
import org.hibernate.search.util.common.impl.SuppressingCloser;
import org.hibernate.search.util.common.logging.impl.LoggerFactory;

public class ObjectBindingContextImpl<T> extends AbstractCompositeBindingContext
		implements ObjectBindingContext {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final PojoBootstrapIntrospector introspector;
	private final PojoTypeModel<?> objectTypeModel;
	private final PojoModelObjectRootElement<T> bridgedElement;
	private final PojoObjectIndexingDependencyConfigurationContextImpl<T> dependencyContext;
	private final IndexFieldTypeFactory indexFieldTypeFactory;
	private final PojoIndexSchemaContributionListener listener;
	private final IndexSchemaElement indexSchemaElement;

	private PartialBinding<T> partialBinding;

	public ObjectBindingContextImpl(BeanResolver beanResolver,
			PojoBootstrapIntrospector introspector,
			PojoTypeModel<T> objectTypeModel,
			IndexBindingContext indexBindingContext,
			PojoModelObjectRootElement<T> bridgedElement,
			PojoObjectIndexingDependencyConfigurationContextImpl<T> dependencyContext,
			Map<String, Object> params) {
		super( beanResolver, params );
		this.introspector = introspector;
		this.objectTypeModel = objectTypeModel;
		this.bridgedElement = bridgedElement;
		this.dependencyContext = dependencyContext;
		this.indexFieldTypeFactory = indexBindingContext.createTypeFactory();
		this.listener = new PojoIndexSchemaContributionListener();
		this.indexSchemaElement = indexBindingContext.schemaElement( listener );
	}

	@Override
	public <P2> void bridge(Class<P2> expectedObjectType, ObjectBridge<P2> bridge) {
		bridge( expectedObjectType, BeanHolder.of( bridge ) );
	}

	@Override
	public <P2> void bridge(Class<P2> expectedObjectType, BeanHolder<? extends ObjectBridge<P2>> bridgeHolder) {
		checkAndBind( bridgeHolder, introspector.typeModel( expectedObjectType ) );
	}

	@Override
	public PojoModelCompositeElement bridgedElement() {
		return bridgedElement;
	}

	@Override
	public PojoObjectIndexingDependencyConfigurationContext dependencies() {
		return dependencyContext;
	}

	@Override
	public IndexFieldTypeFactory typeFactory() {
		return indexFieldTypeFactory;
	}

	@Override
	public IndexSchemaElement indexSchemaElement() {
		return indexSchemaElement;
	}

	public Optional<BoundObjectBridge<T>> applyBinder(ObjectBinder binder) {
		try {
			// This call should set the partial binding
			binder.bind( this );
			if ( partialBinding == null ) {
				throw log.missingBridgeForBinder( binder );
			}

			checkBridgeDependencies( bridgedElement, dependencyContext );

			// If all fields are filtered out, we should ignore the bridge
			if ( !listener.isAnySchemaContributed() ) {
				try ( Closer<RuntimeException> closer = new Closer<>() ) {
					partialBinding.abort( closer );
				}
				return Optional.empty();
			}

			return Optional.of( partialBinding.complete(
					bridgedElement, dependencyContext
			) );
		}
		catch (RuntimeException e) {
			if ( partialBinding != null ) {
				partialBinding.abort( new SuppressingCloser( e ) );
			}
			throw e;
		}
		finally {
			partialBinding = null;
		}
	}

	private <P2> void checkAndBind(BeanHolder<? extends ObjectBridge<P2>> bridgeHolder,
			PojoRawTypeModel<?> expectedObjectTypeModel) {
		if ( !objectTypeModel.rawType().isSubTypeOf( expectedObjectTypeModel ) ) {
			throw log.invalidInputTypeForBridge( bridgeHolder.get(), objectTypeModel, expectedObjectTypeModel );
		}

		@SuppressWarnings("unchecked") // We check that P extends P2 explicitly using reflection (see above)
		BeanHolder<? extends ObjectBridge<? super T>> castedBridgeHolder =
				(BeanHolder<? extends ObjectBridge<? super T>>) bridgeHolder;

		this.partialBinding = new PartialBinding<>( castedBridgeHolder );
	}

	private static class PartialBinding<P> {
		private final BeanHolder<? extends ObjectBridge<? super P>> bridgeHolder;

		private PartialBinding(BeanHolder<? extends ObjectBridge<? super P>> bridgeHolder) {
			this.bridgeHolder = bridgeHolder;
		}

		void abort(AbstractCloser<?, ?> closer) {
			closer.push( ObjectBridge::close, bridgeHolder, BeanHolder::get );
			closer.push( BeanHolder::close, bridgeHolder );
		}

		BoundObjectBridge<P> complete(PojoModelObjectRootElement<P> bridgedElement,
				PojoObjectIndexingDependencyConfigurationContextImpl<P> dependencyContext) {
			return new BoundObjectBridge<>(
					bridgeHolder,
					bridgedElement,
					dependencyContext
			);
		}
	}
}
