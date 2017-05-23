/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.elasticsearch.test;

import static org.fest.assertions.Assertions.assertThat;

import java.io.Serializable;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FullTextFilterDef;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.backend.spi.Work;
import org.hibernate.search.backend.spi.WorkType;
import org.hibernate.search.elasticsearch.cfg.ElasticsearchEnvironment;
import org.hibernate.search.elasticsearch.cfg.IndexSchemaManagementStrategy;
import org.hibernate.search.filter.FullTextFilterImplementor;
import org.hibernate.search.filter.ShardSensitiveOnlyFilter;
import org.hibernate.search.query.engine.spi.HSQuery;
import org.hibernate.search.spi.BuildContext;
import org.hibernate.search.store.ShardIdentifierProviderTemplate;
import org.hibernate.search.testsupport.TestForIssue;
import org.hibernate.search.testsupport.junit.SearchFactoryHolder;
import org.hibernate.search.testsupport.setup.TransactionContextForTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Yoann Rodiere
 */
public class ElasticsearchMultipleApplicationsIT {

	private static final String SHARD_1_IDENTIFIER = "shard1";

	private static final String SHARD_2_IDENTIFIER = "shard2";

	private SearchFactoryHolder factoryHolder1 = new SearchFactoryHolder( DynamicShardingEntity.class )
			.withProperty( "hibernate.search." + DynamicShardingEntity.INDEX_NAME + ".sharding_strategy",
					CustomShardIdentifierProvider.class.getName() );

	private SearchFactoryHolder factoryHolder2 = new SearchFactoryHolder( DynamicShardingEntity.class )
			// We need at least that in order for dynamic sharding to work properly
			.withProperty( "hibernate.search.default." + ElasticsearchEnvironment.INDEX_SCHEMA_MANAGEMENT_STRATEGY,
					IndexSchemaManagementStrategy.UPDATE.getExternalName() )
			.withProperty( "hibernate.search." + DynamicShardingEntity.INDEX_NAME + ".sharding_strategy",
					CustomShardIdentifierProvider.class.getName() );

	@Rule
	public RuleChain chain = RuleChain.outerRule( factoryHolder1 )
			.around( factoryHolder2 );

	@Test
	@TestForIssue(jiraKey = "HSEARCH-2725")
	public void dynamicallyCreatedShardsVisibleFromEverySearchFactory() {
		assertCounts( factoryHolder1, 0, 0 );
		assertCounts( factoryHolder2, 0, 0 );

		insert( factoryHolder2, new DynamicShardingEntity( 1, SHARD_1_IDENTIFIER ) );

		assertCounts( factoryHolder1, 1, 0 );
		assertCounts( factoryHolder2, 1, 0 );

		insert( factoryHolder1, new DynamicShardingEntity( 2, SHARD_2_IDENTIFIER ) );

		assertCounts( factoryHolder1, 1, 1 );
		assertCounts( factoryHolder2, 1, 1 );

		insert( factoryHolder2, new DynamicShardingEntity( 3, SHARD_1_IDENTIFIER ) );

		assertCounts( factoryHolder1, 2, 1 );
		assertCounts( factoryHolder2, 2, 1 );
	}

	private void assertCounts(SearchFactoryHolder factoryHolder, int expectedShard1Count, int expectedShard2Count) {
		/*
		 * Do the global search first, first performing searches on specific shards may trigger
		 * the creation of index managers and thus prevent HSEARCH-2725 to show.
		 */
		assertGlobalCount( factoryHolder, expectedShard1Count + expectedShard2Count );

		assertCountOnShard( factoryHolder, SHARD_1_IDENTIFIER, expectedShard1Count );
		assertCountOnShard( factoryHolder, SHARD_2_IDENTIFIER, expectedShard2Count );
	}

	private void assertGlobalCount(SearchFactoryHolder factoryHolder, int expectedCount) {
		int factoryIdentifier = getFactoryIdentifier( factoryHolder );
		assertThat( countAll( factoryHolder1 ) )
				.as( "Total number of documents for factory " + factoryIdentifier )
				.isEqualTo( expectedCount );
	}

	private void assertCountOnShard(SearchFactoryHolder factoryHolder, String shardIdentifier, int expectedCount) {
		int factoryIdentifier = getFactoryIdentifier( factoryHolder );
		assertThat( countOnShard( factoryHolder1, shardIdentifier ) )
				.as( "Documents on shard '" + shardIdentifier + "' for factory " + factoryIdentifier )
				.isEqualTo( expectedCount );
	}

	private int getFactoryIdentifier(SearchFactoryHolder factoryHolder) {
		return factoryHolder == factoryHolder1 ? 1 : 2;
	}

	private int countOnShard(SearchFactoryHolder factoryHolder, String shardIdentifier) {
		HSQuery query = factoryHolder.getSearchFactory().createHSQuery( new MatchAllDocsQuery(), DynamicShardingEntity.class );
		query.enableFullTextFilter( "shard_filter" ).setParameter( "shardIdentifier", shardIdentifier );
		return query.queryResultSize();
	}

	private int countAll(SearchFactoryHolder factoryHolder) {
		HSQuery query = factoryHolder.getSearchFactory().createHSQuery( new MatchAllDocsQuery(), DynamicShardingEntity.class );
		return query.queryResultSize();
	}

	private void insert(SearchFactoryHolder factoryHolder, DynamicShardingEntity entity) {
		Work work = new Work( entity, entity.id, WorkType.ADD, false );
		TransactionContextForTest tc = new TransactionContextForTest();
		factoryHolder.getSearchFactory().getWorker().performWork( work, tc );
		tc.end();
	}

	@Indexed(index = DynamicShardingEntity.INDEX_NAME)
	@FullTextFilterDef(
			name = "shard_filter",
			impl = ShardSensitiveOnlyFilter.class
	)
	private static class DynamicShardingEntity {
		public static final String INDEX_NAME = "DynamicShardingEntity";

		@DocumentId
		private Integer id;

		@Field(analyze = Analyze.NO)
		private String shardIdentifier;

		public DynamicShardingEntity(Integer id, String shardIdentifier) {
			super();
			this.id = id;
			this.shardIdentifier = shardIdentifier;
		}
	}

	public static class CustomShardIdentifierProvider extends ShardIdentifierProviderTemplate {

		@Override
		public String getShardIdentifier(Class<?> entityType, Serializable id, String idAsString, Document document) {
			if ( entityType.equals( DynamicShardingEntity.class ) ) {
				final String typeValue = document.getField( "shardIdentifier" ).stringValue();
				addShard( typeValue );
				return typeValue;
			}
			throw new RuntimeException( "DynamicShardingEntity expected but found " + entityType );
		}

		@Override
		public Set<String> getShardIdentifiersForQuery(FullTextFilterImplementor[] fullTextFilters) {
			for ( FullTextFilterImplementor ftf : fullTextFilters ) {
				if ( "shard_filter".equals( ftf.getName() ) ) {
					String shardId = (String) ftf.getParameter( "shardIdentifier" );
					return Collections.singleton( shardId );
				}
			}
			return getAllShardIdentifiers();
		}

		@Override
		protected Set<String> loadInitialShardNames(Properties properties, BuildContext buildContext) {
			// Not supported
			return Collections.emptySet();
		}
	}
}
