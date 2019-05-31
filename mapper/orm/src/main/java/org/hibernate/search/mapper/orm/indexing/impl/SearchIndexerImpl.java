/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.orm.indexing.impl;

import java.util.concurrent.CompletableFuture;

import org.hibernate.search.mapper.orm.indexing.SearchIndexer;
import org.hibernate.search.mapper.pojo.work.spi.PojoScopeWorkExecutor;

public class SearchIndexerImpl implements SearchIndexer {
	private final PojoScopeWorkExecutor scopeWorkExecutor;

	public SearchIndexerImpl(PojoScopeWorkExecutor scopeWorkExecutor) {
		this.scopeWorkExecutor = scopeWorkExecutor;
	}

	@Override
	public CompletableFuture<?> optimize() {
		return scopeWorkExecutor.optimize();
	}

	@Override
	public CompletableFuture<?> purge() {
		return scopeWorkExecutor.purge();
	}

	@Override
	public CompletableFuture<?> flush() {
		return scopeWorkExecutor.flush();
	}
}
