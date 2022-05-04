/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.model.additionalmetadata.building.spi;

import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.model.path.PojoModelPathValueNode;

public interface PojoAdditionalMetadataContributionValueNode {

	default List<PojoModelPathValueNode> associationInverseSide() {
		return Collections.emptyList();
	}

	default boolean associationEmbedded() {
		return false;
	}

	default ReindexOnUpdate reindexOnUpdate() {
		return ReindexOnUpdate.DEFAULT;
	}

	default Set<PojoModelPathValueNode> derivedFrom() {
		return Collections.emptySet();
	}

	default OptionalInt decimalScale() {
		return OptionalInt.empty();
	}
}
