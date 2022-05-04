/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.model.additionalmetadata.building.spi;

import java.util.Map;
import java.util.Optional;

public class MappingComponentDefinition<B> {

	private final B binder;
	private final Map<String, Object> params;

	public MappingComponentDefinition(B binder, Map<String, Object> params) {
		this.binder = binder;
		this.params = params;
	}

	public Optional<B> getBinder() {
		return binder;
	}

	public Map<String, Object> getParams() {
		return params;
	}
}
