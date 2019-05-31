/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.work.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.search.backend.elasticsearch.client.spi.ElasticsearchRequest;
import org.hibernate.search.backend.elasticsearch.client.spi.ElasticsearchResponse;
import org.hibernate.search.backend.elasticsearch.client.impl.Paths;
import org.hibernate.search.backend.elasticsearch.util.spi.URLEncodedString;
import org.hibernate.search.backend.elasticsearch.work.builder.impl.OptimizeWorkBuilder;

/**
 * An optimize work for ES5, using the ForceMerge API.
 * <p>
 * The ForceMerge API replaces the removed Optimize API in ES5.
 *
 * @author Yoann Rodiere
 */
public class OptimizeWork extends AbstractSimpleElasticsearchWork<Void> {

	protected OptimizeWork(Builder builder) {
		super( builder );
	}

	@Override
	protected Void generateResult(ElasticsearchWorkExecutionContext context, ElasticsearchResponse response) {
		return null;
	}

	public static class Builder
			extends AbstractBuilder<Builder>
			implements OptimizeWorkBuilder {
		private final List<URLEncodedString> indexNames = new ArrayList<>();

		public Builder(Set<URLEncodedString> indexNames) {
			super( DefaultElasticsearchRequestSuccessAssessor.INSTANCE );
			this.indexNames.addAll( indexNames );
		}

		@Override
		protected ElasticsearchRequest buildRequest() {
			ElasticsearchRequest.Builder builder = ElasticsearchRequest.post()
					.multiValuedPathComponent( indexNames )
					.pathComponent( Paths._FORCEMERGE );

			return builder.build();
		}

		@Override
		public OptimizeWork build() {
			return new OptimizeWork( this );
		}
	}
}