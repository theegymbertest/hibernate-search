/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.integration.spring.massindexing.multipleemf;

import java.io.IOException;
import java.text.ParseException;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.inject.Inject;

import org.hibernate.search.jsr352.massindexing.MassIndexingJob;
import org.hibernate.search.jsr352.test.util.JobTestUtil;
import org.hibernate.search.test.integration.spring.massindexing.model.IndexedEntity;
import org.hibernate.search.test.integration.spring.massindexing.singleemf.SingleEntityManagerFactoryIndexedEntityDao;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.assertEquals;

/**
 * Test the behavior when there are multiple entity manager factories (persistence units),
 * but those are correctly registered as CDI beans.
 *
 * @author Mincong Huang
 */
@RunWith(BMUnitRunner.class)
@SpringBootTest(classes = MultipleEntityManagerFactoriesConfiguration.class)
// Use @DirtiesContext to reinitialize the database (thanks to hbm2ddl.auto) between test methods
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("jsr352")
@BMScript("jsr352/JobInterruptor.btm")
public class MultipleEntityManagerFactoriesIT {

	private static final String ENTITY_MANAGER_FACTORY_BEAN_NAME = MultipleEntityManagerFactoriesConfiguration.PRIMARY_ENTITY_MANAGER_FACTORY_BEAN_NAME;

	private static final int JOB_TIMEOUT_MS = 40_000;

	private static final int ENTITY_COUNT = 600; // Failure at entity #500

	// The two rules below replace the SpringRunner that we cannot use because we need the BMUnitRunner
	@ClassRule
	public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
	@Rule
	public final SpringMethodRule springMethodRule = new SpringMethodRule();

	@Inject
	private SingleEntityManagerFactoryIndexedEntityDao dao;

	@Before
	public void insertData() throws ParseException {
		for ( int i = 0; i < ENTITY_COUNT; ++i ) {
			dao.create( new IndexedEntity( i, "name" + i ) );
		}
	}

	@Test
	public void testJob() throws InterruptedException, IOException, ParseException {
		assertEquals( 0, dao.countUsingHibernateSearch() );
		assertEquals( 0, dao.search( "name" + (ENTITY_COUNT-1) ).size() );

		JobOperator jobOperator = BatchRuntime.getJobOperator();

		long execId1 = jobOperator.start(
				MassIndexingJob.NAME,
				MassIndexingJob.parameters()
						.forEntity( IndexedEntity.class )
						.entityManagerFactoryReference( ENTITY_MANAGER_FACTORY_BEAN_NAME )
						.build()
				);
		JobExecution jobExec1 = jobOperator.getJobExecution( execId1 );
		JobTestUtil.waitForTermination( jobOperator, jobExec1, JOB_TIMEOUT_MS );

		assertEquals( BatchStatus.COMPLETED, jobExec1.getBatchStatus() );
		assertEquals( ENTITY_COUNT, dao.countUsingHibernateSearch() );
		assertEquals( 1, dao.search( "name" + (ENTITY_COUNT-1) ).size() );
	}

}
