/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.orm.massindexing.monitor;

import org.hibernate.search.mapper.orm.indexing.SearchIndexerMonitor;

/**
 * As a MassIndexer can take some time to finish it's job,
 * a MassIndexerProgressMonitor can be defined in the configuration
 * property hibernate.search.worker.indexing.monitor
 * implementing this interface to track indexing performance.
 * <p>
 * Implementations must:
 * <ul>
 * <li>	be threadsafe </li>
 * <li> have a no-arg constructor </li>
 * </ul>
 *
 * @author Sanne Grinovero
 * @author Hardy Ferentschik
 * @deprecated Use {@link org.hibernate.search.mapper.orm.indexing.SearchIndexer}
 * with a {@link org.hibernate.search.mapper.orm.indexing.SearchIndexerMonitor} instead.
 */
@Deprecated
public interface MassIndexingMonitor extends SearchIndexerMonitor {

}
