/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.search.dsl.aggregation;

import java.util.Map;

import org.hibernate.search.engine.search.aggregation.SearchAggregation;
import org.hibernate.search.engine.search.dsl.aggregation.AggregationFinalStep;

/**
 * The final step in a bucket aggregation definition where per-bucket values can be defined.
 *
 * @param <K> The type of keys associated to each bucket.
 * @param <M> The type of metadata associated to each bucket.
 */
public interface ElasticsearchBucketAggregationValuesStep<K, M> {

	/**
	 * Return the given aggregation as value for each bucket,
	 * instead of just returning the document count.
	 * <p>
	 * Calling this method will end the aggregation definition and return a final DSL step,
	 * so it must be called last.
	 *
	 * @param subAggregation The sub-aggregation to be collected within each bucket.
	 * @param <B> The type of values produced by the sub-aggregation.
	 * @return The next step.
	 */
	<B> AggregationFinalStep<Map<K, B>> values(SearchAggregation<B> subAggregation);

	/**
	 * Return the given aggregation as value for each bucket,
	 * instead of just returning the document count.
	 * <p>
	 * Calling this method will end the aggregation definition and return a final DSL step,
	 * so it must be called last.
	 *
	 * @param subAggregation The (almost-built) sub-aggregation to be collected within each bucket.
	 * @param <B> The type of values produced by the sub-aggregation.
	 * @return The next step.
	 */
	<B> AggregationFinalStep<Map<K, B>> values(AggregationFinalStep<B> subAggregation);

	/**
	 * Start the definition of the values to return for each bucket,
	 * instead of just returning the document count.
	 * <p>
	 * Calling this method will end the aggregation definition and lead to a final DSL step,
	 * so it must be called last.
	 *
	 * @return The next step.
	 */
	ElasticsearchBucketAggregationValue1Step<K, M> values();

}
