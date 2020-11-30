/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.hibernate.search.backend.elasticsearch.search.query.ElasticsearchSearchQuery;
import org.hibernate.search.engine.search.query.SearchQuery;
import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api.Page;
import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api.PanacheQuery;

public class PanacheQueryDslImpl<HitSuperType> implements PanacheQuery<HitSuperType> {

	private final SearchQuery<HitSuperType> delegate;

	private Page page = Page.ofSize( 20 );

	public PanacheQueryDslImpl(SearchQuery<HitSuperType> delegate) {
		this.delegate = delegate;
	}

	@Override
	public <T> PanacheQuery<T> project(Class<T> type) {
		throw new UnsupportedOperationException("Not possible with the DSL approach: it's too late to set the return type.");
	}

	@Override
	public <T extends HitSuperType> PanacheQuery<T> page(Page page) {
		this.page = page;
		return castThis();
	}

	@Override
	public <T extends HitSuperType> PanacheQuery<T> page(int pageIndex, int pageSize) {
		return null;
	}

	@Override
	public <T extends HitSuperType> PanacheQuery<T> nextPage() {
		page( page.next() );
		return castThis();
	}

	@Override
	public <T extends HitSuperType> PanacheQuery<T> previousPage() {
		page( page.previous() );
		return castThis();
	}

	@Override
	public <T extends HitSuperType> PanacheQuery<T> firstPage() {
		page( page.first() );
		return castThis();
	}

	@Override
	public <T extends HitSuperType> PanacheQuery<T> lastPage() {
		page( page.index( pageCount() - 1 ) );
		return castThis();
	}

	@Override
	public boolean hasNextPage() {
		return page.index < ( pageCount() - 1 );
	}

	@Override
	public boolean hasPreviousPage() {
		return page.index > 0;
	}

	@Override
	public int pageCount() {
		long count = count();
		if ( count == 0 ) {
			return 1; // a single page of zero results
		}
		return (int) Math.ceil( (double) count / (double) page.size );
	}

	@Override
	public Page page() {
		return page;
	}

	@Override
	public <T extends HitSuperType> PanacheQuery<T> range(int startIndex, int lastIndex) {
		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	@Override
	public long count() {
		return toSearchQuery().fetchTotalHitCount();
	}

	@Override
	public <T extends HitSuperType> List<T> list() {
		return this.<T>toSearchQuery().fetchHits( offset(), limit() );
	}

	private int offset() {
		if ( page != null ) {
			return page.index * page.size;
		}
		else {
			return 0;
		}
	}

	private int limit() {
		if ( page != null ) {
			return page.size;
		}
		else {
			return 20;
		}
	}


	@Override
	public <T extends HitSuperType> Stream<T> stream() {
		return this.<T>list().stream();
	}

	@Override
	public <T extends HitSuperType> T firstResult() {
		return this.<T>toSearchQuery().fetchSingleHit().get();
	}

	@Override
	public <T extends HitSuperType> Optional<T> firstResultOptional() {
		return this.<T>toSearchQuery().fetchSingleHit();
	}

	@Override
	public <T extends HitSuperType> T singleResult() {
		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	@Override
	public <T extends HitSuperType> Optional<T> singleResultOptional() {
		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	@Override
	public <T extends HitSuperType> ElasticsearchSearchQuery<T> toSearchQuery() {
		return (ElasticsearchSearchQuery<T>) delegate;
	}

	@SuppressWarnings("unchecked") // This has to be that way
	private <T extends HitSuperType> PanacheQuery<T> castThis() {
		return (PanacheQuery<T>) this;
	}
}
