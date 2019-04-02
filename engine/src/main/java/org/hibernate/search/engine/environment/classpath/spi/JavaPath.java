/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.environment.classpath.spi;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.hibernate.search.engine.environment.service.spi.Service;
import org.hibernate.search.util.common.SearchException;

/**
 * Utility to load instances of other classes by using a fully qualified name,
 * or from a class type.
 * Uses reflection and throws SearchException(s) with proper descriptions of the error,
 * like the target class is missing a proper constructor, is an interface, is not found...
 *
 * @author Sanne Grinovero
 * @author Hardy Ferentschik
 * @author Ales Justin
 */
public interface JavaPath {

	/**
	 * Creates an instance of a target class specified by the fully qualified class name using a {@link ClassLoader}
	 * as fallback when the class cannot be found in the context one.
	 *
	 * @param <T> matches the type of targetSuperType: defines the return type
	 * @param targetSuperType the return type of the function, the classNameToLoad will be checked
	 * to be assignable to this type.
	 * @param classNameToLoad a fully qualified class name, whose type is assignable to targetSuperType
	 * @param componentDescription a meaningful description of the role the instance will have,
	 * used to enrich error messages to describe the context of the error
	 *
	 * @return a new instance of the type given by {@code classNameToLoad}
	 *
	 * @throws SearchException wrapping other error types with a proper error message for all kind of problems, like
	 * classNotFound, missing proper constructor, wrong type, security errors.
	 */
	<T> T instanceFromName(Class<T> targetSuperType, String classNameToLoad, String componentDescription);

	/**
	 * Creates an instance of target class
	 *
	 * @param <T> the type of targetSuperType: defines the return type
	 * @param targetSuperType the created instance will be checked to be assignable to this type
	 * @param classToLoad the class to be instantiated
	 * @param componentDescription a role name/description to contextualize error messages
	 *
	 * @return a new instance of classToLoad
	 *
	 * @throws SearchException wrapping other error types with a proper error message for all kind of problems, like
	 * missing proper constructor, wrong type, securitymanager errors.
	 */
	<T> T instanceFromClass(Class<T> targetSuperType, Class<?> classToLoad, String componentDescription);

	/**
	 * Creates an instance of target class. Similar to {@link #instanceFromClass(Class, Class, String)} but not checking
	 * the created instance will be of any specific type: using {@link #instanceFromClass(Class, Class, String)} should
	 * be preferred whenever possible.
	 *
	 * @param <T> the type of targetSuperType: defines the return type
	 * @param classToLoad the class to be instantiated
	 * @param componentDescription a role name/description to contextualize error messages. Ideally should be provided, but it can handle null.
	 *
	 * @return a new instance of classToLoad
	 *
	 * @throws SearchException wrapping other error types with a proper error message for all kind of problems, like
	 * missing proper constructor, securitymanager errors.
	 */
	<T> T untypedInstanceFromClass(Class<T> classToLoad, String componentDescription);

	/**
	 * Creates an instance of target class having a Map of strings as constructor parameter.
	 * Most of the Analyzer SPIs provided by Lucene have such a constructor.
	 *
	 * @param <T> the type of targetSuperType: defines the return type
	 * @param targetSuperType the created instance will be checked to be assignable to this type
	 * @param classToLoad the class to be instantiated
	 * @param componentDescription a role name/description to contextualize error messages
	 * @param constructorParameter a Map to be passed to the constructor. The loaded type must have such a constructor.
	 *
	 * @return a new instance of classToLoad
	 *
	 * @throws SearchException wrapping other error types with a proper error message for all kind of problems, like
	 * missing proper constructor, wrong type, security errors.
	 */
	<T> T instanceFromClass(Class<T> targetSuperType, Class<?> classToLoad, String componentDescription,
			Map<String, String> constructorParameter);

	Class<?> classForName(String classNameToLoad, String componentDescription);

	<T> Class<? extends T> classForName(Class<T> targetSuperType, String classNameToLoad,
			String componentDescription);

	/**
	 * Discovers and instantiates implementations of the named service contract.
	 * <p>
	 * NOTE : the term service here is used differently than {@link Service}.
	 * Instead here we are talking about services as defined by {@link java.util.ServiceLoader}.
	 *
	 * @param serviceContract The java type defining the service contract
	 * @param <T> The type of the service contract
	 *
	 * @return The ordered set of discovered services.
	 */
	<T> Iterable<T> loadJavaServices(Class<T> serviceContract);

	/**
	 * Locate a resource by name (classpath lookup).
	 *
	 * @param name The resource name.
	 *
	 * @return The located URL; may return {@code null} to indicate the resource was not found
	 */
	URL locateResource(String name);

	/**
	 * Locate a resource by name (classpath lookup) and get its stream.
	 *
	 * @param name The resource name.
	 *
	 * @return The stream of the located resource; may return {@code null} to indicate the resource was not found
	 */
	InputStream locateResourceStream(String name);

}
