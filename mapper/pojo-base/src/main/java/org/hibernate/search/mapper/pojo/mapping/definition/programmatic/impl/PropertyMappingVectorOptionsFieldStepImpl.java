/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.mapping.definition.programmatic.impl;

import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.engine.backend.types.VectorSimilarity;
import org.hibernate.search.mapper.pojo.extractor.mapping.programmatic.ContainerExtractorPath;
import org.hibernate.search.mapper.pojo.mapping.building.spi.PojoPropertyMetadataContributor;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.PropertyMappingStep;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.PropertyMappingVectorOptionsFieldStep;

class PropertyMappingVectorOptionsFieldStepImpl
		extends AbstractPropertyMappingFieldOptionsStep<PropertyMappingVectorOptionsFieldStepImpl>
		implements PropertyMappingVectorOptionsFieldStep, PojoPropertyMetadataContributor {

	PropertyMappingVectorOptionsFieldStepImpl(PropertyMappingStep parent, int dimension, String relativeFieldName) {
		super( parent, relativeFieldName, c -> c.vectorTypeOptionsStep().dimension( dimension ) );
		extractors( ContainerExtractorPath.noExtractors() );
	}

	@Override
	public PropertyMappingVectorOptionsFieldStep projectable(Projectable projectable) {
		fieldModelContributor.add( c -> c.vectorTypeOptionsStep().projectable( projectable ) );
		return thisAsS();
	}

	@Override
	public PropertyMappingVectorOptionsFieldStep vectorSimilarity(VectorSimilarity vectorSimilarity) {
		fieldModelContributor.add( c -> c.vectorTypeOptionsStep().vectorSimilarity( vectorSimilarity ) );
		return thisAsS();
	}

	@Override
	public PropertyMappingVectorOptionsFieldStep beamWidth(int beamWidth) {
		fieldModelContributor.add( c -> c.vectorTypeOptionsStep().beamWidth( beamWidth ) );
		return thisAsS();
	}

	@Override
	public PropertyMappingVectorOptionsFieldStep maxConnections(int maxConnections) {
		fieldModelContributor.add( c -> c.vectorTypeOptionsStep().maxConnections( maxConnections ) );
		return thisAsS();
	}

	@Override
	public PropertyMappingVectorOptionsFieldStep indexNullAs(String indexNullAs) {
		fieldModelContributor.add( c -> c.indexNullAs( indexNullAs ) );
		return thisAsS();
	}

	@Override
	PropertyMappingVectorOptionsFieldStepImpl thisAsS() {
		return this;
	}
}
