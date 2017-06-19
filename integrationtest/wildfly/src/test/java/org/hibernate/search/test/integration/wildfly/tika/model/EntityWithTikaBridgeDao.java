/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.integration.wildfly.tika.model;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaDelete;
import javax.transaction.Transactional;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * @author Yoann Rodiere
 */
@Singleton
public class EntityWithTikaBridgeDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void create(EntityWithTikaBridge entity) {
		entityManager.persist( entity );
	}

	@Transactional
	public void delete(EntityWithTikaBridge entity) {
		entity = entityManager.merge( entity );
		entityManager.remove( entity );
	}

	@Transactional
	public void deleteAll() {
		CriteriaDelete<EntityWithTikaBridge> delete = entityManager.getCriteriaBuilder()
				.createCriteriaDelete( EntityWithTikaBridge.class );
		delete.from( EntityWithTikaBridge.class );
		entityManager.createQuery( delete ).executeUpdate();
	}

	@Transactional
	@SuppressWarnings("unchecked")
	public List<EntityWithTikaBridge> search(String terms) {
		FullTextEntityManager ftEntityManager = Search.getFullTextEntityManager( entityManager );
		QueryBuilder queryBuilder = ftEntityManager.getSearchFactory().buildQueryBuilder()
				.forEntity( EntityWithTikaBridge.class ).get();
		Query luceneQuery = queryBuilder.keyword()
				.onField( "internationalizedValue" )
				.ignoreFieldBridge()
				.matching( terms )
				.createQuery();
		FullTextQuery query = ftEntityManager.createFullTextQuery( luceneQuery, EntityWithTikaBridge.class );
		return query.getResultList();
	}
}