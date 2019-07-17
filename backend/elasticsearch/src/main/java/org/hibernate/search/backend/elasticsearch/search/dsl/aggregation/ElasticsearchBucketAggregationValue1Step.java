/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.search.dsl.aggregation;

import org.hibernate.search.engine.search.aggregation.SearchAggregation;
import org.hibernate.search.engine.search.dsl.aggregation.AggregationFinalStep;

/**
 * The step in a bucket aggregation definition where the first per-bucket value can be set.
 *
 * @param <K> The type of keys associated to each bucket.
 * @param <M> The type of metadata associated to each bucket.
 */
public interface ElasticsearchBucketAggregationValue1Step<K, M>
		extends ElasticsearchBucketAggregationValue1OrMoreStep<K, M> {

	@Override
	<V1> ElasticsearchBucketAggregationValue2Step<K, M, V1> add(SearchAggregation<V1> subAggregation);

	@Override
	<V1> ElasticsearchBucketAggregationValue2Step<K, M, V1> add(AggregationFinalStep<V1> subAggregation);

	@Override
	ElasticsearchBucketAggregationValue2Step<K, M, Integer> addDocumentCount();

	@Override
	ElasticsearchBucketAggregationValue2Step<K, M, M> addMetadata();
}
