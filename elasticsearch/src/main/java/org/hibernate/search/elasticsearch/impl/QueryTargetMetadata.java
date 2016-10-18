/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.elasticsearch.impl;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.search.engine.spi.DocumentBuilderIndexedEntity;
import org.hibernate.search.exception.AssertionFailure;

class QueryTargetMetadata {
	private final Set<DocumentBuilderIndexedEntity> documentBuilders;

	private Set<String> idFieldNames;

	public QueryTargetMetadata(Set<DocumentBuilderIndexedEntity> documentBuilders) {
		super();
		this.documentBuilders = documentBuilders;
	}

	private Set<String> getIdFieldNames() {
		if ( idFieldNames == null ) {
			idFieldNames = new HashSet<>();
			if ( documentBuilders.isEmpty() ) {
				throw new AssertionFailure( "Cannot guess the ID field name without the list of queried types" );
			}

			for ( DocumentBuilderIndexedEntity builder : documentBuilders ) {
				idFieldNames.add( builder.getIdKeywordName() );
			}
		}
		return idFieldNames;
	}

	public boolean isId(String field) {
		Set<String> idNames = getIdFieldNames();
		if ( idNames.contains( field ) ) {
			if ( idNames.size() > 1 ) {
				// Only fail if we actually need to query on the ID
				throw ToElasticsearch.LOG.ambiguousIdQuery( field, idFieldNames );
			}
			else {
				return true;
			}
		}
		else {
			return false;
		}
	}
}