/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.model.additionalmetadata.building.spi;

import java.util.List;
import java.util.Optional;

import org.hibernate.search.mapper.pojo.model.spi.PojoRawTypeIdentifier;

public interface PojoAdditionalMetadataContributionTypeNode extends PojoAdditionalMetadataCollector {

	/**
	 * @return The identifier of the type to which this definition applies.
	 */
	PojoRawTypeIdentifier<?> typeIdentifier();

	/**
	 * Metadata contributions for an entity type.
	 */
	List<PojoAdditionalMetadataContributionTypeNode> entityType();

	/**
	 * Mark this type as an indexed type.
	 * <p>
	 * <strong>WARNING:</strong> only entity types may be indexed.
	 *
	 * @param enabled {@code true} to mark the type as indexed, {@code false} to mark it as not indexed.
	 * @return A {@link PojoAdditionalMetadataContributionIndexedTypeNode}, to provide optional metadata
	 * about the indexed type.
	 */
	PojoAdditionalMetadataContributionIndexedTypeNode markAsIndexed(boolean enabled);

	default Optional<PojoAdditionalMetadataContributionPropertyNode> property(String propertyName) {
		return Optional.empty();
	}

}
