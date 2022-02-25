/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.impl;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;

import org.hibernate.search.engine.environment.bean.BeanReference;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ObjectBinderRef;
import org.hibernate.search.mapper.pojo.bridge.mapping.impl.BeanDelegatingBinder;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.ObjectBinder;
import org.hibernate.search.mapper.pojo.logging.impl.Log;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectBinding;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.MappingAnnotationProcessorContext;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.TypeMappingAnnotationProcessor;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.TypeMappingAnnotationProcessorContext;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.TypeMappingStep;
import org.hibernate.search.util.common.logging.impl.LoggerFactory;

public final class ObjectBindingProcessor implements TypeMappingAnnotationProcessor<ObjectBinding> {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	@Override
	public void process(TypeMappingStep mapping, ObjectBinding annotation,
			TypeMappingAnnotationProcessorContext context) {
		ObjectBinderRef binderRef = annotation.binder();
		ObjectBinder binder = createBinder( binderRef, context );

		if ( binderRef.params() != null ) {
			Map<String, Object> params = context.toMap( binderRef.params() );
			mapping.objectBinding( binder, params );
		}
		else {
			mapping.objectBinding( binder );
		}
	}

	private ObjectBinder createBinder(ObjectBinderRef binderReferenceAnnotation, MappingAnnotationProcessorContext context) {
		Optional<BeanReference<? extends ObjectBinder>> binderReference = context.toBeanReference(
				ObjectBinder.class,
				ObjectBinderRef.UndefinedBinderImplementationType.class,
				binderReferenceAnnotation.type(), binderReferenceAnnotation.name(),
				binderReferenceAnnotation.retrieval()
		);

		if ( !binderReference.isPresent() ) {
			throw log.missingBinderReferenceInBinding();
		}

		return new BeanDelegatingBinder( binderReference.get() );
	}
}
