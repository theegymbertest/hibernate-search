/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.search.backend.elasticsearch.search.aggregation.dsl.ElasticsearchSearchAggregationFactory;
import org.hibernate.search.backend.elasticsearch.search.query.ElasticsearchSearchQuery;
import org.hibernate.search.backend.elasticsearch.search.query.ElasticsearchSearchRequestTransformer;
import org.hibernate.search.backend.elasticsearch.search.sort.dsl.ElasticsearchSearchSortFactory;
import org.hibernate.search.engine.search.aggregation.AggregationKey;
import org.hibernate.search.engine.search.aggregation.dsl.AggregationFinalStep;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.query.SearchResultTotal;
import org.hibernate.search.engine.search.sort.dsl.SortFinalStep;

public interface PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> {

	/**
	 * Configure routing of the search query.
	 * <p>
	 * Useful when indexes are sharded, to limit the number of shards interrogated by the search query.
	 * <p>
	 * This method may be called multiple times,
	 * in which case all submitted routing keys will be taken into account.
	 * <p>
	 * By default, if routing is not configured, all shards will be queried.
	 *
	 * @param routingKey A string key. All shards matching this key will be queried.
	 * @return {@code this}, for method chaining.
	 */
	PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> routing(String routingKey);

	/**
	 * Configure routing of the search query.
	 * <p>
	 * Similar to {@link #routing(String)}, but allows passing multiple keys in a single call.
	 *
	 * @param routingKeys A collection containing zero, one or multiple string keys.
	 * @return {@code this}, for method chaining.
	 */
	PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> routing(Collection<String> routingKeys);

	/**
	 * Stop the query and return truncated results after a given timeout.
	 * <p>
	 * The timeout is handled on a best effort basis:
	 * Hibernate Search will *try* to stop the query as soon as possible after the timeout.
	 *
	 * @param timeout Timeout value.
	 * @param timeUnit Timeout unit.
	 * @return {@code this}, for method chaining.
	 */
	PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> truncateAfter(long timeout, TimeUnit timeUnit);

	/**
	 * Stop the query and throw a {@link org.hibernate.search.util.common.SearchTimeoutException} after a given timeout.
	 * <p>
	 * The timeout is handled on a best effort basis:
	 * Hibernate Search will *try* to stop the query as soon as possible after the timeout.
	 * However, this method is more likely to trigger an early stop than {@link #truncateAfter(long, TimeUnit)}.
	 *
	 * @param timeout Timeout value.
	 * @param timeUnit Timeout unit.
	 * @return {@code this}, for method chaining.
	 */
	PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> failAfter(long timeout, TimeUnit timeUnit);

	/**
	 * Configure entity loading for this query.
	 * @param loadingOptionsContributor A consumer that will alter the loading options passed in parameter.
	 * Should generally be a lambda expression.
	 * @return {@code this}, for method chaining.
	 */
	PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> loading(Consumer<? super LOS> loadingOptionsContributor);

	/**
	 * Add a sort to this query.
	 * @param contributor A function that will use the factory passed in parameter to create a sort,
	 * returning the final step in the sort DSL.
	 * Should generally be a lambda expression.
	 * @return {@code this}, for method chaining.
	 */
	PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> sort(
			Function<? super ElasticsearchSearchSortFactory, ? extends SortFinalStep> contributor);

	/**
	 * Set the {@link ElasticsearchSearchRequestTransformer} for this search query.
	 * <p>
	 * <strong>WARNING:</strong> Direct changes to the request may conflict with Hibernate Search features
	 * and be supported differently by different versions of Elasticsearch.
	 * Thus they cannot be guaranteed to continue to work when upgrading Hibernate Search,
	 * even for micro upgrades ({@code x.y.z} to {@code x.y.(z+1)}).
	 * Use this at your own risk.
	 *
	 * @param transformer The search request transformer.
	 * @return {@code this}, for method chaining.
	 */
	PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> requestTransformer(ElasticsearchSearchRequestTransformer transformer);

	/**
	 * Add an aggregation to this query.
	 * @param key The key that will be used to {@link SearchResult#aggregation(AggregationKey) retrieve the aggregation}
	 * from the {@link SearchResult}.
	 * @param contributor A function that will use the factory passed in parameter to create an aggregation,
	 * returning the final step in the sort DSL.
	 * Should generally be a lambda expression.
	 * @param <T> The type of aggregation values.
	 * @return {@code this}, for method chaining.
	 */
	<T> PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> aggregation(AggregationKey<T> key,
			Function<? super ElasticsearchSearchAggregationFactory, ? extends AggregationFinalStep<T>> contributor);

	/**
	 * Allow Hibernate Search to return a lower-bound estimate of the total hit count
	 * if it exceeds {@code totalHitCountThreshold}.
	 * <p>
	 * Allowing Hibernate Search to return a lower-bound estimate of the total hit count can lead to significantly fewer
	 * index scans and yield significant performance improvements,
	 * in particular when sorting by score (the default) on a large result set.
	 * <p>
	 * Note this optimization has no effect when also requesting aggregations.
	 *
	 * @param totalHitCountThreshold the value below which the hit count is always exact
	 * @return {@code this}, for method chaining.
	 * @see SearchResultTotal
	 */
	PanacheElasticsearchQueryOptionsStep<HitSuperType, LOS> totalHitCountThreshold(long totalHitCountThreshold);

	<H extends HitSuperType> PanacheQuery<H> toQuery();

	<H extends HitSuperType> ElasticsearchSearchQuery<H> toSearchQuery();
}
