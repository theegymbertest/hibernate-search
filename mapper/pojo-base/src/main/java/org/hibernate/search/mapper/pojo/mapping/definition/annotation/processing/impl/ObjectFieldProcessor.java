/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.search.engine.backend.types.ObjectStructure;
import org.hibernate.search.mapper.pojo.extractor.mapping.programmatic.ContainerExtractorPath;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.PropertyMappingAnnotationProcessor;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.PropertyMappingAnnotationProcessorContext;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.PropertyMappingStep;

public class ObjectFieldProcessor implements PropertyMappingAnnotationProcessor<ObjectField> {

	@Override
	public void process(PropertyMappingStep mappingContext, ObjectField annotation,
			PropertyMappingAnnotationProcessorContext context) {
		String cleanedUpName = annotation.name();
		if ( cleanedUpName.isEmpty() ) {
			cleanedUpName = null;
		}

		Integer cleanedUpIncludeDepth = annotation.includeDepth();
		if ( cleanedUpIncludeDepth.equals( -1 ) ) {
			cleanedUpIncludeDepth = null;
		}

		String[] includePathsArray = annotation.includePaths();
		Set<String> cleanedUpIncludePaths;
		if ( includePathsArray.length > 0 ) {
			cleanedUpIncludePaths = new HashSet<>();
			Collections.addAll( cleanedUpIncludePaths, includePathsArray );
		}
		else {
			cleanedUpIncludePaths = Collections.emptySet();
		}

		ContainerExtractorPath extractorPath = context.toContainerExtractorPath( annotation.extraction() );

		Class<?> cleanedUpTargetType = annotation.targetType();
		if ( cleanedUpTargetType.equals( void.class ) ) {
			cleanedUpTargetType = null;
		}

		ObjectStructure structure = annotation.structure();

		mappingContext.indexedEmbedded( cleanedUpName )
				.extractors( extractorPath )
				.structure( structure )
				.includeDepth( cleanedUpIncludeDepth )
				.includePaths( cleanedUpIncludePaths )
				.includeRootObjectId( annotation.includeRootObjectId() )
				.targetType( cleanedUpTargetType );
	}
}
