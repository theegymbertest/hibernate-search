/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.jsr352.massindexing.impl.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.search.util.StringHelper;

/**
 * Internal utility class for persistence usage.
 *
 * @author Mincong Huang
 */
public final class PersistenceUtil {

	private PersistenceUtil() {
		// Private constructor, do not use it.
	}

	/**
	 * Open a session with specific tenant ID. If the tenant ID argument is {@literal null} or empty, then a normal
	 * session will be returned. The entity manager factory should be not null and opened when calling this method.
	 *
	 * @param entityManagerFactory entity manager factory
	 * @param tenantId tenant ID, can be {@literal null} or empty.
	 *
	 * @return a new session
	 */
	public static Session openSession(EntityManagerFactory entityManagerFactory, String tenantId) {
		SessionFactory sessionFactory = entityManagerFactory.unwrap( SessionFactory.class );
		Session session;
		if ( StringHelper.isEmpty( tenantId ) ) {
			session = sessionFactory.openSession();
		}
		else {
			session = sessionFactory.withOptions()
					.tenantIdentifier( tenantId )
					.openSession();
		}
		return session;
	}

	/**
	 * Open a stateless session with specific tenant ID. If the tenant ID argument is {@literal null} or empty, then a
	 * normal stateless session will be returned. The entity manager factory should be not null and opened when calling
	 * this method.
	 *
	 * @param entityManagerFactory entity manager factory
	 * @param tenantId tenant ID, can be {@literal null} or empty.
	 *
	 * @return a new stateless session
	 */
	public static StatelessSession openStatelessSession(EntityManagerFactory entityManagerFactory, String tenantId) {
		SessionFactory sessionFactory = entityManagerFactory.unwrap( SessionFactory.class );
		StatelessSession statelessSession;
		if ( StringHelper.isEmpty( tenantId ) ) {
			statelessSession = sessionFactory.openStatelessSession();
		}
		else {
			statelessSession = sessionFactory.withStatelessOptions()
					.tenantIdentifier( tenantId )
					.openStatelessSession();
		}
		return statelessSession;
	}

	/**
	 * @see #createProjectionCriteria(EntityManagerFactory, StatelessSession, Class, Set)
	 */
	public static <X> Criteria createProjectionCriteria(
			EntityManagerFactory entityManagerFactory,
			StatelessSession statelessSession,
			Class<X> entity) {
		return createProjectionCriteria( entityManagerFactory, statelessSession, entity, null );
	}

	/**
	 * TODO
	 * @param entityManagerFactory
	 * @param statelessSession
	 * @param entity
	 * @param <X>
	 * @return
	 */
	public static <X> Criteria createProjectionCriteria(
			EntityManagerFactory entityManagerFactory,
			StatelessSession statelessSession,
			Class<X> entity,
			Set<Criterion> criterionSet) {
		Criteria criteria = statelessSession.createCriteria( entity );
		if ( criterionSet != null ) {
			criterionSet.forEach( criteria::add );
		}

		EntityType<X> entityType = entityManagerFactory.getMetamodel().entity( entity );

		if ( entityType.hasSingleIdAttribute() ) {
			// TODO Find embedded id?
			criteria.setProjection( Projections.alias( Projections.id(), "aliasedId" ) );
			criteria.addOrder( Order.asc( "aliasedId" ) );
		}
		else {
			List<SingularAttribute<? super X, ?>> attributeList = new ArrayList<>( entityType.getIdClassAttributes() );
			attributeList.sort( Comparator.comparing( Attribute::getName ) );
			attributeList.forEach( attr -> criteria.addOrder( Order.asc( attr.getName() ) ) );
			criteria.setProjection( Projections.id() );
		}
		return criteria;
	}

	// TODO Merge with the method above
	public static <X> Criteria createNormalCriteria(
			EntityManagerFactory entityManagerFactory,
			StatelessSession statelessSession,
			Class<X> entity) {
		EntityType<X> entityType = entityManagerFactory.getMetamodel().entity( entity );
		Criteria criteria = statelessSession.createCriteria( entity );

		if ( entityType.hasSingleIdAttribute() ) {
			// TODO Find embedded id?
		}
		else {
			List<SingularAttribute<? super X, ?>> attributeList = new ArrayList<>( entityType.getIdClassAttributes() );
			attributeList.sort( Comparator.comparing( Attribute::getName ) );
			attributeList.forEach( attr -> criteria.addOrder( Order.asc( attr.getName() ) ) );
		}
		return criteria;
	}

}
