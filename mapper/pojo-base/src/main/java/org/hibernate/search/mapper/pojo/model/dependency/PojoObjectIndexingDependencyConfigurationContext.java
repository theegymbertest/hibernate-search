/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.model.dependency;

import org.hibernate.search.mapper.pojo.model.path.PojoModelPath;
import org.hibernate.search.mapper.pojo.model.path.PojoModelPathValueNode;
import org.hibernate.search.util.common.annotation.Incubating;

public interface PojoObjectIndexingDependencyConfigurationContext {

	/**
	 * Declare that the bridge will only use the bridged Java object directly,
	 * and will not access any mutable property of that bridged Java object.
	 * <p>
	 * This is unusual, and generally only possible for bridges that are applied to immutable types ({@code String}, an enum, ...)
	 * or collections of immutable types ({@code List<String>}, ...),
	 * or that do not rely on the bridged element at all (constant bridges, bridges adding the last indexing date, ...).
	 * <p>
	 * Note that calling this method prevents from declaring any other dependency,
	 * and trying to do so will trigger an exception.
	 */
	void useRootOnly();

	/**
	 * Declare that the given path is read by the bridge at index time to populate the index object.
	 *
	 * @param pathFromBridgedJavaObjectToUsedValue The path from the value of the bridged Java object
	 * to the values used by the bridge, as a String.
	 * The string is interpreted with default value extractors: see {@link PojoModelPath#parse(String)}.
	 * @return {@code this}, for method chaining.
	 * @throws org.hibernate.search.util.common.SearchException If the given path cannot be applied to the values of the bridged Java object.
	 * @see #use(PojoModelPathValueNode)
	 */
	default PojoObjectIndexingDependencyConfigurationContext use(String pathFromBridgedJavaObjectToUsedValue) {
		return use( PojoModelPath.parse( pathFromBridgedJavaObjectToUsedValue ) );
	}

	/**
	 * Declare that the given path is read by the bridge at index time to populate the index object.
	 *
	 * @param pathFromBridgedJavaObjectToUsedValue The path from the value of the bridged Java object
	 * to the values used by the bridge.
	 * @return {@code this}, for method chaining.
	 * @throws org.hibernate.search.util.common.SearchException If the given extractor path cannot be applied to the bridged Java object,
	 * or if the given path cannot be applied to the values of the bridged Java object.
	 */
	PojoObjectIndexingDependencyConfigurationContext use(PojoModelPathValueNode pathFromBridgedJavaObjectToUsedValue);

	/**
	 * Start the declaration of dependencies to properties of another entity,
	 * without specifying the path to that other entity.
	 * <p>
	 * <strong>Note:</strong> this is only useful when the path from the bridged type to that other entity
	 * cannot be easily represented, but the inverse path can.
	 * For almost all use cases, this method won't be useful and calling {@link #use(String)} will be enough.
	 *
	 * @param otherEntityType The raw type of the other entity.
	 * @param pathFromOtherEntityTypeToBridgedJavaObject The path from the other entity type to the bridged Java object,
	 * as a String. The string is interpreted with default value extractors: see {@link PojoModelPath#parse(String)}.
	 * Used when the other entity changes, to collect the Java object instances that must be reindexed.
	 * @return A context allowing to declare which properties
	 * @throws org.hibernate.search.util.common.SearchException If the bridged Java object type is not an entity type,
	 * or the given type is not an entity type,
	 * or the given path cannot be applied to the given entity type.
	 */
	@Incubating
	default PojoOtherEntityIndexingDependencyConfigurationContext fromOtherEntity(Class<?> otherEntityType,
			String pathFromOtherEntityTypeToBridgedJavaObject) {
		return fromOtherEntity( otherEntityType, PojoModelPath.parse( pathFromOtherEntityTypeToBridgedJavaObject ) );
	}

	/**
	 * Start the declaration of dependencies to properties of another entity,
	 * without specifying the path to that other entity.
	 * <p>
	 * <strong>Note:</strong> this is only useful when the path from the bridged type to that other entity
	 * cannot be easily represented, but the inverse path can.
	 * For almost all use cases, this method won't be useful and calling {@link #use(PojoModelPathValueNode)} will be enough.
	 *
	 * @param otherEntityType The raw type of the other entity.
	 * @param pathFromOtherEntityTypeToBridgedJavaObject The path from the other entity type to the bridged Java object.
	 * Used when the other entity changes, to collect the Java object instances that must be reindexed.
	 * @return A context allowing to declare which properties
	 * @throws org.hibernate.search.util.common.SearchException If the bridged Java object type is not an entity type,
	 * or the given type is not an entity type,
	 * or the given path cannot be applied to the given entity type.
	 */
	@Incubating
	PojoOtherEntityIndexingDependencyConfigurationContext fromOtherEntity(Class<?> otherEntityType,
			PojoModelPathValueNode pathFromOtherEntityTypeToBridgedJavaObject);

}
