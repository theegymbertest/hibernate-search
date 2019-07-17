/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.search.dsl.aggregation;

import org.hibernate.search.engine.search.dsl.aggregation.TermsAggregationOptionsStep;

public interface ElasticsearchTermsAggregationOptionsStep<F, A>
		extends TermsAggregationOptionsStep<ElasticsearchTermsAggregationOptionsStep<F, A>, F, A>,
				ElasticsearchBucketAggregationValuesStep<F> {

}
