/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.pojo.testsupport.types.expectations;

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

	interface ActiveTestEnvironment extends AutoCloseable {
		@Override
		void close();
	}

}
