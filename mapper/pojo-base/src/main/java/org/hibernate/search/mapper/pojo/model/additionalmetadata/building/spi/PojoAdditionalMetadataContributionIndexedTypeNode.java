/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.model.additionalmetadata.building.spi;

import java.util.Map;
import java.util.Optional;

import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.RoutingBinder;

public interface PojoAdditionalMetadataContributionIndexedTypeNode extends PojoAdditionalMetadataCollector {

	/**
	 * @return The name of the backend where this type should be indexed,
	 * or {@link Optional#empty()} (the default) to target the default backend.
	 */
	default Optional<String> backendName() {
		return Optional.empty();
	}

	/**
	 * @return The name of the index where this type should be indexed,
	 * or {@link Optional#empty()} (the default) to derive the index name from the entity type.
	 */
	default Optional<String> indexName() {
		return Optional.empty();
	}

	/**
	 * @return {@code true} if this type must be indexed
	 * (the default once a {@link PojoAdditionalMetadataContributionIndexedTypeNode} is created),
	 * {@code false} if it must not (in which case metadata provided through other methods is ignored).
	 */
	default boolean enabled() {
		return true;
	}

	/**
	 * @param binder The routing binder.
	 * @param params The parameters to pass to the binder.
	 */
	Optional<MappingComponentDefinition<RoutingBinder>> routingBinder();

}
