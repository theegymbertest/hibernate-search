/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.indexes.impl;

import java.util.Properties;

import org.hibernate.search.indexes.spi.IndexManager;
import org.hibernate.search.spi.WorkerBuildContext;
import org.hibernate.search.store.IndexShardingStrategy;
import org.hibernate.search.util.impl.ClassLoaderHelper;


/**
 * @author Yoann Rodiere
 */
@SuppressWarnings("deprecation")
public class NonDynamicShardingStrategyFactory implements IndexShardingStrategyFactory {

	private final Class<? extends IndexShardingStrategy> shardingStrategyClass;
	private final Properties properties;

	public NonDynamicShardingStrategyFactory(Class<? extends IndexShardingStrategy> shardingStrategyClass, Properties properties) {
		this.shardingStrategyClass = shardingStrategyClass;
		this.properties = properties;
	}

	@Override
	public IndexShardingStrategy create(IndexManagerGroupHolder holder, Class<?> entityType, WorkerBuildContext buildContext) {
		IndexShardingStrategy shardingStrategy = ClassLoaderHelper.instanceFromClass(
				IndexShardingStrategy.class,
				shardingStrategyClass,
				"IndexShardingStrategy"
		);

		IndexManager[] indexManagers = holder.preInitializeIndexManagers( entityType, buildContext );
		shardingStrategy.initialize( properties, indexManagers );

		return shardingStrategy;
	}

}
