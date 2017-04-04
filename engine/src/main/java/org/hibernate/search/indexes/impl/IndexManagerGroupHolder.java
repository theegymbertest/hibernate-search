/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.indexes.impl;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.lucene.search.similarities.Similarity;
import org.hibernate.search.indexes.spi.IndexManager;
import org.hibernate.search.spi.WorkerBuildContext;
import org.hibernate.search.util.configuration.impl.MaskedProperty;

/**
 * A class responsible for holding references to
 * @author Yoann Rodiere
 */
public class IndexManagerGroupHolder {
	private static final String INDEX_SHARD_ID_SEPARATOR = ".";

	private final IndexManagerHolder parentHolder;

	private final String indexNameBase;

	private final Properties[] properties;

	private final Similarity similarity;

	private final ConcurrentMap<String, IndexManager> indexManagersRegistry = new ConcurrentHashMap<>();

	public IndexManagerGroupHolder(IndexManagerHolder parentHolder,
			String indexNameBase,
			Properties[] properties,
			Similarity similarity) {
		super();
		this.parentHolder = parentHolder;
		this.indexNameBase = indexNameBase;
		this.properties = properties;
		this.similarity = similarity;
	}

	IndexManager[] preInitializeIndexManagers(Class<?> entityType, WorkerBuildContext context) {
		IndexManager[] indexManagers;
		int nbrOfIndexManagers = properties.length;
		indexManagers = new IndexManager[nbrOfIndexManagers];
		for ( int index = 0; index < nbrOfIndexManagers; index++ ) {
			String indexManagerName = nbrOfIndexManagers > 1 ?
					indexNameBase + INDEX_SHARD_ID_SEPARATOR + index :
					indexNameBase;
			Properties indexProp = properties[index];
			IndexManager indexManager = indexManagersRegistry.get( indexManagerName );
			if ( indexManager == null ) {
				indexManager = createIndexManager( indexManagerName, entityType, indexProp, context );
			}
			else {
				indexManager.addContainedEntity( entityType );
			}
			indexManagers[index] = indexManager;
		}
		return indexManagers;
	}

	IndexManager getOrCreateIndexManager(String shardName, Class<?> entityType) {
		String indexName = indexNameBase;
		if ( shardName != null ) {
			indexName += INDEX_SHARD_ID_SEPARATOR + shardName;
		}

		IndexManager indexManager = indexManagersRegistry.get( indexName );
		if ( indexManager == null ) {
			Properties indexProperties = properties[0];
			if ( shardName != null ) {
				indexProperties = new MaskedProperty( indexProperties, shardName, indexProperties );
			}
			indexManager = createIndexManager( indexName, entityType, indexProperties, null );
		}
		else {
			indexManager.addContainedEntity( entityType );
		}

		return indexManager;
	}

	private IndexManager createIndexManager(String indexManagerName, Class<?> entityType,
			Properties indexProperties, WorkerBuildContext context) {
		return parentHolder.createIndexManager(
				indexManagerName,
				entityType,
				similarity,
				indexProperties,
				context
		);
	}

}
