/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.util.impl.integrationtest.common.stub.backend.index.impl;

import java.util.concurrent.CompletableFuture;

import org.hibernate.search.engine.backend.work.execution.spi.IndexScopeWorkExecutor;
import org.hibernate.search.engine.mapper.session.context.spi.DetachedSessionContextImplementor;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.StubBackendBehavior;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.index.StubIndexScopeWork;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.search.impl.StubScopeModel;

class StubIndexScopeWorkExecutor implements IndexScopeWorkExecutor {

	private final StubBackendBehavior behavior;
	private final DetachedSessionContextImplementor sessionContext;
	private final StubScopeModel scopeModel;

	StubIndexScopeWorkExecutor(StubBackendBehavior behavior, DetachedSessionContextImplementor sessionContext,
			StubScopeModel scopeModel) {
		this.behavior = behavior;
		this.sessionContext = sessionContext;
		this.scopeModel = scopeModel;
	}

	@Override
	public CompletableFuture<?> optimize() {
		StubIndexScopeWork work = StubIndexScopeWork.builder( StubIndexScopeWork.Type.OPTIMIZE ).build();
		return behavior.executeIndexScopeWork( scopeModel.getIndexNames(), work );
	}

	@Override
	public CompletableFuture<?> purge() {
		StubIndexScopeWork work = StubIndexScopeWork.builder( StubIndexScopeWork.Type.PURGE )
				.tenantIdentifier( sessionContext.getTenantIdentifier() )
				.build();
		return behavior.executeIndexScopeWork( scopeModel.getIndexNames(), work );
	}

	@Override
	public CompletableFuture<?> flush() {
		StubIndexScopeWork work = StubIndexScopeWork.builder( StubIndexScopeWork.Type.FLUSH ).build();
		return behavior.executeIndexScopeWork( scopeModel.getIndexNames(), work );
	}
}
