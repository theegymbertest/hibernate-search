/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.integration.spring.massindexing.singleemf;

import org.hibernate.search.test.integration.spring.massindexing.model.IndexedEntity;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
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
@EnableBatchProcessing
public class SingleEntityManagerFactoryConfiguration {

}
