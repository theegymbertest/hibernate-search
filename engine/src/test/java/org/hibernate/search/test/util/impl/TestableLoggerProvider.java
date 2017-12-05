/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.util.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.logging.Logger;
import org.jboss.logging.LoggerProvider;
import org.jboss.logging.LoggerProviders2;

/**
 * A {@code LoggerProvider} for JBoss Logger.
 * See also META-INF/services/org.jboss.logging.LoggerProvider
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2015 Red Hat Inc.
 */
public final class TestableLoggerProvider implements org.jboss.logging.LoggerProvider {

	//We LEAK Logger instances: good only for testing as we know the set of categories is limited in practice
	private final ConcurrentMap<String,Logger> reuseLoggerInstances = new ConcurrentHashMap<String,Logger>();

	private volatile LoggerProvider delegate;

	private final LogListenable listenable = new LogListenable();

	private LoggerProvider getDelegate() {
		LoggerProvider theDelegate = delegate;
		if ( theDelegate != null ) {
			return theDelegate;
		}
		synchronized (this) {
			theDelegate = delegate;
			if ( theDelegate == null ) {
				theDelegate = findDelegate();
				delegate = theDelegate;
			}
			return theDelegate;
		}
	}

	private static LoggerProvider findDelegate() {
		return LoggerProviders2.findProvider( TestableLoggerProvider.class );
	}

	@Override
	public Logger getLogger(final String name) {
		Logger logger = reuseLoggerInstances.get( name );
		if ( logger == null ) {
			Logger delegateLogger = getDelegate().getLogger( name );
			logger = new ListenableDelegatingLogger( "".equals( name ) ? "ROOT" : name, delegateLogger, listenable );
			Logger previous = reuseLoggerInstances.putIfAbsent( name, logger );
			if ( previous != null ) {
				return previous;
			}
		}
		return logger;
	}

	@Override
	public void clearMdc() {
		getDelegate().clearMdc();
	}

	@Override
	public Object putMdc(String key, Object value) {
		return getDelegate().putMdc( key, value );
	}

	@Override
	public Object getMdc(String key) {
		return getDelegate().getMdc( key );
	}

	@Override
	public void removeMdc(String key) {
		getDelegate().removeMdc( key );
	}

	@Override
	public Map<String, Object> getMdcMap() {
		return getDelegate().getMdcMap();
	}

	@Override
	public void clearNdc() {
		getDelegate().clearNdc();
	}

	@Override
	public String getNdc() {
		return getDelegate().getNdc();
	}

	@Override
	public int getNdcDepth() {
		return getDelegate().getNdcDepth();
	}

	@Override
	public String popNdc() {
		return getDelegate().popNdc();
	}

	@Override
	public String peekNdc() {
		return getDelegate().peekNdc();
	}

	@Override
	public void pushNdc(String message) {
		getDelegate().pushNdc( message );
	}

	@Override
	public void setNdcMaxDepth(int maxDepth) {
		getDelegate().setNdcMaxDepth( maxDepth );
	}
}
