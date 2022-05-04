/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.model.additionalmetadata.building.spi;

import java.util.Optional;

import org.hibernate.search.engine.mapper.mapping.building.spi.MappingConfigurationCollector;
import org.hibernate.search.engine.mapper.model.spi.TypeMetadataDiscoverer;
import org.hibernate.search.mapper.pojo.model.additionalmetadata.impl.PojoTypeAdditionalMetadata;
import org.hibernate.search.mapper.pojo.model.path.spi.PojoPathsDefinition;


/**
 * Metadata contribution for an entity type.
 * <p>
 * <strong>WARNING:</strong> entity types must always be defined upfront without relying on
 * {@link MappingConfigurationCollector#collectDiscoverer(TypeMetadataDiscoverer) metadata discovery},
 * because Hibernate Search needs to be able to have a complete view of all the possible entity types
 * in order to handle automatic reindexing.
 * Relying on type discovery for entity detection would mean running the risk of one particular
 * entity subtype not being detected (because only its supertype is mentioned in the schema of indexed entities),
 * which could result in incomplete automatic reindexing.
 *
 * @see PojoTypeAdditionalMetadata#isEntity()
 * @see PojoTypeAdditionalMetadata#getEntityTypeMetadata()
 *
 * @return A {@link PojoAdditionalMetadataContributionEntityTypeNode}, to provide optional metadata
 * about the entity.
 */
public interface PojoAdditionalMetadataContributionEntityTypeNode {

	/**
	 * @return The name of this entity type.
	 */
	String entityName();

	/**
	 * @return The paths definition for this entity type,
	 * i.e. the object supporting the creation of path filters that will be used in particular
	 * when performing dirty checking during automatic reindexing.
	 */
	PojoPathsDefinition pathsDefinition();

	/**
	 * @return The name of a property hosting the entity ID.
	 * This ID will be used by default to generate document IDs when no document ID was configured in the mapping.
	 */
	Optional<String> entityIdPropertyName();

}
