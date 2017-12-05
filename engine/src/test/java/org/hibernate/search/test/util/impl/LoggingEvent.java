/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.util.impl;

import org.jboss.logging.Logger;

public final class LoggingEvent {
	private final String loggerName;
	private final String loggerFqcn;
	private final Logger.Level level;
	private final String renderedMessage;
	private final Throwable thrown;

	LoggingEvent(
			String loggerName,
			String loggerFqcn,
			Logger.Level level,
			String renderedMessage,
			Throwable thrown) {
		this.loggerName = loggerName;
		this.loggerFqcn = loggerFqcn;
		this.level = level;
		this.renderedMessage = renderedMessage;
		this.thrown = thrown;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public String getLoggerFqcn() {
		return loggerFqcn;
	}

	public Logger.Level getLevel() {
		return level;
	}

	public String getRenderedMessage() {
		return renderedMessage;
	}

	public Throwable getThrown() {
		return thrown;
	}
}
