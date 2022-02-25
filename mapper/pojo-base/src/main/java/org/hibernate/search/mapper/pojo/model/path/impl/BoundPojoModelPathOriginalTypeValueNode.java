/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.model.path.impl;

import org.hibernate.search.mapper.pojo.extractor.impl.BoundContainerExtractorPath;
import org.hibernate.search.mapper.pojo.model.spi.PojoTypeModel;

/**
 * @param <P> The property type of this node, i.e. the type of the property from which the values are extracted.
 * @param <V> The value type of this node, i.e. the type of values extracted from the property.
 */
public class BoundPojoModelPathOriginalTypeValueNode<P, V> extends BoundPojoModelPathValueNode<P, V> {

	private final BoundContainerExtractorPath<? super P, V> boundExtractorPath;

	BoundPojoModelPathOriginalTypeValueNode(BoundPojoModelPathPropertyNode<?, P> parent,
			BoundContainerExtractorPath<? super P, V> boundExtractorPath) {
		super( parent );
		this.boundExtractorPath = boundExtractorPath;
	}

	@Override
	public BoundContainerExtractorPath<? super P, V> getBoundExtractorPath() {
		return boundExtractorPath;
	}

	@Override
	public PojoTypeModel<V> getTypeModel() {
		return boundExtractorPath.getExtractedType();
	}

	@Override
	void appendSelfPathType(StringBuilder builder) {
		builder.append( "type " ).append( getTypeModel() );
	}
}
