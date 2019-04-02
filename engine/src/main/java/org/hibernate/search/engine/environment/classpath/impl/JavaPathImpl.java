/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.environment.classpath.impl;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.search.engine.environment.classpath.spi.ClassLoadingException;
import org.hibernate.search.engine.environment.classpath.spi.ClassResolver;
import org.hibernate.search.engine.environment.classpath.spi.JavaPath;
import org.hibernate.search.engine.environment.classpath.spi.ResourceResolver;
import org.hibernate.search.engine.logging.impl.Log;
import org.hibernate.search.util.common.logging.impl.LoggerFactory;
import org.hibernate.search.util.common.impl.StringHelper;
import org.hibernate.search.util.common.impl.Throwables;

/**
 * A representation of the Java path (classpath, modulepath) allowing to perform operation on classes and resources.
 * <p>
 * Allows to load instances of other classes by using a fully qualified name,
 * to load resources, to load services, ...
 * <p>
 * Uses reflection and throws SearchException(s) with proper descriptions of the error,
 * such as the target class is missing a proper constructor, is an interface, is not found...
 *
 * @author Sanne Grinovero
 * @author Hardy Ferentschik
 * @author Ales Justin
 */
public final class JavaPathImpl implements JavaPath {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final ClassResolver classResolver;

	private final ResourceResolver resourceResolver;

	public JavaPathImpl(ClassResolver classResolver, ResourceResolver resourceResolver) {
		this.classResolver = classResolver;
		this.resourceResolver = resourceResolver;
	}

	@Override
	public <T> T instanceFromName(Class<T> targetSuperType, String classNameToLoad, String componentDescription) {
		final Class<?> clazzDef = classForName( classNameToLoad, componentDescription );
		return instanceFromClass( targetSuperType, clazzDef, componentDescription );
	}

	@Override
	public <T> T instanceFromClass(Class<T> targetSuperType, Class<?> classToLoad, String componentDescription) {
		checkClassType( classToLoad, componentDescription );
		final Object instance = untypedInstanceFromClass( classToLoad, componentDescription );
		return verifySuperTypeCompatibility( targetSuperType, instance, classToLoad, componentDescription );
	}

	@Override
	public <T> T untypedInstanceFromClass(final Class<T> classToLoad, final String componentDescription) {
		checkClassType( classToLoad, componentDescription );
		Constructor<T> constructor = getNoArgConstructor( classToLoad, componentDescription );
		try {
			return constructor.newInstance();
		}
		catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
			if ( StringHelper.isEmpty( componentDescription ) ) {
				throw log.unableToInstantiateClass( classToLoad, Throwables.getFirstNonNullMessage( e ), e );
			}
			else {
				throw log.unableToInstantiateComponent(
						componentDescription, classToLoad, Throwables.getFirstNonNullMessage( e ), e
				);
			}
		}
	}

	/**
	 * Verifies that an object instance is implementing a specific interface, or extending a type.
	 *
	 * @param targetSuperType the type to extend, or the interface it should implement
	 * @param instance the object instance to be verified
	 * @param classToLoad the Class of the instance
	 * @param componentDescription a user friendly description of the component represented by the verified instance
	 *
	 * @return the same instance
	 */
	@SuppressWarnings("unchecked")
	private static <T> T verifySuperTypeCompatibility(Class<T> targetSuperType, Object instance, Class<?> classToLoad, String componentDescription) {
		if ( !targetSuperType.isInstance( instance ) ) {
			// have a proper error message according to interface implementation or subclassing
			if ( targetSuperType.isInterface() ) {
				throw log.interfaceImplementedExpected( componentDescription, classToLoad, targetSuperType );
			}
			else {
				throw log.subtypeExpected( componentDescription, classToLoad, targetSuperType );
			}
		}
		else {
			return (T) instance;
		}
	}

	@Override
	public <T> T instanceFromClass(Class<T> targetSuperType, Class<?> classToLoad, String componentDescription,
			Map<String, String> constructorParameter) {
		checkClassType( classToLoad, componentDescription );
		Constructor<?> singleMapConstructor = getSingleMapConstructor( classToLoad, componentDescription );
		if ( constructorParameter == null ) {
			constructorParameter = new HashMap<>( 0 );//can't use the emptyMap singleton as it needs to be mutable
		}
		final Object instance;
		try {
			instance = singleMapConstructor.newInstance( constructorParameter );
		}
		catch (Exception e) {
			throw log.unableToInstantiateComponent(
					componentDescription, classToLoad, Throwables.getFirstNonNullMessage( e ), e
			);
		}
		return verifySuperTypeCompatibility( targetSuperType, instance, classToLoad, componentDescription );
	}

	private static void checkClassType(Class<?> classToLoad, String componentDescription) {
		if ( classToLoad.isInterface() ) {
			throw log.implementationRequired( componentDescription, classToLoad );
		}
	}

	/**
	 * Verifies if target class has a no-args constructor, and that it is
	 * accessible in current security manager.
	 * If checks are succesfull, return the constructor; otherwise appropriate exceptions are thrown.
	 * @param classToLoad the class type to check
	 * @param componentDescription adds a meaningful description to the type to describe in the error messsage
	 */
	private static <T> Constructor<T> getNoArgConstructor(Class<T> classToLoad, String componentDescription) {
		try {
			return classToLoad.getConstructor();
		}
		catch (SecurityException e) {
			throw log.securityManagerLoadingError( componentDescription, classToLoad, e );
		}
		catch (NoSuchMethodException e) {
			throw log.noPublicNoArgConstructor( componentDescription, classToLoad );
		}
	}

	private static Constructor<?> getSingleMapConstructor(Class<?> classToLoad, String componentDescription) {
		try {
			return classToLoad.getConstructor( Map.class );
		}
		catch (SecurityException e) {
			throw log.securityManagerLoadingError( componentDescription, classToLoad, e );
		}
		catch (NoSuchMethodException e) {
			throw log.missingConstructor( componentDescription, classToLoad );
		}
	}

	@Override
	public Class<?> classForName(String classNameToLoad, String componentDescription) {
		Class<?> clazz;
		try {
			clazz = classResolver.classForName( classNameToLoad );
		}
		catch (ClassLoadingException e) {
			throw log.unableToFindComponentImplementation( componentDescription, classNameToLoad, e );
		}
		return clazz;
	}

	@Override
	public <T> Class<? extends T> classForName(Class<T> targetSuperType, String classNameToLoad,
			String componentDescription) {
		final Class<?> clazzDef = classForName( classNameToLoad, componentDescription );
		try {
			return clazzDef.asSubclass( targetSuperType );
		}
		catch (ClassCastException cce) {
			throw log.notAssignableImplementation( componentDescription, classNameToLoad, targetSuperType );
		}
	}

	@Override
	public <T> Iterable<T> loadJavaServices(Class<T> serviceContract) {
		return classResolver.loadJavaServices( serviceContract );
	}

	@Override
	public URL locateResource(String name) {
		return resourceResolver.locateResource( name );
	}

	@Override
	public InputStream locateResourceStream(String name) {
		return resourceResolver.locateResourceStream( name );
	}
}
