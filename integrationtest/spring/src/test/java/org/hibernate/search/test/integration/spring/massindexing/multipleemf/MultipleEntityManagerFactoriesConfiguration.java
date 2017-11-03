/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.integration.spring.massindexing.multipleemf;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.hibernate.search.test.integration.spring.massindexing.model.IndexedEntity;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/**
 * @author Yoann Rodiere
 */
@Configuration
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = JtaAutoConfiguration.class)
@ComponentScan
@EntityScan(basePackageClasses = IndexedEntity.class)
public class MultipleEntityManagerFactoriesConfiguration {

	public static final String PRIMARY_PERSISTENCE_UNIT_NAME = "primary_pu";
	public static final String UNUSED_PERSISTENCE_UNIT_NAME = "unused_pu";

	public static final String PRIMARY_ENTITY_MANAGER_FACTORY_BEAN_NAME = "primary_emf_bean";
	private static final String UNUSED_ENTITY_MANAGER_FACTORY_BEAN_NAME = "unused_emf_bean";

	@PersistenceUnit(unitName = PRIMARY_PERSISTENCE_UNIT_NAME)
	private EntityManagerFactory h2PersistenceUnit;

	@PersistenceUnit(unitName = UNUSED_PERSISTENCE_UNIT_NAME)
	private EntityManagerFactory unusedPersistenceUnit;

	@Bean
	@Singleton
	@Named(PRIMARY_ENTITY_MANAGER_FACTORY_BEAN_NAME)
	public EntityManagerFactory createH2PersistenceUnit() {
		return h2PersistenceUnit;
	}

	@Bean
	@Singleton
	@Named(UNUSED_ENTITY_MANAGER_FACTORY_BEAN_NAME)
	public EntityManagerFactory createUnusedPersistenceUnit() {
		return unusedPersistenceUnit;
	}

}
