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
import org.hibernate.search.engine.search.dsl.aggregation.TermsAggregationOptionsStep;

public interface ElasticsearchTermsAggregationOptionsStep<F, A>
		extends TermsAggregationOptionsStep<ElasticsearchTermsAggregationOptionsStep<F, A>, F, A> {

	/**
	 * Collect the given aggregation within each bucket (i.e. for each term),
	 * instead of just the document count.
	 * <p>
	 * Calling this method will end the aggregation definition and return a final DSL step,
	 * so it must be called last.
	 *
	 * @param subAggregation The sub-aggregation to be collected within each bucket.
	 * @param <B> The type of values produced by the sub-aggregation.
	 * @return The next step.
	 */
	<B> AggregationFinalStep<Map<F, B>> subAggregation(SearchAggregation<B> subAggregation);

	/**
	 * Collect the given aggregation within each bucket (i.e. for each term),
	 * instead of just the document count.
	 * <p>
	 * Calling this method will end the aggregation definition and return a final DSL step,
	 * so it must be called last.
	 *
	 * @param subAggregation The (almost-built) sub-aggregation to be collected within each bucket.
	 * @param <B> The type of values produced by the sub-aggregation.
	 * @return The next step.
	 */
	<B> AggregationFinalStep<Map<F, B>> subAggregation(AggregationFinalStep<B> subAggregation);

}
