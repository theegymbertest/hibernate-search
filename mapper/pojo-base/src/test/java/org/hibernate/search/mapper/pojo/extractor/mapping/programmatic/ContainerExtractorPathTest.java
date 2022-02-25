/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.extractor.mapping.programmatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hibernate.search.mapper.pojo.extractor.builtin.BuiltinContainerExtractors;
import org.hibernate.search.util.common.SearchException;

import org.junit.Test;

public class ContainerExtractorPathTest {

	@Test
	public void builder() {
		ContainerExtractorPath path = ContainerExtractorPath.builder()
				.build();
		assertThat( path.isEmpty() ).isFalse();
		assertThat( path.isDefault() ).isTrue();
		assertThat( path.explicitExtractorNames() ).isEmpty();

		path = ContainerExtractorPath.builder()
				.noExtractors()
				.build();
		assertThat( path.isEmpty() ).isTrue();
		assertThat( path.isDefault() ).isFalse();
		assertThat( path.explicitExtractorNames() ).isEmpty();

		path = ContainerExtractorPath.builder()
				.extractor( BuiltinContainerExtractors.COLLECTION )
				.build();
		assertThat( path.isEmpty() ).isFalse();
		assertThat( path.isDefault() ).isFalse();
		assertThat( path.explicitExtractorNames() )
				.containsExactly( BuiltinContainerExtractors.COLLECTION );

		path = ContainerExtractorPath.builder()
				.extractor( BuiltinContainerExtractors.COLLECTION )
				.extractor( BuiltinContainerExtractors.MAP_VALUE )
				.build();
		assertThat( path.isEmpty() ).isFalse();
		assertThat( path.isDefault() ).isFalse();
		assertThat( path.explicitExtractorNames() )
				.containsExactly( BuiltinContainerExtractors.COLLECTION, BuiltinContainerExtractors.MAP_VALUE );

		path = ContainerExtractorPath.builder()
				.noExtractors()
				.extractor( BuiltinContainerExtractors.COLLECTION )
				.noExtractors()
				.extractor( BuiltinContainerExtractors.MAP_VALUE )
				.noExtractors()
				.build();
		assertThat( path.isEmpty() ).isFalse();
		assertThat( path.isDefault() ).isFalse();
		assertThat( path.explicitExtractorNames() )
				.containsExactly( BuiltinContainerExtractors.COLLECTION, BuiltinContainerExtractors.MAP_VALUE );

		path = ContainerExtractorPath.builder()
				.defaultExtractors()
				.build();
		assertThat( path.isEmpty() ).isFalse();
		assertThat( path.isDefault() ).isTrue();
		assertThat( path.explicitExtractorNames() ).isEmpty();

		path = ContainerExtractorPath.builder()
				.noExtractors()
				.defaultExtractors()
				.noExtractors()
				.build();
		assertThat( path.isEmpty() ).isFalse();
		assertThat( path.isDefault() ).isTrue();
		assertThat( path.explicitExtractorNames() ).isEmpty();
	}

	@Test
	public void builder_chainedContainerExtractors_defaultExtractors() {
		String errorMessage = "Invalid reference to default extractors:"
				+ " a chain of multiple container extractors must not include the default extractors";

		assertThatThrownBy(
				() -> ContainerExtractorPath.builder()
						.extractor( BuiltinContainerExtractors.COLLECTION ).defaultExtractors()
		)
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( errorMessage );

		assertThatThrownBy(
				() -> ContainerExtractorPath.builder()
						.defaultExtractors().extractor( BuiltinContainerExtractors.COLLECTION )
		)
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( errorMessage );
	}

}