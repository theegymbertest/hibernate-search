/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.orm.indexing.impl;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.search.engine.mapper.session.context.spi.DetachedSessionContextImplementor;
import org.hibernate.search.mapper.orm.indexing.SearchIndexer;
import org.hibernate.search.mapper.orm.indexing.SearchIndexerReindexOptionsContext;
import org.hibernate.search.mapper.orm.massindexing.impl.MassIndexerImpl;
import org.hibernate.search.mapper.pojo.work.spi.PojoScopeWorkExecutor;

public class SearchIndexerImpl implements SearchIndexer {
	private final SessionFactoryImplementor sessionFactory;
	private final Set<? extends Class<?>> targetedIndexedTypes;
	private final DetachedSessionContextImplementor sessionContext;

	private final PojoScopeWorkExecutor scopeWorkExecutor;

	public SearchIndexerImpl(SessionFactoryImplementor sessionFactory,
			Set<? extends Class<?>> targetedIndexedTypes,
			DetachedSessionContextImplementor sessionContext,
			PojoScopeWorkExecutor scopeWorkExecutor) {
		this.sessionFactory = sessionFactory;
		this.targetedIndexedTypes = targetedIndexedTypes;
		this.sessionContext = sessionContext;
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

	@Override
	public CompletableFuture<?> reindex() {
		return createMassIndexer().start();
	}

	@Override
	public CompletableFuture<?> reindex(Consumer<SearchIndexerReindexOptionsContext> optionsContributor) {
		MassIndexerImpl massIndexer = createMassIndexer();
		optionsContributor.accept( massIndexer );
		return massIndexer.start();
	}

	private MassIndexerImpl createMassIndexer() {
		return new MassIndexerImpl(
				sessionFactory, targetedIndexedTypes,
				sessionContext,
				scopeWorkExecutor
		);
	}
}
