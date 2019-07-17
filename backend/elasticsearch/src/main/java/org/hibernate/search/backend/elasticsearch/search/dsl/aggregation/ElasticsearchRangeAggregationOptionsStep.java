/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.search.dsl.aggregation;

import org.hibernate.search.engine.search.dsl.aggregation.RangeAggregationOptionsStep;
import org.hibernate.search.util.common.data.Range;

public interface ElasticsearchRangeAggregationOptionsStep<F, A>
		extends RangeAggregationOptionsStep<ElasticsearchRangeAggregationOptionsStep<F, A>, F, A>,
				// TODO HSEARCH-3271 define a metadata type for range aggregation buckets
				ElasticsearchBucketAggregationValuesStep<Range<F>, Object> {

}
