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
 * The step in a bucket aggregation definition where the first or more per-bucket value can be set.
 *
 * @param <K> The type of keys associated to each bucket.
 * @param <M> The type of metadata associated to each bucket.
 */
public interface ElasticsearchBucketAggregationValue1OrMoreStep<K, M> {

	/**
	 * Sets the given aggregation as the next value to collect for each bucket.
	 *
	 * @param subAggregation The first sub-aggregation to be collected within each bucket.
	 * @param <V> The type of values produced by the sub-aggregation.
	 * @return The next step.
	 */
	<V> ElasticsearchBucketAggregationValue2OrMoreStep<K, M> add(SearchAggregation<V> subAggregation);

	/**
	 * Sets the given aggregation as the second value to collect for each bucket.
	 *
	 * @param subAggregation The first sub-aggregation to be collected within each bucket.
	 * @param <V3> The type of values produced by the sub-aggregation.
	 * @return The next step.
	 */
	<V3> ElasticsearchBucketAggregationValue2OrMoreStep<K, M> add(AggregationFinalStep<V3> subAggregation);

	/**
	 * Sets the document count as the next value to collect for each bucket.
	 *
	 * @return The next step.
	 */
	ElasticsearchBucketAggregationValue2OrMoreStep<K, M> addDocumentCount();

	/**
	 * Sets the per-bucket metadata as the next value to collect for each bucket.
	 *
	 * @return The next step.
	 */
	ElasticsearchBucketAggregationValue2OrMoreStep<K, M> addMetadata();

}
