/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.impl;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.search.backend.elasticsearch.search.aggregation.dsl.ElasticsearchSearchAggregationFactory;
import org.hibernate.search.backend.elasticsearch.search.query.ElasticsearchSearchQuery;
import org.hibernate.search.backend.elasticsearch.search.query.ElasticsearchSearchRequestTransformer;
import org.hibernate.search.backend.elasticsearch.search.query.dsl.ElasticsearchSearchQueryOptionsStep;
import org.hibernate.search.backend.elasticsearch.search.sort.dsl.ElasticsearchSearchSortFactory;
import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.aggregation.dsl.AggregationFinalStep;
import org.hibernate.search.engine.search.sort.dsl.SortFinalStep;
import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api.PanacheElasticsearchQueryOptionsStep;
import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api.PanacheQuery;

class PanacheElasticsearchQueryOptionsStepImpl<HitSuperType, LOS>
		implements PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> {

	private ElasticsearchSearchQueryOptionsStep<?, LOS> delegate;

	PanacheElasticsearchQueryOptionsStepImpl(
			ElasticsearchSearchQueryOptionsStep<?, LOS> delegate) {
		this.delegate = delegate;
	}

	@Override
	public PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> routing(String routingKey) {
		delegate = delegate.routing( routingKey );
		return this;
	}

	@Override
	public PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> routing(Collection<String> routingKeys) {
		delegate = delegate.routing( routingKeys );
		return this;
	}

	@Override
	public PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> truncateAfter(long timeout, TimeUnit timeUnit) {
		delegate = delegate.truncateAfter( timeout, timeUnit );
		return this;
	}

	@Override
	public PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> failAfter(long timeout, TimeUnit timeUnit) {
		delegate = delegate.failAfter( timeout, timeUnit );
		return this;
	}

	@Override
	public PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> loading(Consumer<? super LOS> loadingOptionsContributor) {
		delegate = delegate.loading( loadingOptionsContributor );
		return this;
	}

	@Override
	public PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> sort(
			Function<? super ElasticsearchSearchSortFactory, ? extends SortFinalStep> contributor) {
		delegate = delegate.sort( contributor );
		return this;
	}

	@Override
	public PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> requestTransformer(
			ElasticsearchSearchRequestTransformer transformer) {
		delegate = delegate.requestTransformer( transformer );
		return this;
	}

	@Override
	public <T> PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> aggregation(AggregationKey<T> key,
			Function<? super ElasticsearchSearchAggregationFactory, ? extends AggregationFinalStep<T>> contributor) {
		delegate = delegate.aggregation( key, contributor );
		return this;
	}

	@Override
	public PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> totalHitCountThreshold(long totalHitCountThreshold) {
		delegate = delegate.totalHitCountThreshold( totalHitCountThreshold );
		return this;
	}

	@Override
	public <H extends HitSuperType> PanacheQuery<H> toQuery() {
		return new PanacheQueryDslImpl<>( toSearchQuery() );
	}

	@Override
	@SuppressWarnings("unchecked") // This has to be that way
	public <H extends HitSuperType> ElasticsearchSearchQuery<H> toSearchQuery() {
		return (ElasticsearchSearchQuery<H>) delegate.toQuery();
	}
}
