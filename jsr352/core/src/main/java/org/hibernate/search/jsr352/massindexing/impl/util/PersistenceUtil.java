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
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

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
	 * TODO
	 * @param entityManagerFactory
	 * @param statelessSession
	 * @param entity
	 * @param <X>
	 * @return
	 */
	@Deprecated
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
			// TODO Can @EmbeddedId and @Id be handled together?
			Type<?> idType = entityType.getIdType();
			Class<?> idJavaType = idType.getJavaType();
			String idName = entityType.getId( idJavaType ).getName();

			if ( idType.getPersistenceType() == Type.PersistenceType.EMBEDDABLE ) {
				// TODO Extract this part into a private method
				// HQL without ID sorting:
//				Hibernate:
//				select
//					this_.day as y0_,
//					this_.month as y1_,
//					this_.year as y2_
//				from
//					EntityWithNonComparableId this_
//				where
//					(
//						this_.day>=?
//						and this_.month>=?
//						and this_.year>=?
//					)
				// HQL with ID sorting:
				// TODO The WHERE clause need to be changed
//				Hibernate:
//				select
//					this_.day as y0_,
//					this_.month as y1_,
//					this_.year as y2_
//				from
//					EntityWithNonComparableId this_
//				where
//					(
//						this_.day>=?
//						and this_.month>=?
//						and this_.year>=?
//					)
//				order by
//					this_.day asc,
//					this_.month asc,
//					this_.year asc

				List<SingularAttribute<?, ?>> attributeList;
				EmbeddableType<?> embeddableType = entityManagerFactory.getMetamodel().embeddable( idJavaType );
				attributeList = new ArrayList<>( embeddableType.getSingularAttributes() );
				attributeList.sort( Comparator.comparing( Attribute::getName ) );
				// idName is necessary: embedded ID's properties cannot be resolved from EntityType itself
				attributeList.forEach( attr -> criteria.addOrder( Order.asc( idName + "." + attr.getName() ) ) );
				criteria.setProjection( Projections.id() );
			}
			else {
				// TODO Find @Id
				criteria.setProjection( Projections.alias( Projections.id(), "aliasedId" ) );
				criteria.addOrder( Order.asc( "aliasedId" ) );
			}
		}
		else {
			List<SingularAttribute<? super X, ?>> attributeList = new ArrayList<>( entityType.getIdClassAttributes() );
			attributeList.sort( Comparator.comparing( Attribute::getName ) );
			attributeList.forEach( attr -> criteria.addOrder( Order.asc( attr.getName() ) ) );
			criteria.setProjection( Projections.id() );
		}
		return criteria;
	}

	public static <X> Criteria createCriteria(
			EntityManagerFactory entityManagerFactory,
			StatelessSession statelessSession,
			Class<X> entity) {
		Criteria criteria = statelessSession.createCriteria( entity );
		EntityType<X> entityType = entityManagerFactory.getMetamodel().entity( entity );

		if ( entityType.hasSingleIdAttribute() ) {
			Type<?> idType = entityType.getIdType();
			Class<?> idJavaType = idType.getJavaType();
			String idName = entityType.getId( idJavaType ).getName();

			if ( idType.getPersistenceType() == Type.PersistenceType.EMBEDDABLE ) {
				// HQL with ID sorting:
				// TODO The WHERE clause need to be changed
//				Hibernate:
//				select
//					this_.day as y0_,
//					this_.month as y1_,
//					this_.year as y2_
//				from
//					EntityWithNonComparableId this_
//				where
//					(
//						this_.day>=?
//						and this_.month>=?
//						and this_.year>=?
//					)
//				order by
//					this_.day asc,
//					this_.month asc,
//					this_.year asc

				List<SingularAttribute<?, ?>> attributeList;
				EmbeddableType<?> embeddableType = entityManagerFactory.getMetamodel().embeddable( idJavaType );
				attributeList = new ArrayList<>( embeddableType.getSingularAttributes() );
				attributeList.sort( Comparator.comparing( Attribute::getName ) );
				// idName is necessary: embedded ID's properties cannot be resolved from EntityType itself
				attributeList.forEach( attr -> criteria.addOrder( Order.asc( idName + "." + attr.getName() ) ) );
			}
			else {
				SingularAttribute<?, ?> idAttribute = entityType.getId( idJavaType );
				criteria.addOrder( Order.asc( idAttribute.getName() ) );
			}
		}
		else {
			List<SingularAttribute<? super X, ?>> attributeList = new ArrayList<>( entityType.getIdClassAttributes() );
			attributeList.sort( Comparator.comparing( Attribute::getName ) );
			attributeList.forEach( attr -> criteria.addOrder( Order.asc( attr.getName() ) ) );
		}
		return criteria;
	}

}
