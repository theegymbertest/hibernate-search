/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.backend.types.dsl;

import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.engine.backend.types.VectorSimilarity;

/**
 * TODO: vector : docs
 *
 * @param <F> The type of field values.
 */

public interface VectorFieldTypeOptionsStep<S extends VectorFieldTypeOptionsStep<?, F>, F>
		extends IndexFieldTypeOptionsStep<S, F> {

	S dimension(int dimension);

	S projectable(Projectable projectable);

	S vectorSimilarity(VectorSimilarity vectorSimilarity);

	S beamWidth(int beamWidth);

	S maxConnections(int maxConnections);

	/**
	 * @param indexNullAs A value used instead of null values when indexing.
	 * @return {@code this}, for method chaining.
	 */
	S indexNullAs(F indexNullAs);

}
