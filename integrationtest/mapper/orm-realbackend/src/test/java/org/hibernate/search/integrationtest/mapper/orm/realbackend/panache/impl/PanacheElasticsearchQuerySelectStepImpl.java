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

class PanacheElasticsearchQuerySelectStepImpl<LOS> implements PanacheElasticsearchQuerySelectStep<LOS> {

	private final ElasticsearchSearchQuerySelectStep<EntityReference, ?, LOS> delegate;

	PanacheElasticsearchQuerySelectStepImpl(
			ElasticsearchSearchQuerySelectStep<EntityReference, ?, LOS> delegate) {
		this.delegate = delegate;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public PanacheElasticsearchQueryWhereStep<LOS> select(
			Function<? super ElasticsearchSearchProjectionFactory<EntityReference, ?>, ? extends ProjectionFinalStep<?>> contributor) {
		return new PanacheElasticsearchQueryWhereStepImpl<LOS>( delegate.select( f -> (ProjectionFinalStep) contributor.apply( f ) ) );
	}

}
