/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.bridge.binding.impl;

import org.hibernate.search.engine.environment.bean.BeanHolder;
import org.hibernate.search.mapper.pojo.automaticindexing.building.impl.PojoIndexingDependencyCollectorTypeNode;
import org.hibernate.search.mapper.pojo.bridge.ObjectBridge;
import org.hibernate.search.mapper.pojo.model.dependency.impl.PojoObjectIndexingDependencyConfigurationContextImpl;
import org.hibernate.search.mapper.pojo.model.dependency.impl.PojoTypeIndexingDependencyConfigurationContextImpl;
import org.hibernate.search.mapper.pojo.model.impl.PojoModelObjectRootElement;
import org.hibernate.search.mapper.pojo.model.impl.PojoModelTypeRootElement;

public final class BoundObjectBridge<T> {
	private final BeanHolder<? extends ObjectBridge<? super T>> bridgeHolder;
	private final PojoModelObjectRootElement<T> pojoModelRootElement;
	private final PojoObjectIndexingDependencyConfigurationContextImpl<T> pojoDependencyContext;

	BoundObjectBridge(BeanHolder<? extends ObjectBridge<? super T>> bridgeHolder,
			PojoModelObjectRootElement<T> pojoModelRootElement,
			PojoObjectIndexingDependencyConfigurationContextImpl<T> pojoDependencyContext) {
		this.bridgeHolder = bridgeHolder;
		this.pojoModelRootElement = pojoModelRootElement;
		this.pojoDependencyContext = pojoDependencyContext;
	}

	public BeanHolder<? extends ObjectBridge<? super T>> getBridgeHolder() {
		return bridgeHolder;
	}

	public ObjectBridge<? super T> getBridge() {
		return bridgeHolder.get();
	}

	public void contributeDependencies(PojoIndexingDependencyCollectorTypeNode<T> dependencyCollector) {
		pojoModelRootElement.contributeDependencies( dependencyCollector );
		pojoDependencyContext.contributeDependencies( dependencyCollector );
	}
}
