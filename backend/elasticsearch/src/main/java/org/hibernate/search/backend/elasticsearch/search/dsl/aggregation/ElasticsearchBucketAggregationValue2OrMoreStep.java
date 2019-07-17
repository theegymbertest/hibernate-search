/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.search.dsl.aggregation;

import java.util.List;
import java.util.Map;

import org.hibernate.search.engine.search.dsl.aggregation.AggregationFinalStep;

/**
 * The step in a bucket aggregation definition where the second or more per-bucket value can be set,
 * or the way to transform previously added values can be set.
 *
 * @param <K> The type of keys associated to each bucket.
 * @param <M> The type of metadata associated to each bucket.
 */
public interface ElasticsearchBucketAggregationValue2OrMoreStep<K, M>
		extends ElasticsearchBucketAggregationValue1OrMoreStep<K, M> {

	/**
	 * Instructs to transform the values into a {@code List<?>} for each bucket.
	 *
	 * @return The next step.
	 */
	AggregationFinalStep<Map<K, List<?>>> transformToList();

}
