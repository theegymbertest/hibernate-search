/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.util.impl;

import java.lang.reflect.Field;


import org.hibernate.search.exception.AssertionFailure;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.DelegatingBasicLogger;

/**
 * Test helper to listen for logging events.
 * For this to work, it requires JBoss Logging to pick up our custom
 * implementation {@code Log4DelegatingLogger} via ServiceLoader.
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2015 Red Hat Inc.
 */
final class LogInspectionHelper {

	private LogInspectionHelper() {
	}

	public static ListenableDelegatingLogger convertType(BasicLogger log) {
		if ( log instanceof DelegatingBasicLogger ) {
			//Most loggers generated via the annotation processor are of this type
			DelegatingBasicLogger wrapper = (DelegatingBasicLogger) log;
			try {
				return extractFromWrapper( wrapper );
			}
			catch (Exception cause) {
				throw new RuntimeException( cause );
			}
		}
		if ( ! ( log instanceof ListenableDelegatingLogger ) ) {
			throw new AssertionFailure( "Unexpected log type: JBoss Logger didn't register the custom TestableLoggerProvider as logger provider" );
		}
		return (ListenableDelegatingLogger) log;
	}

	private static ListenableDelegatingLogger extractFromWrapper(DelegatingBasicLogger wrapper) throws Exception {
		Field field = DelegatingBasicLogger.class.getDeclaredField( "log" );
		field.setAccessible( true );
		Object object = field.get( wrapper );
		return convertType( (BasicLogger) object );
	}

}
