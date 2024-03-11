/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.loading.binding.spi;

import java.util.Map;
import java.util.Optional;

import org.hibernate.search.engine.environment.bean.BeanResolver;
import org.hibernate.search.mapper.pojo.loading.MassLoadingStrategy;
import org.hibernate.search.mapper.pojo.loading.SelectionLoadingStrategy;
import org.hibernate.search.mapper.pojo.loading.binding.EntityLoadingBindingContext;
import org.hibernate.search.mapper.pojo.loading.definition.spi.PojoEntityLoadingBindingContext;
import org.hibernate.search.mapper.pojo.loading.spi.PojoMassLoadingStrategy;
import org.hibernate.search.mapper.pojo.loading.spi.PojoSelectionLoadingStrategy;
import org.hibernate.search.mapper.pojo.mapping.impl.AbstractPojoTypeManager;
import org.hibernate.search.mapper.pojo.model.PojoModelElement;
import org.hibernate.search.mapper.pojo.model.impl.PojoModelValueElement;
import org.hibernate.search.mapper.pojo.model.spi.PojoBootstrapIntrospector;
import org.hibernate.search.mapper.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.util.common.impl.Contracts;

public abstract class AbstractEntityLoadingBindingContext<E, I> implements EntityLoadingBindingContext {
	private final PojoModelValueElement<E> entityType;
	private final PojoModelValueElement<?> identifierType;
	private final BeanResolver beanResolver;
	private final Map<String, ?> params;

	public AbstractEntityLoadingBindingContext(PojoBootstrapIntrospector introspector,
			PojoRawTypeModel<E> entityType, PojoRawTypeModel<I> identifierType,
			BeanResolver beanResolver, Map<String, ?> params) {
		this.entityType = new PojoModelValueElement<>( introspector, entityType );
		this.identifierType = new PojoModelValueElement<>( introspector, identifierType );
		this.beanResolver = beanResolver;
		this.params = params;
	}

	@Override
	public final PojoModelElement entityType() {
		return entityType;
	}

	@Override
	public final PojoModelElement identifierType() {
		return identifierType;
	}

	@Override
	@SuppressWarnings("unchecked") // Checked using reflection
	public final <E2> void selectionLoadingStrategy(Class<E2> expectedEntitySuperType,
			SelectionLoadingStrategy<? super E2> strategy) {
		selectionLoadingStrategy( expectedEntitySuperType, adapt( strategy ) );
		checkEntitySuperType( expectedEntitySuperType );
	}

	protected abstract <E2> PojoSelectionLoadingStrategy<E2> adapt(SelectionLoadingStrategy<? super E2> strategy);

	protected abstract <E2, I2> PojoMassLoadingStrategy<E2, I2> adapt(MassLoadingStrategy<E2, I2> strategy);

	@Override
	@SuppressWarnings("unchecked") // Checked using reflection
	public final <E2> void massLoadingStrategy(Class<E2> expectedEntitySuperType,
			MassLoadingStrategy<? super E2, ?> strategy) {
		massLoadingStrategy( expectedEntitySuperType, adapt( strategy ) );
	}

	public final <E2> void selectionLoadingStrategy(Class<E2> expectedEntitySuperType,
			PojoSelectionLoadingStrategy<? super E2> strategy){
		checkEntitySuperType( expectedEntitySuperType );

	}

	public final <E2> void massLoadingStrategy(Class<E2> expectedEntitySuperType,
			PojoMassLoadingStrategy<? super E2, ?> strategy) {
		checkEntitySuperType( expectedEntitySuperType );

	}


	private <E2> void checkEntitySuperType(Class<E2> expectedEntitySuperType) {
		if ( !expectedEntitySuperType.isAssignableFrom( typeModel.typeIdentifier().javaClass() ) ) {
			throw log.loadingConfigurationTypeMismatch( typeModel, expectedEntitySuperType );
		}
	}

	@Override
	public final BeanResolver beanResolver() {
		return beanResolver;
	}

	@Override
	public final <T> T param(String name, Class<T> paramType) {
		Contracts.assertNotNull( name, "name" );
		Contracts.assertNotNull( paramType, "paramType" );

		Object value = params.get( name );
		if ( value == null ) {
			throw log.paramNotDefined( name );
		}

		return paramType.cast( value );
	}

	@Override
	public final <T> Optional<T> paramOptional(String name, Class<T> paramType) {
		Contracts.assertNotNull( name, "name" );
		Contracts.assertNotNull( paramType, "paramType" );

		return Optional.ofNullable( params.get( name ) ).map( paramType::cast );
	}
}
