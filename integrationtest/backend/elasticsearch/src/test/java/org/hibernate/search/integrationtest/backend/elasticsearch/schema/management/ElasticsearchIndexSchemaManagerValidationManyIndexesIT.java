/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.backend.elasticsearch.schema.management;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.search.integrationtest.backend.elasticsearch.schema.management.ElasticsearchIndexSchemaManagerTestUtils.hasValidationFailureReport;
import static org.hibernate.search.integrationtest.backend.elasticsearch.schema.management.ElasticsearchIndexSchemaManagerTestUtils.simpleMappingForInitialization;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;
import org.hibernate.search.backend.elasticsearch.cfg.ElasticsearchBackendSettings;
import org.hibernate.search.backend.elasticsearch.cfg.ElasticsearchIndexSettings;
import org.hibernate.search.integrationtest.backend.tck.testsupport.util.rule.SearchSetupHelper;
import org.hibernate.search.util.common.SearchException;
import org.hibernate.search.util.common.impl.Futures;
import org.hibernate.search.util.impl.integrationtest.backend.elasticsearch.rule.TestElasticsearchClient;
import org.hibernate.search.util.impl.integrationtest.common.reporting.FailureReportChecker;
import org.hibernate.search.util.impl.integrationtest.mapper.stub.StubMappedIndex;
import org.hibernate.search.util.impl.integrationtest.mapper.stub.StubMappingSchemaManagementStrategy;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Test schema validation when the application uses a large number of indexes.
 */
@RunWith(Parameterized.class)
public class ElasticsearchIndexSchemaManagerValidationManyIndexesIT {

	@Parameterized.Parameters(name = "With operation {0}, request_timeout = {1}")
	public static List<Object[]> params() {
		List<Object[]> params = new ArrayList<>();
		for ( ElasticsearchIndexSchemaManagerValidationOperation operation : ElasticsearchIndexSchemaManagerValidationOperation.all() ) {
			params.add( new Object[] { operation, false } );
			params.add( new Object[] { operation, true } );
		}
		return params;
	}

	@Rule
	public final SearchSetupHelper setupHelper = new SearchSetupHelper();

	@Rule
	public TestElasticsearchClient elasticSearchClient = new TestElasticsearchClient();

	private final ElasticsearchIndexSchemaManagerValidationOperation operation;
	private final boolean requestTimeout;

	public ElasticsearchIndexSchemaManagerValidationManyIndexesIT(
			ElasticsearchIndexSchemaManagerValidationOperation operation, boolean requestTimeout) {
		this.operation = operation;
		this.requestTimeout = requestTimeout;
	}

	@Test
	public void success() {
		List<StubMappedIndex> indexes = createManyIndexes();

		setupAndValidate( indexes );

		// If we get here, it means validation passed (no exception was thrown)
	}

	@Test
	public void attribute_field_notPresent() {
		List<StubMappedIndex> indexes = createManyIndexes();

		StubMappedIndex failingIndex = StubMappedIndex.ofNonRetrievable(
						root -> root.field( "myField", f -> f.asInteger() ).toReference()
				)
				.name( "failing" );
		indexes.add( failingIndex );

		elasticSearchClient.index( failingIndex.name() ).deleteAndCreate();
		elasticSearchClient.index( failingIndex.name() ).type().putMapping(
				simpleMappingForInitialization(
						"'notMyField': {"
								+ "'type': 'integer',"
								+ "'index': true"
								+ "}"
				)
		);

		setupAndValidateExpectingFailure(
				indexes,
				hasValidationFailureReport()
						.indexFieldContext( "myField" )
						.failure( "Missing property mapping" )
		);
	}

	private List<StubMappedIndex> createManyIndexes() {
		List<StubMappedIndex> indexes = new ArrayList<>();
		for ( int i = 0; i < 42; i++ ) {
			StubMappedIndex index = StubMappedIndex.ofNonRetrievable( root -> {
						root.field(
										"myField",
										f -> f.asString().analyzer( "default" )
								)
								.toReference();
					} )
					.name( "idx" + i );
			indexes.add( index );
		}

		indexes.stream()
				.map( index -> ForkJoinPool.commonPool().submit( () -> {
					elasticSearchClient.index( index.name() ).deleteAndCreate();
					elasticSearchClient.index( index.name() ).type().putMapping(
							simpleMappingForInitialization(
									"'myField': {"
											+ "'type': 'text',"
											+ "'index': true,"
											+ "'analyzer': 'default'"
											+ "},"
											+ "'NOTmyField': {" // Ignored during validation
											+ "'type': 'text',"
											+ "'index': true"
											+ "}"
							)
					);
				} ) )
				.collect( Collectors.toList())
				.forEach( ForkJoinTask::join );

		return indexes;
	}

	private void setupAndValidateExpectingFailure(List<StubMappedIndex> indexes,
			FailureReportChecker failureReportChecker) {
		assertThatThrownBy( () -> setupAndValidate( indexes ) )
				.isInstanceOf( SearchException.class )
				.satisfies( failureReportChecker );
	}

	private void setupAndValidate(List<StubMappedIndex> indexes) {
		setupHelper.start()
				.withSchemaManagement( StubMappingSchemaManagementStrategy.DROP_ON_SHUTDOWN_ONLY )
				.withBackendProperty( ElasticsearchBackendSettings.REQUEST_TIMEOUT,
						requestTimeout ? 100 : null )
				.withBackendProperty(
						// Don't contribute any analysis definitions, migration of those is tested in another test class
						ElasticsearchIndexSettings.ANALYSIS_CONFIGURER,
						(ElasticsearchAnalysisConfigurer) (ElasticsearchAnalysisConfigurationContext context) -> {
							// No-op
						}
				)
				.withIndexes( indexes )
				.setup();
		Futures.unwrappedExceptionJoin( CompletableFuture.allOf(
				indexes.stream().map( index -> operation.apply( index.schemaManager() ) )
						.toArray( CompletableFuture[]::new )
		) );
	}
}
