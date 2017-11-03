/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.integration.spring.massindexing.singleemf;

import java.io.IOException;
import java.text.ParseException;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.inject.Inject;

import org.hibernate.criterion.Restrictions;
import org.hibernate.search.jsr352.massindexing.MassIndexingJob;
import org.hibernate.search.jsr352.test.util.JobTestUtil;
import org.hibernate.search.test.integration.spring.massindexing.model.IndexedEntity;
import org.hibernate.search.test.integration.spring.massindexing.util.JobInterruptorUtil;

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
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.assertEquals;


@RunWith(BMUnitRunner.class)
@SpringBootTest(classes = SingleEntityManagerFactoryConfiguration.class)
// Use @DirtiesContext to reinitialize the database (thanks to hbm2ddl.auto) between test methods
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("jsr352")
@BMScript("jsr352/JobInterruptor.btm")
public class RestartIT {

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

		// The 1st execution. Keep it alive and wait Byteman to stop it
		JobInterruptorUtil.enable();
		long execId1 = jobOperator.start(
				MassIndexingJob.NAME,
				MassIndexingJob.parameters()
						.forEntity( IndexedEntity.class )
						.build()
				);
		JobExecution jobExec1 = jobOperator.getJobExecution( execId1 );
		jobExec1 = JobTestUtil.waitForTermination( jobOperator, jobExec1, JOB_TIMEOUT_MS );
		JobInterruptorUtil.disable();

		// Restart the job. This is the 2nd execution.
		long execId2 = jobOperator.restart( execId1, null );
		JobExecution jobExec2 = jobOperator.getJobExecution( execId2 );
		jobExec2 = JobTestUtil.waitForTermination( jobOperator, jobExec2, JOB_TIMEOUT_MS );

		assertEquals( BatchStatus.COMPLETED, jobExec2.getBatchStatus() );
		assertEquals( ENTITY_COUNT, dao.countUsingHibernateSearch() );
		assertEquals( 1, dao.search( "name" + (ENTITY_COUNT-1) ).size() );
	}

	@Test
	public void testJob_usingCriteria() throws InterruptedException, IOException, ParseException {
		assertEquals( 0, dao.countUsingHibernateSearch() );
		assertEquals( 0, dao.search( "name" + (ENTITY_COUNT-1) ).size() );


		JobOperator jobOperator = BatchRuntime.getJobOperator();

		// The 1st execution. Keep it alive and wait Byteman to stop it
		JobInterruptorUtil.enable();
		long execId1 = jobOperator.start(
				MassIndexingJob.NAME,
				MassIndexingJob.parameters()
						.forEntity( IndexedEntity.class )
						// Not an actual restriction, we just want to test restarts in Criteria mode
						.restrictedBy( Restrictions.ge( "id", 0 ) )
						.build()
				);
		JobExecution jobExec1 = jobOperator.getJobExecution( execId1 );
		jobExec1 = JobTestUtil.waitForTermination( jobOperator, jobExec1, JOB_TIMEOUT_MS );
		JobInterruptorUtil.disable();

		// Restart the job. This is the 2nd execution.
		long execId2 = jobOperator.restart( execId1, null );
		JobExecution jobExec2 = jobOperator.getJobExecution( execId2 );
		jobExec2 = JobTestUtil.waitForTermination( jobOperator, jobExec2, JOB_TIMEOUT_MS );

		assertEquals( BatchStatus.COMPLETED, jobExec2.getBatchStatus() );
		assertEquals( ENTITY_COUNT, dao.countUsingHibernateSearch() );
		assertEquals( 1, dao.search( "name" + (ENTITY_COUNT-1) ).size() );
	}

	@Test
	public void testJob_usingHQL() throws Exception {
		assertEquals( 0, dao.countUsingHibernateSearch() );
		assertEquals( 0, dao.search( "name" + (ENTITY_COUNT-1) ).size() );

		JobOperator jobOperator = BatchRuntime.getJobOperator();

		JobInterruptorUtil.enable();
		long execId1 = jobOperator.start(
				MassIndexingJob.NAME,
				MassIndexingJob.parameters()
						.forEntity( IndexedEntity.class )
						// Not an actual restriction, we just want to test restarts in HQL mode
						.restrictedBy( "select e from IndexedEntity e where id >= 0" )
						.build()
				);
		JobExecution jobExec1 = BatchRuntime.getJobOperator().getJobExecution( execId1 );
		jobExec1 = JobTestUtil.waitForTermination( jobOperator, jobExec1, JOB_TIMEOUT_MS );
		JobInterruptorUtil.disable();

		assertEquals( BatchStatus.FAILED, jobExec1.getBatchStatus() );

		// Restart the job.
		long execId2 = jobOperator.restart( execId1, null );
		JobExecution jobExec2 = jobOperator.getJobExecution( execId2 );
		JobTestUtil.waitForTermination( jobOperator, jobExec2, JOB_TIMEOUT_MS );

		assertEquals( BatchStatus.COMPLETED, jobExec2.getBatchStatus() );
		assertEquals( ENTITY_COUNT, dao.countUsingHibernateSearch() );
		assertEquals( 1, dao.search( "name" + (ENTITY_COUNT-1) ).size() );
	}

}
