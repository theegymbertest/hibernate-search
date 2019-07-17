/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.search.dsl.aggregation;

import java.util.Map;

import org.hibernate.search.engine.search.dsl.aggregation.TermsAggregationFieldStep;
import org.hibernate.search.engine.search.dsl.aggregation.TermsAggregationOptionsStep;
import org.hibernate.search.engine.search.projection.ProjectionConverter;

public interface ElasticsearchTermsAggregationFieldStep
		extends TermsAggregationFieldStep {

	@Override
	<F> ElasticsearchTermsAggregationOptionsStep<F, Map<F, Integer>> field(String absoluteFieldPath, Class<F> type);

	@Override
	<F> ElasticsearchTermsAggregationOptionsStep<F, Map<F, Integer>> field(String absoluteFieldPath, Class<F> type,
			ProjectionConverter projectionConverter);
}
