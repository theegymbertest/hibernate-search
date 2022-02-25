/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.model.path.impl;

import org.hibernate.search.mapper.pojo.extractor.impl.BoundContainerExtractorPath;
import org.hibernate.search.mapper.pojo.extractor.mapping.programmatic.ContainerExtractorPath;
import org.hibernate.search.mapper.pojo.model.path.PojoModelPath;
import org.hibernate.search.mapper.pojo.model.path.PojoModelPathValueNode;
import org.hibernate.search.mapper.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.mapper.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.mapper.pojo.model.spi.PojoTypeModel;

/**
 * @param <P> The property type of this node, i.e. the type of the property from which the values are extracted.
 * @param <V> The value type of this node, i.e. the type of values extracted from the property.
 */
public abstract class BoundPojoModelPathValueNode<P, V> extends BoundPojoModelPath {

	private final BoundPojoModelPathPropertyNode<?, P> parent;

	BoundPojoModelPathValueNode(BoundPojoModelPathPropertyNode<?, P> parent) {
		this.parent = parent;
	}

	/**
	 * @return The model path to the property from which the value represented by this node is extracted.
	 */
	@Override
	public final BoundPojoModelPathPropertyNode<?, P> getParent() {
		return parent;
	}

	@Override
	public final PojoTypeModel<?> getRootType() {
		if ( parent == null ) {
			return getTypeModel();
		}
		else {
			return parent.getRootType();
		}
	}

	@Override
	public final PojoModelPathValueNode toUnboundPath() {
		PojoModelPath.Builder builder = PojoModelPath.builder();
		appendPath( builder );
		return builder.toValuePathOrNull();
	}

	/**
	 * @return A sibling path node representing this value, cast to the given type.
	 */
	public final <U> BoundPojoModelPathCastedTypeValueNode<P, ? extends U> castTo(PojoRawTypeModel<U> typeModel) {
		return new BoundPojoModelPathCastedTypeValueNode<>( parent, getBoundExtractorPath(), typeModel.cast( getTypeModel() ) );
	}

	public final BoundPojoModelPathPropertyNode<V, ?> property(String propertyName) {
		PojoPropertyModel<?> propertyModel = getTypeModel().property( propertyName );
		return new BoundPojoModelPathPropertyNode<>(
				this, propertyModel
		);
	}

	/**
	 * @return The bound extractor path from the parent property to this value.
	 */
	public abstract BoundContainerExtractorPath<? super P, ?> getBoundExtractorPath();

	public abstract PojoTypeModel<V> getTypeModel();

	/**
	 * @return The extractor path from the parent property to this value.
	 * The path is guaranteed to be explicit (i.e. it won't be {@link ContainerExtractorPath#defaultExtractors()}).
	 */
	public final ContainerExtractorPath getExtractorPath() {
		return getBoundExtractorPath().getExtractorPath();
	}

	@Override
	final void appendSelfPath(StringBuilder builder) {
		builder.append( getExtractorPath() );
		builder.append( "(" );
		appendSelfPathType( builder );
		builder.append( ")" );
	}

	abstract void appendSelfPathType(StringBuilder builder);

	@Override
	final void appendSelfPath(PojoModelPath.Builder builder) {
		builder.value( getExtractorPath() );
	}
}
