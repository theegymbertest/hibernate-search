/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.search.dsl.aggregation;

import java.util.Map;
import java.util.function.Function;

import org.hibernate.search.engine.search.aggregation.SearchAggregation;
import org.hibernate.search.engine.search.dsl.aggregation.AggregationFinalStep;

/**
 * The step in a bucket aggregation definition where the second per-bucket value can be set,
 * or the way to transform previously added values can be set.
 *
 * @param <K> The type of keys associated to each bucket.
 * @param <M> The type of metadata associated to each bucket.
 * @param <V1> The type of the first value to collect for each bucket.
 */
public interface ElasticsearchBucketAggregationValue2Step<K, M, V1>
		extends ElasticsearchBucketAggregationValue2OrMoreStep<K, M> {

	/**
	 * Sets the given function as the way to transform the single value into the value to return for each bucket.
	 *
	 * @param transformer The transformer for the values.
	 * @return The next step.
	 */
	<V> AggregationFinalStep<Map<K, V>> transform(Function<V1, V> transformer);

	@Override
	<V2> ElasticsearchBucketAggregationValue3Step<K, M, V1, V2> add(SearchAggregation<V2> subAggregation);

	@Override
	<V2> ElasticsearchBucketAggregationValue3Step<K, M, V1, V2> add(AggregationFinalStep<V2> subAggregation);

	@Override
	ElasticsearchBucketAggregationValue3Step<K, M, V1, Integer> addDocumentCount();

	@Override
	ElasticsearchBucketAggregationValue3Step<K, M, V1, M> addMetadata();

}
