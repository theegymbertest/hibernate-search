/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.mapping.building.spi;

import org.hibernate.search.mapper.pojo.model.additionalmetadata.building.spi.PojoAdditionalMetadataCollectorTypeNode;

// TODO Remove this interface in the next few commits; we're only keeping it temporarily because it's simpler that way.
public interface PojoTypeMetadataContributor extends PojoTypeAdditonalMetadataContributor, PojoTypeMappingContributor {

	default void contributeMapping(PojoMappingCollectorTypeNode collector) {
		// No-op by default
	}

}
