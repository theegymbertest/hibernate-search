/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.engine.search.query.SearchScroll;
import org.hibernate.search.util.common.SearchException;
import org.hibernate.search.util.common.SearchTimeoutException;

public interface PanacheElasticsearchQueryFetchStep {

	/**
	 * Execute the query and return the {@link SearchResult},
	 * limiting to {@code limit} hits.
	 *
	 * @param limit The maximum number of hits to be included in the {@link SearchResult}. {@code null} means no limit.
	 * @return The {@link SearchResult}.
	 * @throws SearchException If something goes wrong while executing the query.
	 * @throws SearchTimeoutException If a
	 * {@link org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep#failAfter(long, TimeUnit) failure timeout was set}
	 * and was reached while executing the query.
	 * @throws RuntimeException If something goes wrong while loading entities. The exact type depends on the mapper,
	 * e.g. HibernateException/PersistenceException for the Hibernate ORM mapper.
	 */
	default <H> SearchResult<H> fetch(Integer limit) {
		return this.<H>toQuery().fetch( limit );
	}

	/**
	 * Execute the query and return the {@link SearchResult},
	 * skipping {@code offset} hits and limiting to {@code limit} hits.
	 *
	 * @param offset The number of hits to skip before adding the hits to the {@link SearchResult}. {@code null} means no offset.
	 * @param limit The maximum number of hits to be included in the {@link SearchResult}. {@code null} means no limit.
	 * @return The {@link SearchResult}.
	 * @throws SearchException If something goes wrong while executing the query.
	 * @throws SearchTimeoutException If a
	 * {@link org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep#failAfter(long, TimeUnit) failure timeout was set}
	 * and was reached while executing the query.
	 * @throws RuntimeException If something goes wrong while loading entities. The exact type depends on the mapper,
	 * e.g. HibernateException/PersistenceException for the Hibernate ORM mapper.
	 */
	default <H> SearchResult<H> fetch(Integer offset, Integer limit) {
		return this.<H>toQuery().fetch( offset, limit );
	}

	/**
	 * Execute the query and return the hits as a {@link List},
	 * limiting to {@code limit} hits.
	 *
	 * @param limit The maximum number of hits to be returned by this method. {@code null} means no limit.
	 * @return The query hits.
	 * @throws SearchException If something goes wrong while executing the query.
	 * @throws SearchTimeoutException If a
	 * {@link org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep#failAfter(long, TimeUnit) failure timeout was set}
	 * and was reached while executing the query.
	 * @throws RuntimeException If something goes wrong while loading entities. The exact type depends on the mapper,
	 * e.g. HibernateException/PersistenceException for the Hibernate ORM mapper.
	 */
	default <H> List<H> fetchHits(Integer limit) {
		return this.<H>toQuery().fetchHits( limit );
	}

	/**
	 * Execute the query and return the hits as a {@link List},
	 * skipping {@code offset} hits and limiting to {@code limit} hits.
	 *
	 * @param offset The number of hits to skip. {@code null} means no offset.
	 * @param limit The maximum number of hits to be returned by this method. {@code null} means no limit.
	 * @return The query hits.
	 * @throws SearchException If something goes wrong while executing the query.
	 * @throws SearchTimeoutException If a
	 * {@link org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep#failAfter(long, TimeUnit) failure timeout was set}
	 * and was reached while executing the query.
	 * @throws RuntimeException If something goes wrong while loading entities. The exact type depends on the mapper,
	 * e.g. HibernateException/PersistenceException for the Hibernate ORM mapper.
	 */
	default <H> List<H> fetchHits(Integer offset, Integer limit) {
		return this.<H>toQuery().fetchHits( offset, limit );
	}

	/**
	 * Execute the query and return the hits as a single, optional element.
	 *
	 * @return The single, optional query hit.
	 * @throws SearchException If something goes wrong while executing the query,
	 * or the number of hits is more than one.
	 * @throws SearchTimeoutException If a
	 * {@link org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep#failAfter(long, TimeUnit) failure timeout was set}
	 * and was reached while executing the query.
	 * @throws RuntimeException If something goes wrong while loading entities. The exact type depends on the mapper,
	 * e.g. HibernateException/PersistenceException for the Hibernate ORM mapper.
	 */
	default <H> Optional<H> fetchSingleHit() {
		return this.<H>toQuery().fetchSingleHit();
	}

	/**
	 * Execute the query and return the total hit count.
	 *
	 * @return The total number of matching entities, ignoring pagination settings.
	 * @throws SearchException If something goes wrong while executing the query.
	 * @throws SearchTimeoutException If a
	 * {@link org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep#failAfter(long, TimeUnit) failure timeout was set}
	 * and was reached while executing the query.
	 */
	default long fetchTotalHitCount() {
		return this.toQuery().fetchTotalHitCount();
	}

	/**
	 * Execute the query and return the {@link SearchResult},
	 * including <strong>all</strong> hits, without any sort of limit.
	 * <p>
	 * {@link #fetch(Integer)} or {@link #fetch(Integer, Integer)} should generally be preferred, for performance reasons.
	 *
	 * @return The {@link SearchResult}.
	 * @throws SearchException If something goes wrong while executing the query.
	 * @throws SearchTimeoutException If a
	 * {@link org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep#failAfter(long, TimeUnit) failure timeout was set}
	 * and was reached while executing the query.
	 * @throws RuntimeException If something goes wrong while loading entities. The exact type depends on the mapper,
	 * e.g. HibernateException/PersistenceException for the Hibernate ORM mapper.
	 */
	default <H> SearchResult<H> fetchAll() {
		return this.<H>toQuery().fetchAll();
	}

	/**
	 * Execute the query and return <strong>all</strong> hits as a {@link List},
	 * without any sort of limit.
	 * <p>
	 * {@link #fetchHits(Integer)} or {@link #fetchHits(Integer, Integer)} should generally be preferred,
	 * for performance reasons.
	 *
	 * @return The query hits.
	 * @throws SearchException If something goes wrong while executing the query.
	 * @throws SearchTimeoutException If a
	 * {@link org.hibernate.search.engine.search.query.dsl.SearchQueryOptionsStep#failAfter(long, TimeUnit) failure timeout was set}
	 * and was reached while executing the query.
	 * @throws RuntimeException If something goes wrong while loading entities. The exact type depends on the mapper,
	 * e.g. HibernateException/PersistenceException for the Hibernate ORM mapper.
	 */
	default <H> List<H> fetchAllHits() {
		return this.<H>toQuery().fetchAllHits();
	}

	/**
	 * Execute the query continuously to deliver results in small chunks through a {@link SearchScroll}.
	 * <p>
	 * Useful to process large datasets.
	 *
	 * @param chunkSize The maximum number of hits to be returned for each call to {@link SearchScroll#next()}
	 * @return The {@link SearchScroll}.
	 * @throws IllegalArgumentException if passed 0 or less for {@code chunkSize}.
	 */
	default <H> SearchScroll<H> scroll(int chunkSize) {
		return this.<H>toQuery().scroll( chunkSize );
	}

	/**
	 * Create a {@link SearchQuery} instance
	 * matching the definition given in the previous DSL steps.
	 * <p>
	 * Calling this method is generally not necessary as most query execution methods
	 * are also implemented by this DSL step,
	 * so for example {@code .toQuery().fetch()} can be replaced with simply {@code .fetch()}.
	 *
	 * @return The {@link SearchQuery} resulting from the previous DSL steps.
	 */
	<H> SearchQuery<H> toQuery();

}
