/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.elasticsearch.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.search.Query;
import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.engine.service.spi.Startable;
import org.hibernate.search.engine.spi.DocumentBuilderIndexedEntity;
import org.hibernate.search.engine.spi.EntityIndexBinding;
import org.hibernate.search.indexes.spi.IndexManager;
import org.hibernate.search.query.engine.impl.LuceneQueryTranslator;
import org.hibernate.search.query.engine.spi.QueryDescriptor;
import org.hibernate.search.spi.BuildContext;
import org.hibernate.search.util.impl.CollectionHelper;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

/**
 * Translates Lucene queries into ES queries.
 * <p>
 * Extra-experimental ;)
 *
 * @author Gunnar Morling
 */
public class ElasticsearchLuceneQueryTranslator implements LuceneQueryTranslator, Startable {

	private ExtendedSearchIntegrator extendedIntegrator;

	@Override
	public void start(Properties properties, BuildContext context) {
		extendedIntegrator = context.getUninitializedSearchIntegrator();
	}

	@Override
	public QueryDescriptor convertLuceneQuery(Collection<Class<?>> entities, Query luceneQuery) {
		Set<DocumentBuilderIndexedEntity> documentBuilders = getElasticsearchEntitiesDocumentBuilders( entities );
		if ( bindings.isEmpty() ) {
			return null;
		}

		QueryTargetMetadata targetMetadata = new QueryTargetMetadata( documentBuilders );
		JsonObject convertedQuery = ToElasticsearch.fromLuceneQuery( targetMetadata, luceneQuery );

		JsonObject query = new JsonObject();
		query.add( "query", convertedQuery );

		return new ElasticsearchJsonQueryDescriptor( query );
	}

	private Set<DocumentBuilderIndexedEntity> getElasticsearchEntitiesDocumentBuilders(Collection<Class<?>> entities) {
		Set<DocumentBuilderIndexedEntity> result = new HashSet<>();
		Set<Class<?>> queriedEntityTypes = getQueriedEntityTypes( entities );
		Set<Class<?>> queriedEntityTypesWithSubTypes = extendedIntegrator.getIndexedTypesPolymorphic( queriedEntityTypes.toArray( new Class<?>[queriedEntityTypes.size()] ) );

		for ( Class<?> queriedEntityType : queriedEntityTypesWithSubTypes ) {
			EntityIndexBinding binding = extendedIntegrator.getIndexBinding( queriedEntityType );

			if ( binding == null ) {
				continue;
			}

			IndexManager[] indexManagers = binding.getIndexManagers();

			for ( IndexManager indexManager : indexManagers ) {
				if ( indexManager instanceof ElasticsearchIndexManager ) {
					result.add( binding.getDocumentBuilder() );
				}
			}
		}

		return result;
	}

	private Set<Class<?>> getQueriedEntityTypes(Collection<Class<?>> indexedTargetedEntities) {
		if ( indexedTargetedEntities == null || indexedTargetedEntities.isEmpty() ) {
			return extendedIntegrator.getIndexBindings().keySet();
		}
		else {
			return new HashSet<>( indexedTargetedEntities );
		}
	}
}
