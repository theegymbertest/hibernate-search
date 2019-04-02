/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.common.impl;

import org.hibernate.search.engine.environment.bean.spi.BeanResolverBuildContext;
import org.hibernate.search.engine.environment.bean.spi.ReflectionBeanResolver;

class BeanResolverBuildContextImpl implements BeanResolverBuildContext {
	private final ReflectionBeanResolver fallback;

	BeanResolverBuildContextImpl(ReflectionBeanResolver fallback) {
		this.fallback = fallback;
	}

	@Override
	public ReflectionBeanResolver getFallback() {
		return fallback;
	}
}
