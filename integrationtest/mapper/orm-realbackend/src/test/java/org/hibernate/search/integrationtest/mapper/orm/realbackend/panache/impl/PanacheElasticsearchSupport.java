/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.impl;

import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.search.backend.elasticsearch.ElasticsearchExtension;
import org.hibernate.search.backend.elasticsearch.search.predicate.dsl.ElasticsearchSearchPredicateFactory;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.projection.SearchProjection;
import org.hibernate.search.engine.search.sort.SearchSort;
import org.hibernate.search.engine.search.sort.dsl.CompositeSortComponentsStep;
import org.hibernate.search.engine.search.sort.dsl.SearchSortFactory;
import org.hibernate.search.engine.search.sort.dsl.SortOrder;
import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api.PanacheElasticsearchQuerySelectStep;
import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api.PanacheQuery;
import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api.Sort;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.search.loading.dsl.SearchLoadingOptionsStep;

public class PanacheElasticsearchSupport {
	public static EntityManagerFactory currentEntityManagerFactory;
	public static EntityManager currentEntityManager;

	public static <Entity> PanacheElasticsearchQuerySelectStep<Entity, SearchLoadingOptionsStep> search(Class<? extends Entity> entityType) {
		return new PanacheElasticsearchQuerySelectStepImpl<>( Search.session( currentEntityManager )
				.search( entityType ).extension( ElasticsearchExtension.get() ) );
	}

	public static <T, Entity> PanacheQuery<T> search(Class<? extends Entity> entityType,
			Function<? super ElasticsearchSearchPredicateFactory, ? extends PredicateFinalStep> predicateContributor) {
		return search( entityType, predicateContributor, null );
	}

	@SuppressWarnings("unchecked") // Not pretty, but that's how it is.
	public static <T, Entity> PanacheQuery<T> search(Class<? extends Entity> entityType,
			Function<? super ElasticsearchSearchPredicateFactory, ? extends PredicateFinalStep> predicateContributor,
			Sort sort) {
		SearchScope<? extends Entity> scope = Search.mapping( currentEntityManagerFactory ).scope( entityType );
		SearchProjection<? extends Entity> searchProjection = scope.projection().entity().toProjection();
		SearchPredicate searchPredicate = toSearchPredicate( scope, predicateContributor );
		SearchSort searchSort = toSearchSort( scope, sort );
		return (PanacheQuery<T>) new PanacheQuerySimpleMethodImpl<>(
				scope, searchProjection, searchPredicate, searchSort );
	}

	private static SearchPredicate toSearchPredicate(SearchScope<?> scope,
			Function<? super ElasticsearchSearchPredicateFactory, ? extends PredicateFinalStep> predicateContributor) {
		return predicateContributor.apply(
				scope.predicate().extension( ElasticsearchExtension.get() ) )
				.toPredicate();
	}

	private static SearchSort toSearchSort(SearchScope<?> scope, Sort sort) {
		if ( sort == null ) {
			return null;
		}
		SearchSortFactory factory = scope.sort();
		CompositeSortComponentsStep<?> builder = factory.composite();
		for ( Sort.Column column : sort.getColumns() ) {
			builder.add( factory.field( column.getName() ).order( toSortOrder( column.getDirection() ) ) );
		}
		return builder.toSort();
	}

	private static SortOrder toSortOrder(Sort.Direction direction) {
		switch ( direction ) {
			case Ascending:
				return SortOrder.ASC;
			case Descending:
				return SortOrder.DESC;
			default:
				throw new IllegalStateException( "Unknown direction: " + direction );
		}
	}
}
