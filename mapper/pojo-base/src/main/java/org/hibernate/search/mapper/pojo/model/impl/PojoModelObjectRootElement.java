/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.model.impl;

import java.util.Collection;
import java.util.Collections;

import org.hibernate.search.mapper.pojo.automaticindexing.building.impl.PojoIndexingDependencyCollectorTypeNode;
import org.hibernate.search.mapper.pojo.model.PojoElementAccessor;
import org.hibernate.search.mapper.pojo.model.additionalmetadata.building.impl.PojoTypeAdditionalMetadataProvider;
import org.hibernate.search.mapper.pojo.model.path.impl.BoundPojoModelPathTypeNode;
import org.hibernate.search.mapper.pojo.model.spi.PojoBootstrapIntrospector;

/**
 * @param <T> The type of the object used as a root element.
 */
public class PojoModelObjectRootElement<T> extends AbstractPojoModelCompositeElement<T> {

	private final BoundPojoModelPathTypeNode<T> modelPath;

	public PojoModelObjectRootElement(BoundPojoModelPathTypeNode<T> modelPath,
			PojoBootstrapIntrospector introspector,
			PojoTypeAdditionalMetadataProvider typeAdditionalMetadataProvider) {
		super( introspector, typeAdditionalMetadataProvider );
		this.modelPath = modelPath;
	}

	@Override
	public String toString() {
		return modelPath.toString();
	}

	@Override
	public <M> Collection<M> markers(Class<M> markerType) {
		return Collections.emptyList();
	}

	public void contributeDependencies(PojoIndexingDependencyCollectorTypeNode<T> dependencyCollector) {
		contributePropertyDependencies( dependencyCollector );
	}

	@Override
	PojoElementAccessor<T> doCreateAccessor() {
		return new PojoRootElementAccessor<>();
	}

	@Override
	BoundPojoModelPathTypeNode<T> getModelPathTypeNode() {
		return modelPath;
	}
}
