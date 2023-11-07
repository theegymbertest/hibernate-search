/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.mapping.definition.programmatic;

import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.engine.backend.types.VectorSimilarity;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.VectorField;

/**
 * The step in a property-to-index-field mapping where optional parameters can be set,
 * when the index field is a vector field.
 *
 * @see VectorField
 */
public interface PropertyMappingVectorOptionsFieldStep extends PropertyMappingStep {

	/**
	 * @param projectable Whether this field should be projectable.
	 * @return {@code this}, for method chaining.
	 * @see VectorField#projectable() ()
	 * @see Projectable
	 */
	PropertyMappingVectorOptionsFieldStep projectable(Projectable projectable);

	/**
	 * @param vectorSimilarity method of calculating the vector similarity, i.e. distance between vectors.
	 * @return {@code this}, for method chaining.
	 * @see VectorField#vectorSimilarity()
	 * @see VectorSimilarity
	 */
	PropertyMappingVectorOptionsFieldStep vectorSimilarity(VectorSimilarity vectorSimilarity);

	/**
	 * @param beamWidth The size of the dynamic list used during k-NN graph creation.
	 * @return {@code this}, for method chaining.
	 * @see VectorField#beamWidth()
	 */
	PropertyMappingVectorOptionsFieldStep beamWidth(int beamWidth);

	/**
	 * @param maxConnections The number of neighbors each node will be connected to in the HNSW graph.
	 * @return {@code this}, for method chaining.
	 * @see VectorField#maxConnections()
	 */
	PropertyMappingVectorOptionsFieldStep maxConnections(int maxConnections);

	/**
	 * @param indexNullAs A value used instead of null values when indexing.
	 * @return {@code this}, for method chaining.
	 * @see VectorField#indexNullAs()
	 */
	PropertyMappingVectorOptionsFieldStep indexNullAs(String indexNullAs);

}
