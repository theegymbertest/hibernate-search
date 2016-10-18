/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.query.engine.impl;

import java.util.Collection;

import org.apache.lucene.search.Query;
import org.hibernate.search.engine.service.spi.Service;
import org.hibernate.search.query.engine.spi.QueryDescriptor;

/**
 * Implementations translate Lucene queries into other backend-specific representions.
 * <p>
 * Not a public contract for the time being!
 *
 * @author Gunnar Morling
 */
public interface LuceneQueryTranslator extends Service {

	/**
	 * Convert a Lucene query to a backend-specific representation, if such conversion is
	 * required.
	 * @param entities The scope of this query
	 * @param query The query to convert
	 * @return A backend-specific representation of the Lucene query, or null if the Lucene
	 * query can be used directly.
	 */
	QueryDescriptor convertLuceneQuery(Collection<Class<?>> entities, Query query);

}
