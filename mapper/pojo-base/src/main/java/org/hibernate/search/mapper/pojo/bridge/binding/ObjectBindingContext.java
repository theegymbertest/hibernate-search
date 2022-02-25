/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.bridge.binding;

import org.hibernate.search.engine.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.engine.backend.types.dsl.IndexFieldTypeFactory;
import org.hibernate.search.engine.environment.bean.BeanHolder;
import org.hibernate.search.mapper.pojo.bridge.ObjectBridge;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.ObjectBinder;
import org.hibernate.search.mapper.pojo.model.PojoModelCompositeElement;
import org.hibernate.search.mapper.pojo.model.PojoModelType;
import org.hibernate.search.mapper.pojo.model.dependency.PojoObjectIndexingDependencyConfigurationContext;
import org.hibernate.search.util.common.annotation.Incubating;

/**
 * The context provided to the {@link ObjectBinder#bind(ObjectBindingContext)} method.
 */
public interface ObjectBindingContext extends BindingContext {

	/**
	 * Sets the bridge implementing the object binding.
	 *
	 * @param expectedEntityType The type of the entity expected by the given bridge.
	 * @param bridge The bridge to use at runtime to convert between the Java object and the index object.
	 * @param <T2> The type of bridged Java objects expected by the given bridge.
	 */
	<T2> void bridge(Class<T2> expectedEntityType, ObjectBridge<T2> bridge);

	/**
	 * Sets the bridge implementing the type/index binding.
	 *
	 * @param expectedEntityType The type of the entity expected by the given bridge.
	 * @param bridgeHolder A {@link BeanHolder} containing
	 * the bridge to use at runtime to convert between the Java object and the index object.
	 * Use {@link BeanHolder#of(Object)} if you don't need any particular closing behavior,
	 * or simply call {@link #bridge(Class, ObjectBridge)} instead of this method.
	 * @param <T2> The type of bridged Java objects expected by the given bridge.
	 */
	<T2> void bridge(Class<T2> expectedEntityType, BeanHolder<? extends ObjectBridge<T2>> bridgeHolder);

	/**
	 * @return An entry point allowing to retrieve metadata about, and accessors to the bridged Java object type.
	 */
	@Incubating
	PojoModelCompositeElement bridgedElement();

	/**
	 * @return An entry point allowing to declare the parts of the Java object graph (entity graph) that this bridge will depend on.
	 */
	PojoObjectIndexingDependencyConfigurationContext dependencies();

	/**
	 * @return An entry point allowing to define a new field type.
	 */
	IndexFieldTypeFactory typeFactory();

	/**
	 * @return An entry point allowing to declare expectations and retrieve accessors to the index schema.
	 */
	IndexSchemaElement indexSchemaElement();

}
