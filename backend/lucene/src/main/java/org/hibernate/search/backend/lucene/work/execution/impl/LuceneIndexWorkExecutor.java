/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.work.execution.impl;

import java.util.concurrent.CompletableFuture;

import org.hibernate.search.backend.lucene.orchestration.impl.LuceneWriteWorkOrchestrator;
import org.hibernate.search.backend.lucene.work.impl.LuceneWriteWork;
import org.hibernate.search.engine.backend.work.execution.DocumentCommitStrategy;
import org.hibernate.search.engine.backend.work.execution.DocumentRefreshStrategy;

public class LuceneIndexWorkExecutor {

	private final LuceneWriteWorkOrchestrator orchestrator;
	private final String indexName;

	public LuceneIndexWorkExecutor(LuceneWriteWorkOrchestrator orchestrator, String indexName) {
		this.orchestrator = orchestrator;
		this.indexName = indexName;
	}

	String getIndexName() {
		return indexName;
	}

	<T> CompletableFuture<T> submit(LuceneWriteWork<T> work) {
		return orchestrator.submit(
				work,
				DocumentCommitStrategy.FORCE,
				DocumentRefreshStrategy.NONE
		);
	}
}
