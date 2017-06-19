/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.integration.wildfly.tika;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.search.test.integration.VersionTestHelper.getHibernateORMModuleName;
import static org.hibernate.search.test.integration.VersionTestHelper.getWildFlyModuleIdentifier;

import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;

import org.hibernate.search.test.integration.VersionTestHelper;
import org.hibernate.search.test.integration.wildfly.PackagerHelper;
import org.hibernate.search.test.integration.wildfly.model.Member;
import org.hibernate.search.test.integration.wildfly.tika.model.EntityWithTikaBridge;
import org.hibernate.search.test.integration.wildfly.tika.model.EntityWithTikaBridgeDao;
import org.hibernate.search.test.integration.wildfly.tika.parser.CustomParser;
import org.hibernate.search.test.util.impl.ClasspathResourceAsFile;
import org.hibernate.search.testsupport.TestForIssue;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.persistence20.PersistenceDescriptor;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Yoann Rodiere
 */
@RunWith(Arquillian.class)
@TestForIssue(jiraKey = "HSEARCH-1899")
public class TikaBridgeIT {

	private static final String TEST_FILE_PATH = "/org/hibernate/search/test/bridge/tika/mysong.mp3";

	@Rule
	public ClasspathResourceAsFile testFile = new ClasspathResourceAsFile( getClass(), TEST_FILE_PATH );

	@Deployment
	public static Archive<?> createTestArchive() throws Exception {
		WebArchive archive = ShrinkWrap
				.create( WebArchive.class, TikaBridgeIT.class.getSimpleName() + ".war" )
				.addPackages( true /* recursive */, TikaBridgeIT.class.getPackage() )
				.addAsResource( persistenceXml(), "META-INF/persistence.xml" )
				.addAsWebInfResource( "jboss-deployment-structure-hcann-tika.xml", "/jboss-deployment-structure.xml" )
				.addAsLibraries( PackagerHelper.hibernateSearchTestingLibraries() )
				.addAsLibraries( Maven.resolver()
						.resolve( "org.apache.tika:tika-core:" + VersionTestHelper.getDependencyVersionTika() )
						.withoutTransitivity()
						.as( JavaArchive.class )
				)
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
		return archive;
	}

	private static Asset persistenceXml() {
		String persistenceXml = Descriptors.create( PersistenceDescriptor.class )
			.version( "2.0" )
			.createPersistenceUnit()
				.name( "primary" )
				// The deployment Scanner is disabled as the JipiJapa integration is not available because of the custom Hibernate ORM module:
				.clazz( Member.class.getName() )
				.jtaDataSource( "java:jboss/datasources/ExampleDS" )
				.getOrCreateProperties()
					.createProperty().name( "hibernate.hbm2ddl.auto" ).value( "create-drop" ).up()
					.createProperty().name( "hibernate.search.default.lucene_version" ).value( "LUCENE_CURRENT" ).up()
					.createProperty().name( "hibernate.search.default.directory_provider" ).value( "local-heap" ).up()
					.createProperty().name( "wildfly.jpa.hibernate.search.module" ).value( getWildFlyModuleIdentifier() ).up()
					.createProperty().name( "jboss.as.jpa.providerModule" ).value( getHibernateORMModuleName() ).up()
				.up().up()
			.exportAsString();
		return new StringAsset( persistenceXml );
	}

	@Inject
	private EntityWithTikaBridgeDao dao;

	@After
	public void cleanupDatabase() {
		dao.deleteAll();
	}

	/**
	 * This test should only pass if both the custom detector and custom parser are enabled.
	 */
	@Test
	public void test() {
		Function<String, List<EntityWithTikaBridge>> search = dao::search;

		assertThat( search.apply( CustomParser.PARSED_CONTENT ) ).onProperty( "id" ).isEmpty();

		EntityWithTikaBridge entity = new EntityWithTikaBridge();
		entity.setContent( testFile.get().toURI() );
		dao.create( entity );
		assertThat( search.apply( CustomParser.PARSED_CONTENT ) ).onProperty( "id" ).containsOnly( entity.getId() );

		dao.delete( entity );
		assertThat( search.apply( CustomParser.PARSED_CONTENT ) ).onProperty( "id" ).isEmpty();
	}
}
