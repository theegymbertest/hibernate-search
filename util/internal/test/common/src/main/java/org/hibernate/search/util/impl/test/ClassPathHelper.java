/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.util.impl.test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClassPathHelper {

	private ClassPathHelper() {
	}

	/**
	 * @param classLoader A classloader to extract the classpath from.
	 * @return URLs that are for sure in the classpath of the given classloader.
	 * Some may be missing, if using exotic classloaders,
	 * since this method only extracts information from {@link URLClassLoader}.
	 */
	public static List<URL> collectClassPath(ClassLoader classLoader) {
		List<URL> result = new ArrayList<>();
		ClassLoader current = classLoader;
		while ( current != null ) {
			if ( current instanceof URLClassLoader ) {
				Collections.addAll( result, ( (URLClassLoader) classLoader ).getURLs() );
			}
			current = current.getParent();
		}
		return result;
	}

}
