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
import org.hibernate.search.engine.search.dsl.aggregation.RangeAggregationOptionsStep;
import org.hibernate.search.engine.search.dsl.aggregation.RangeAggregationRangeMoreStep;
import org.hibernate.search.util.common.data.Range;

public interface ElasticsearchRangeAggregationRangeMoreStep<F>
		extends RangeAggregationRangeMoreStep<
						ElasticsearchRangeAggregationRangeMoreStep<F>,
						ElasticsearchRangeAggregationOptionsStep<F, Map<F, Integer>>,
						F
				>,
				ElasticsearchRangeAggregationOptionsStep<F, Map<F, Integer>> {

}
