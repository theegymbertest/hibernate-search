/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.indexes.impl;

import org.hibernate.search.spi.WorkerBuildContext;
import org.hibernate.search.store.IndexShardingStrategy;

/**
 * @author Yoann Rodiere
 */
@SuppressWarnings("deprecation")
public interface IndexShardingStrategyFactory {

	IndexShardingStrategy create(IndexManagerGroupHolder holder, Class<?> entityType, WorkerBuildContext buildContext);

}
