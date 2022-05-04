/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.mapper.model.spi;

import java.util.Optional;

public interface TypeMetadataDiscoverer {

	/**
	 * A hook to discover metadata lazily during bootstrap, which can be helpful when resolving metadata
	 * from the type itself (Java annotations on a Java type, in particular).
	 *
	 * @param typeModel The type model which is about to be contributed to.
	 * @return An additional, automatically discovered contributor.
	 */
	Optional<?> discover(MappableTypeModel typeModel);

}
