/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.environment.bean.spi;

import org.hibernate.search.engine.environment.classpath.spi.JavaPath;
import org.hibernate.search.engine.environment.bean.BeanHolder;
import org.hibernate.search.util.common.AssertionFailure;


/**
 * @author Yoann Rodiere
 */
public final class ReflectionBeanResolver implements BeanResolver {

	private final JavaPath javaPath;

	public ReflectionBeanResolver(JavaPath javaPath) {
		this.javaPath = javaPath;
	}

	@Override
	public void initialize(BeanResolverBuildContext buildContext) {
		throw new AssertionFailure( "This method should not be called" );
	}

	@Override
	public void close() {
		// Nothing to do
	}

	@Override
	public <T> BeanHolder<T> resolve(Class<T> typeReference) {
		return BeanHolder.of( resolveNoClosingNecessary( typeReference ) );
	}

	public <T> T resolveNoClosingNecessary(Class<T> typeReference) {
		return javaPath.untypedInstanceFromClass( typeReference, typeReference.getName() );
	}

	@Override
	public <T> BeanHolder<T> resolve(Class<T> typeReference, String implementationFullyQualifiedClassName) {
		return BeanHolder.of( resolveNoClosingNecessary( typeReference, implementationFullyQualifiedClassName ) );
	}

	public <T> T resolveNoClosingNecessary(Class<T> typeReference, String implementationFullyQualifiedClassName) {
		Class<? extends T> implementationClass = javaPath.classForName(
				typeReference, implementationFullyQualifiedClassName, typeReference.getName()
		);
		return javaPath.untypedInstanceFromClass( implementationClass, implementationFullyQualifiedClassName );
	}

}
