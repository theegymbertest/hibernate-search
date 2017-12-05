/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.util.impl;

import java.text.MessageFormat;
import java.util.Locale;

import org.jboss.logging.Logger;

/**
 * A {@code Logger} implementation which delegates to another one
 * but makes it possible to test for events being logged (not logged).
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2015 Red Hat Inc.
 */
final class ListenableDelegatingLogger extends Logger {

	private final Logger delegate;
	private final LogListenable listenable;

	ListenableDelegatingLogger(String name, Logger delegate, LogListenable listenable) {
		super( name );
		this.delegate = delegate;
		this.listenable = listenable;
	}

	public LogListenable getListenable() {
		return listenable;
	}

	@Override
	public boolean isEnabled(final Level level) {
		// We want users to think this logger is enabled, so as to detect all logger calls
		return listenable.isEnabled() || delegate.isEnabled(level);
	}

	@Override
	protected void doLog(final Level level, final String loggerClassName, final Object message, final Object[] parameters, final Throwable thrown) {
		if ( listenable.isEnabled() ) {
			LoggingEvent event = new LoggingEvent(
					getName(), loggerClassName, level,
					parameters == null || parameters.length == 0 ? String.valueOf( message )
							: new MessageFormat( String.valueOf( message ), Locale.getDefault() ).format( parameters ),
					thrown );
			listenable.notify( event );
		}
		delegate.log( loggerClassName, level, message, parameters, thrown );
	}

	@Override
	protected void doLogf(final Level level, final String loggerClassName, final String format, final Object[] parameters, final Throwable thrown) {
		if ( listenable.isEnabled() ) {
			LoggingEvent event = new LoggingEvent(
					getName(), loggerClassName, level,
					parameters == null ? format : String.format( Locale.getDefault(), format, parameters ),
					thrown );
			listenable.notify( event );
		}
		delegate.logf( loggerClassName, level, thrown, format, parameters );
	}
}
