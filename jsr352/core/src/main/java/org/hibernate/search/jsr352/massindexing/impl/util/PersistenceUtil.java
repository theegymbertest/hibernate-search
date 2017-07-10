/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.jsr352.massindexing.impl.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.search.util.StringHelper;

/**
 * Internal utility class for persistence usage.
 *
 * @author Mincong Huang
 */
public final class PersistenceUtil {

	public enum IdRestriction {
		GE {
			@Override
			public <X> Criterion generate(SingularAttribute<X, ?>[] idAttributes, Object idObj, String prefix)
					throws Exception {
				Conjunction[] or = new Conjunction[idAttributes.length];
				prefix = prefix != null ? prefix : "";

				for ( int i = 0; i < or.length; i++ ) {
					// Group expressions together in a single conjunction (A and B and C...).
					SimpleExpression[] and = new SimpleExpression[i + 1];
					int j = 0;
					for ( ; j < and.length - 1; j++ ) {
						// The first N-1 expressions have symbol `=`
						String key = idAttributes[j].getName();
						Object val = getProperty( idObj, key );
						and[j] = Restrictions.eq( prefix + key, val );
					}
					// The last expression has symbol `>=`
					String key = idAttributes[j].getName();
					Object val = getProperty( idObj, key );
					and[j] = Restrictions.ge( prefix + key, val );

					or[i] = Restrictions.conjunction( and );
				}
				// Group the disjunction of multiple expressions (X or Y or Z...).
				return Restrictions.or( or );
			}
		},
		LT {
			@Override
			public <X> Criterion generate(SingularAttribute<X, ?>[] idAttributes, Object idObj, String prefix)
					throws Exception {
				Conjunction[] or = new Conjunction[idAttributes.length];
				prefix = prefix != null ? prefix : "";

				for ( int i = 0; i < or.length; i++ ) {
					// Group expressions together in a single conjunction (A and B and C...).
					SimpleExpression[] and = new SimpleExpression[i + 1];
					int j = 0;
					for ( ; j < and.length - 1; j++ ) {
						// The first N-1 expressions have symbol `=`
						String key = idAttributes[j].getName();
						Object val = getProperty( idObj, key );
						and[j] = Restrictions.eq( prefix + key, val );
					}
					// The last expression has symbol `<`
					String key = idAttributes[j].getName();
					Object val = getProperty( idObj, key );
					and[j] = Restrictions.lt( prefix + key, val );

					or[i] = Restrictions.conjunction( and );
				}
				// Group the disjunction of multiple expressions (X or Y or Z...).
				return Restrictions.or( or );
			}
		};

		public abstract <X> Criterion generate(SingularAttribute<X, ?>[] idAttributes, Object idObj, String prefix)
				throws Exception;
	}

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
	 *
	 * @param entityManagerFactory
	 * @param statelessSession
	 * @param entity
	 * @param <X>
	 *
	 * @return
	 */
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
				List<SingularAttribute<?, ?>> attributeList;
				EmbeddableType<?> embeddableType = entityManagerFactory.getMetamodel().embeddable( idJavaType );
				attributeList = new ArrayList<>( embeddableType.getSingularAttributes() );
				attributeList.sort( Comparator.comparing( Attribute::getName ) );
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

	public static List<Criterion> createCriterionList(EntityManagerFactory entityManagerFactory, PartitionBound partitionBound)
			throws Exception {
		Class<?> entity = partitionBound.getEntityType();
		List<Criterion> result = new ArrayList<>();

		if ( partitionBound.hasUpperBound() ) {
			result.add( getCriteriaFromId( entityManagerFactory, entity, partitionBound.getUpperBound(), IdRestriction.LT ) );
		}
		if ( partitionBound.hasLowerBound() ) {
			result.add( getCriteriaFromId( entityManagerFactory, entity, partitionBound.getLowerBound(), IdRestriction.GE ) );
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	// TODO Use PartitionBound as 3rd input argument instead of {Object + IdRestriction}
	public static <X> Criterion getCriteriaFromId(
			EntityManagerFactory emf,
			Class<X> entity,
			Object idObj,
			PersistenceUtil.IdRestriction idRestriction) throws Exception {
		EntityType<X> entityType = emf.getMetamodel().entity( entity );

		if ( entityType.hasSingleIdAttribute() ) {
			Type<?> idType = entityType.getIdType();
			Class<?> idJavaType = idType.getJavaType();
			String idName = entityType.getId( idJavaType ).getName();

			if ( idType.getPersistenceType() == Type.PersistenceType.EMBEDDABLE ) {
				List<SingularAttribute<?, ?>> attributeList;
				EmbeddableType<?> embeddableType = emf.getMetamodel().embeddable( idJavaType );
				attributeList = new ArrayList<>( embeddableType.getSingularAttributes() );
				attributeList.sort( Comparator.comparing( Attribute::getName ) );
				return idRestriction.generate( attributeList.toArray( new SingularAttribute[0] ), idObj, idName + "." );
			}
			else {
				switch ( idRestriction ) {
					case LT:
						return Restrictions.lt( idName, idObj );
					case GE:
						return Restrictions.ge( idName, idObj );
					default:
						throw new UnsupportedOperationException( "bla bla bla" );
				}
			}
		}
		else {
			List<SingularAttribute<? super X, ?>> attributeList = new ArrayList<>( entityType.getIdClassAttributes() );
			attributeList.sort( Comparator.comparing( Attribute::getName ) );
			return idRestriction.generate( attributeList.toArray( new SingularAttribute[0] ), idObj, null );
		}
	}

	private static Object getProperty(Object obj, String propertyName)
			throws IntrospectionException, InvocationTargetException, IllegalAccessException {
		return new PropertyDescriptor( propertyName, obj.getClass() ).getReadMethod().invoke( obj );
	}

}
