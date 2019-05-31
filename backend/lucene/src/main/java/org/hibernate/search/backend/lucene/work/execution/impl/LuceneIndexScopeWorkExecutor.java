/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.work.execution.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.hibernate.search.backend.lucene.work.impl.LuceneWorkFactory;
import org.hibernate.search.engine.backend.work.execution.spi.IndexScopeWorkExecutor;
import org.hibernate.search.engine.mapper.session.context.spi.DetachedSessionContextImplementor;

public class LuceneIndexScopeWorkExecutor implements IndexScopeWorkExecutor {

	private final LuceneWorkFactory factory;
	private final List<LuceneIndexWorkExecutor> indexWorkExecutors;
	private final DetachedSessionContextImplementor sessionContext;

	public LuceneIndexScopeWorkExecutor(LuceneWorkFactory factory,
			List<LuceneIndexWorkExecutor> indexWorkExecutors,
			DetachedSessionContextImplementor sessionContext) {
		this.factory = factory;
		this.indexWorkExecutors = indexWorkExecutors;
		this.sessionContext = sessionContext;
	}

	@Override
	public CompletableFuture<?> optimize() {
		CompletableFuture<?>[] futures = new CompletableFuture[indexWorkExecutors.size()];
		int i = 0;
		for ( LuceneIndexWorkExecutor indexExecutor : indexWorkExecutors ) {
			futures[i] = indexExecutor.submit(
					factory.optimize( indexExecutor.getIndexName() )
			);
			++i;
		}
		return CompletableFuture.allOf( futures );
	}

	@Override
	public CompletableFuture<?> purge() {
		CompletableFuture<?>[] futures = new CompletableFuture[indexWorkExecutors.size()];
		int i = 0;
		for ( LuceneIndexWorkExecutor indexExecutor : indexWorkExecutors ) {
			futures[i] = indexExecutor.submit(
					factory.deleteAll( indexExecutor.getIndexName(), sessionContext.getTenantIdentifier() )
			);
			++i;
		}
		return CompletableFuture.allOf( futures );
	}

	@Override
	public CompletableFuture<?> flush() {
		CompletableFuture<?>[] futures = new CompletableFuture[indexWorkExecutors.size()];
		int i = 0;
		for ( LuceneIndexWorkExecutor indexExecutor : indexWorkExecutors ) {
			futures[i] = indexExecutor.submit(
					factory.flush( indexExecutor.getIndexName() )
			);
			++i;
		}
		return CompletableFuture.allOf( futures );
	}
}
