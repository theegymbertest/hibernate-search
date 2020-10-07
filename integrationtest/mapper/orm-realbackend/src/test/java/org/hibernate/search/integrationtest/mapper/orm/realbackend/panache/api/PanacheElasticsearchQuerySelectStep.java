/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.orm.realbackend.panache.api;

import java.util.function.Function;

import org.hibernate.search.backend.elasticsearch.search.predicate.dsl.ElasticsearchSearchPredicateFactory;
import org.hibernate.search.backend.elasticsearch.search.projection.dsl.ElasticsearchSearchProjectionFactory;
import org.hibernate.search.engine.search.predicate.dsl.PredicateFinalStep;
import org.hibernate.search.engine.search.projection.dsl.ProjectionFinalStep;
import org.hibernate.search.mapper.orm.common.EntityReference;

public interface PanacheElasticsearchQuerySelectStep<Entity, LOS> extends PanacheElasticsearchQueryWhereStep<Entity, LOS> {

	<P> PanacheElasticsearchQueryWhereStep<P, LOS> select(
			Function<? super ElasticsearchSearchProjectionFactory<EntityReference, ? extends Entity>, ? extends ProjectionFinalStep<? extends P>> contributor);

	@Override
	default PanacheElasticsearchQueryOptionsStep<Entity, LOS> where(
			Function<? super ElasticsearchSearchPredicateFactory, ? extends PredicateFinalStep> contributor) {
		return this.<Entity>select( f -> f.entity() ).where( contributor );
	}
}
