/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.orm.realbackend.panache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.search.util.impl.integrationtest.mapper.orm.OrmUtils.withinJPATransaction;

import java.util.List;
import javax.persistence.EntityManagerFactory;

import org.hibernate.search.engine.cfg.BackendSettings;
import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api.Page;
import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api.PanacheQuery;
import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.impl.PanacheElasticsearchSupport;
import org.hibernate.search.mapper.orm.automaticindexing.session.AutomaticIndexingSynchronizationStrategyNames;
import org.hibernate.search.mapper.orm.cfg.HibernateOrmMapperSettings;
import org.hibernate.search.mapper.orm.common.EntityReference;
import org.hibernate.search.util.impl.integrationtest.backend.elasticsearch.ElasticsearchBackendConfiguration;
import org.hibernate.search.util.impl.integrationtest.mapper.orm.OrmSetupHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class PanacheDslIT {

	@Rule
	public OrmSetupHelper setupHelper = OrmSetupHelper.withSingleBackend( new ElasticsearchBackendConfiguration() );

	private EntityManagerFactory entityManagerFactory;

	@Before
	public void setup() {
		entityManagerFactory = setupHelper.start()
				.withBackendProperty( BackendSettings.TYPE, "elasticsearch" )
				.withProperty( HibernateOrmMapperSettings.AUTOMATIC_INDEXING_SYNCHRONIZATION_STRATEGY,
						AutomaticIndexingSynchronizationStrategyNames.SYNC )
				.setup( Book.class );
		PanacheElasticsearchSupport.currentEntityManagerFactory = entityManagerFactory;
	}

	@Test
	public void dsl() {
		withinJPATransaction( entityManagerFactory, entityManager -> {
			Book book1 = new Book();
			book1.setId( 1 );
			book1.setTitle( "I, Robot" );
			book1.setGenre( Genre.SCIENCE_FICTION );
			entityManager.persist( book1 );
			Book book2 = new Book();
			book2.setId( 2 );
			book2.setTitle( "The Caves of Steel" );
			book2.setGenre( Genre.CRIME_FICTION );
			entityManager.persist( book1 );
		} );

		withinJPATransaction( entityManagerFactory, entityManager -> {
			PanacheElasticsearchSupport.currentEntityManager = entityManager;
			PanacheQuery<Book> query = Book.search()
					.where( f -> f.match().field( "title" ).matching( "robot" ) )
					.toQuery();
			List<Book> hits = query.page( Page.of( 0, 20 ) ).list();
			assertThat( hits ).extracting( Book::getId ).containsExactly( 1 );
		} );

		withinJPATransaction( entityManagerFactory, entityManager -> {
			PanacheElasticsearchSupport.currentEntityManager = entityManager;
			PanacheQuery<EntityReference> query = Book.search()
					.select( f -> f.entityReference() )
					.where( f -> f.match().field( "title" ).matching( "robot" ) )
					.toQuery();
			List<EntityReference> hits = query.page( Page.of( 0, 20 ) ).list();
			assertThat( hits ).extracting( EntityReference::id ).containsExactly( 1 );
		} );
	}

}
