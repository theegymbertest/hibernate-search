/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.orm.indexing;

import java.util.concurrent.CompletableFuture;

/**
 * The entry point for all explicit indexing operations.
 * <p>
 * While {@link org.hibernate.search.mapper.orm.cfg.HibernateOrmMapperSettings#AUTOMATIC_INDEXING_STRATEGY automatic indexing}
 * generally takes care of indexing entities as they are persisted/deleted in the database,
 * there are cases where massive operations must be applied to the index,
 * such as completely purging the index.
 * This is where the {@link SearchIndexer} comes in.
 */
public interface SearchIndexer {

	/**
	 * Purge the indexes targeted by this indexer, removing all documents.
	 * <p>
	 * When using multi-tenancy, only documents of one tenant will be removed:
	 * the tenant that was targeted by the session from where this indexer originated.
	 *
	 * @return A {@link CompletableFuture} reflecting the completion state of the operation.
	 */
	CompletableFuture<?> purge();

	/**
	 * Flush the changes to indexes that were not committed yet,
	 * and in the case of backends with a transaction log (Elasticsearch),
	 * apply operations in the transaction log that were not applied yet.
	 * <p>
	 * This is generally not useful as Hibernate Search commits changes automatically.
	 * Only use if you know what you are doing.
	 * <p>
	 * Note that some operations may still be queued when {@link #flush()} is called,
	 * in particular operations queued as part of automatic indexing before a transaction
	 * is committed.
	 * These operations will not be affected by a call to {@link #flush()},
	 * which is a very low-level operation.
	 *
	 * @return A {@link CompletableFuture} reflecting the completion state of the operation.
	 */
	CompletableFuture<?> flush();

	/**
	 * Merge all segments of the indexes targeted by this indexer into a single one.
	 * <p>
	 * Note this operation may affect performance positively as well as negatively.
	 * <p>
	 * As a rule of thumb, if indexes are read-only for extended periods of time,
	 * then calling {@link #optimize()} may improve performance.
	 * If indexes are written to, then calling {@link #optimize()}
	 * is likely to degrade read/write performance overall.
	 *
	 * @return A {@link CompletableFuture} reflecting the completion state of the operation.
	 */
	CompletableFuture<?> optimize();

}
