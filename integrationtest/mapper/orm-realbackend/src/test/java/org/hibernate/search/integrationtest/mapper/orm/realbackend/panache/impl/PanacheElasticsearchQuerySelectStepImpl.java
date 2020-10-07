/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.impl;

import java.util.function.Function;

import org.hibernate.search.backend.elasticsearch.search.projection.dsl.ElasticsearchSearchProjectionFactory;
import org.hibernate.search.backend.elasticsearch.search.query.dsl.ElasticsearchSearchQuerySelectStep;
import org.hibernate.search.engine.search.projection.dsl.ProjectionFinalStep;
import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api.PanacheElasticsearchQuerySelectStep;
import org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api.PanacheElasticsearchQueryWhereStep;
import org.hibernate.search.mapper.orm.common.EntityReference;

class PanacheElasticsearchQuerySelectStepImpl<Entity, LOS> implements PanacheElasticsearchQuerySelectStep<Entity, LOS> {

	private final ElasticsearchSearchQuerySelectStep<EntityReference, ? extends Entity, LOS> delegate;

	PanacheElasticsearchQuerySelectStepImpl(ElasticsearchSearchQuerySelectStep<EntityReference, ? extends Entity, LOS> delegate) {
		this.delegate = delegate;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <P> PanacheElasticsearchQueryWhereStep<P, LOS> select(
			Function<? super ElasticsearchSearchProjectionFactory<EntityReference, ? extends Entity>, ? extends ProjectionFinalStep<? extends P>> contributor) {
		return new PanacheElasticsearchQueryWhereStepImpl<>( delegate.select( f -> (ProjectionFinalStep<P>) contributor.apply( f ) ) );	}

}
