/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.indexes.impl;

import java.util.Properties;

import org.hibernate.search.engine.impl.DynamicShardingStrategy;
import org.hibernate.search.spi.WorkerBuildContext;
import org.hibernate.search.store.IndexShardingStrategy;
import org.hibernate.search.store.ShardIdentifierProvider;
import org.hibernate.search.util.impl.ClassLoaderHelper;


/**
 * @author Yoann Rodiere
 */
@SuppressWarnings("deprecation")
public class DynamicShardingStrategyFactory implements IndexShardingStrategyFactory {

	private final Class<?> shardIdentifierProviderClass;
	private final Properties properties;

	public DynamicShardingStrategyFactory(Class<?> shardIdentifierProviderClass, Properties properties) {
		this.shardIdentifierProviderClass = shardIdentifierProviderClass;
		this.properties = properties;
	}

	@Override
	public IndexShardingStrategy create(IndexManagerGroupHolder holder, Class<?> entityType, WorkerBuildContext buildContext) {
		ShardIdentifierProvider shardIdentifierProvider = createShardIdentifierProvider(
				buildContext, properties
		);
		return new DynamicShardingStrategy( shardIdentifierProvider, holder, entityType );
	}

	private ShardIdentifierProvider createShardIdentifierProvider(WorkerBuildContext buildContext, Properties indexProperty) {
		ShardIdentifierProvider shardIdentifierProvider = ClassLoaderHelper.instanceFromClass(
				ShardIdentifierProvider.class,
				shardIdentifierProviderClass,
				"ShardIdentifierProvider"
		);

		shardIdentifierProvider.initialize( properties, buildContext );

		return shardIdentifierProvider;
	}

}
