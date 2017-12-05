/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.util.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

final class LogListenable {

	// Synchronize access on the field
	private final List<LogListener> enabledListeners = new LinkedList<>();
	private final AtomicBoolean interceptEnabled = new AtomicBoolean( false );

	void registerListener(LogListener newListener) {
		synchronized (enabledListeners) {
			if ( newListener != null ) {
				enabledListeners.add( newListener );
				interceptEnabled.set( true );
			}
		}
	}

	void unregisterListener(LogListener listener) {
		synchronized (enabledListeners) {
			enabledListeners.remove( listener );
			if ( enabledListeners.isEmpty() ) {
				interceptEnabled.set( false );
			}
		}
	}

	boolean isEnabled() {
		return interceptEnabled.get();
	}

	void notify(LoggingEvent event) {
		synchronized (enabledListeners) {
			for ( LogListener listener : enabledListeners ) {
				listener.loggedEvent( event );
			}
		}
	}
}
