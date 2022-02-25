/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.mapping.definition.programmatic.impl;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.search.engine.backend.common.spi.FieldPaths;
import org.hibernate.search.engine.backend.types.ObjectStructure;
import org.hibernate.search.mapper.pojo.extractor.mapping.programmatic.ContainerExtractorPath;
import org.hibernate.search.mapper.pojo.logging.impl.Log;
import org.hibernate.search.mapper.pojo.mapping.building.spi.PojoMappingCollectorPropertyNode;
import org.hibernate.search.mapper.pojo.mapping.building.spi.PojoPropertyMetadataContributor;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.PropertyMappingObjectFieldStep;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.PropertyMappingStep;
import org.hibernate.search.mapper.pojo.model.additionalmetadata.building.spi.PojoAdditionalMetadataCollectorPropertyNode;
import org.hibernate.search.mapper.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.util.common.logging.impl.LoggerFactory;


class PropertyMappingObjectFieldStepImpl extends DelegatingPropertyMappingStep
		implements PropertyMappingObjectFieldStep, PojoPropertyMetadataContributor {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final PojoRawTypeModel<?> definingTypeModel;

	private final String relativeFieldName;

	private ObjectStructure structure = ObjectStructure.DEFAULT;

	private Integer includeDepth;
	private final Set<String> includePaths = new HashSet<>();
	private boolean includeRootObjectId = false;

	private Class<?> targetType;

	private ContainerExtractorPath extractorPath = ContainerExtractorPath.defaultExtractors();

	PropertyMappingObjectFieldStepImpl(PropertyMappingStep parent, PojoRawTypeModel<?> definingTypeModel,
			String relativeFieldName) {
		super( parent );
		this.definingTypeModel = definingTypeModel;
		if ( relativeFieldName != null && relativeFieldName.contains( FieldPaths.PATH_SEPARATOR_STRING ) ) {
			throw log.invalidFieldNameDotNotAllowed( relativeFieldName );
		}
		this.relativeFieldName = relativeFieldName;
	}

	@Override
	public void contributeAdditionalMetadata(PojoAdditionalMetadataCollectorPropertyNode collector) {
		// Nothing to do
	}

	@Override
	public void contributeMapping(PojoMappingCollectorPropertyNode collector) {
		collector.value( extractorPath ).objectField(
				definingTypeModel, relativeFieldName, structure, includeDepth, includePaths, includeRootObjectId,
				targetType
		);
	}

	@Override
	public PropertyMappingObjectFieldStep structure(ObjectStructure structure) {
		this.structure = structure;
		return this;
	}

	@Override
	public PropertyMappingObjectFieldStep includeDepth(Integer depth) {
		this.includeDepth = depth;
		return this;
	}

	@Override
	public PropertyMappingObjectFieldStep includePaths(Collection<String> paths) {
		this.includePaths.addAll( paths );
		return this;
	}

	@Override
	public PropertyMappingObjectFieldStep includeRootObjectId(boolean include) {
		this.includeRootObjectId = include;
		return this;
	}

	@Override
	public PropertyMappingObjectFieldStep extractors(ContainerExtractorPath extractorPath) {
		this.extractorPath = extractorPath;
		return this;
	}

	@Override
	public PropertyMappingObjectFieldStep targetType(Class<?> targetType) {
		this.targetType = targetType;
		return this;
	}
}
