/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.pojo.testsupport.types.expectations;

import java.time.ZoneId;
import java.util.TimeZone;

public interface TestEnvironment {

	ActiveTestEnvironment setup();

	static TestEnvironment getDefault() {
		return new TestEnvironment() {
			@Override
			public ActiveTestEnvironment setup() {
				return () -> { };
			}

			@Override
			public String toString() {
				return "Default env.";
			}
		};
	}

	static TestEnvironment withDefaultTimeZone(String zoneId) {
		return new TestEnvironment() {
			@Override
			public ActiveTestEnvironment setup() {
				TimeZone previousDefaultTimeZone = TimeZone.getDefault();
				TimeZone.setDefault( TimeZone.getTimeZone( ZoneId.of( zoneId ) ) );
				return () -> {
					TimeZone.setDefault( previousDefaultTimeZone );
				};
			}

			@Override
			public String toString() {
				return "Default TZ = " + zoneId;
			}
		};
	}

	interface ActiveTestEnvironment extends AutoCloseable {
		@Override
		void close();
	}

}
