/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.integration.spring.massindexing.model;

import java.util.List;
import javax.persistence.EntityManager;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import org.apache.lucene.search.Query;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractIndexedEntityDao {

	@Transactional
	public void create(IndexedEntity entity) {
		getEntityManager().persist( entity );
	}

	@Transactional
	@SuppressWarnings("unchecked")
	public List<IndexedEntity> search(String terms) {
		FullTextEntityManager ftEntityManager = Search.getFullTextEntityManager( getEntityManager() );
		QueryBuilder queryBuilder = ftEntityManager.getSearchFactory().buildQueryBuilder()
				.forEntity( IndexedEntity.class ).get();
		Query luceneQuery = queryBuilder.keyword().onField( "name" ).matching( terms ).createQuery();
		FullTextQuery query = ftEntityManager.createFullTextQuery( luceneQuery, IndexedEntity.class );
		return query.getResultList();
	}

	@Transactional
	public int countUsingHibernateSearch() {
		FullTextEntityManager ftEntityManager = Search.getFullTextEntityManager( getEntityManager() );
		QueryBuilder queryBuilder = ftEntityManager.getSearchFactory().buildQueryBuilder()
				.forEntity( IndexedEntity.class ).get();
		Query luceneQuery = queryBuilder.all().createQuery();
		FullTextQuery query = ftEntityManager.createFullTextQuery( luceneQuery, IndexedEntity.class );
		return query.getResultSize();
	}

	protected abstract EntityManager getEntityManager();
}
