/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.extractor.mapping.programmatic;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.hibernate.search.mapper.pojo.extractor.ContainerExtractor;
import org.hibernate.search.mapper.pojo.extractor.ContainerExtractorConfigurationContext;
import org.hibernate.search.mapper.pojo.extractor.builtin.BuiltinContainerExtractors;
import org.hibernate.search.mapper.pojo.logging.impl.Log;
import org.hibernate.search.util.common.logging.impl.LoggerFactory;

/**
 * A chain of {@link ContainerExtractor container extractors} to be applied one after the other to a property value,
 * in order to extract other values.
 * <p>
 * The extractors are either represented:
 * <ul>
 * <li>explicitly by their name, e.g. {@code ["map-values", "collection"]},
 * meaning "apply the 'map-values' extractor to the property value, then apply the 'collection' extractor to the map values".
 * Names are either {@link BuiltinContainerExtractors built-in}
 * or {@link ContainerExtractorConfigurationContext registered at bootstrap}.
 * <li>or simply by the "default" path ({@link #defaultExtractors()}),
 * which means "whatever default Hibernate Search manages to apply using its internal extractor resolution algorithm".
 * This second form may result in different "resolved" paths depending on the type of the property it is applied to.
 * </ul>
 *
 * @see ContainerExtractor
 * @see BuiltinContainerExtractors
 */
public class ContainerExtractorPath {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private static final ContainerExtractorPath DEFAULT = new ContainerExtractorPath(
			true, Collections.emptyList()
	);
	private static final ContainerExtractorPath NONE = new ContainerExtractorPath(
			false, Collections.emptyList()
	);

	/**
	 * @return A builder allowing to create a {@link ContainerExtractorPath}
	 * by specifying its components one by one.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * @return A path that will apply the default extractor(s) based on the property type.
	 */
	public static ContainerExtractorPath defaultExtractors() {
		return DEFAULT;
	}

	/**
	 * @return A path that will not apply any container extractor.
	 */
	public static ContainerExtractorPath noExtractors() {
		return NONE;
	}

	/**
	 * @param extractorName A container extractor referenced by its name.
	 * @return A path that will apply the referenced container extractor.
	 * @see BuiltinContainerExtractors
	 */
	public static ContainerExtractorPath explicitExtractor(String extractorName) {
		return new ContainerExtractorPath(
				false,
				Collections.singletonList( extractorName )
		);
	}

	/**
	 * @param extractorNames A list of container extractors referenced by their name.
	 * @return A path that will apply the referenced container extractors in order.
	 */
	public static ContainerExtractorPath explicitExtractors(List<String> extractorNames) {
		if ( extractorNames.isEmpty() ) {
			return noExtractors();
		}
		else {
			return new ContainerExtractorPath(
					false,
					Collections.unmodifiableList( new ArrayList<>( extractorNames ) )
			);
		}
	}

	private final boolean applyDefaultExtractors;
	private final List<String> explicitExtractorNames;

	private ContainerExtractorPath(boolean applyDefaultExtractors, List<String> explicitExtractorNames) {
		this.applyDefaultExtractors = applyDefaultExtractors;
		this.explicitExtractorNames = explicitExtractorNames;
	}

	@Override
	public boolean equals(Object obj) {
		if ( ! ( obj instanceof ContainerExtractorPath ) ) {
			return false;
		}
		ContainerExtractorPath other = (ContainerExtractorPath) obj;
		return applyDefaultExtractors == other.applyDefaultExtractors
				&& Objects.equals( explicitExtractorNames, other.explicitExtractorNames );
	}

	@Override
	public int hashCode() {
		return Objects.hash( applyDefaultExtractors, explicitExtractorNames );
	}

	@Override
	public String toString() {
		if ( isDefault() ) {
			return "<default value extractors>";
		}
		else if ( explicitExtractorNames.isEmpty() ) {
			return "<no value extractors>";
		}
		else {
			StringJoiner joiner = new StringJoiner( ", ", "<", ">" );
			for ( String extractorName : explicitExtractorNames ) {
				joiner.add( extractorName );
			}
			return joiner.toString();
		}
	}

	/**
	 * @return {@code true} if this path represents the default extractor(s),
	 * which will be determined automatically based on the property type.
	 * {@code false} otherwise.
	 */
	public boolean isDefault() {
		return applyDefaultExtractors;
	}

	/**
	 * @return {@code true} if this path is empty,
	 * i.e. it represents direct access to the property value.
	 * {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return !isDefault() && explicitExtractorNames.isEmpty();
	}

	/**
	 * @return The list of extractor names explicitly referenced by this path.
	 * Empty if this path represents the default extractor(s).
	 */
	public List<String> explicitExtractorNames() {
		return explicitExtractorNames;
	}

	public static class Builder {

		private final List<String> currentExplicitExtractors = new ArrayList<>();
		private boolean noExtractors;
		private boolean defaultExtractors;

		private Builder() {
		}

		/**
		 * Append to the path a value extraction using the given container extractor path.
		 *
		 * @param extractorPath The container extractors to apply, as a {@link ContainerExtractorPath}.
		 * @return {@code this}, for method chaining.
		 * @throws org.hibernate.search.util.common.SearchException If no property name was previously given.
		 */
		public Builder extractors(ContainerExtractorPath extractorPath) {
			if ( extractorPath.isDefault() ) {
				return defaultExtractors();
			}
			else if ( extractorPath.isEmpty() ) {
				return noExtractors();
			}
			else {
				for ( String extractorName : extractorPath.explicitExtractorNames() ) {
					extractor( extractorName );
				}
				return this;
			}
		}

		/**
		 * Appends to the path a value extraction using the given container extractor.
		 * <p>
		 * Multiple {@link #extractor(String)} calls can be chained to apply multiple extractors.
		 *
		 * @param extractorName The name of the container extractor to apply.
		 * @return {@code this}, for method chaining.
		 * @throws org.hibernate.search.util.common.SearchException If no property name was previously given.
		 * @see org.hibernate.search.mapper.pojo.extractor.builtin.BuiltinContainerExtractors
		 */
		public Builder extractor(String extractorName) {
			if ( defaultExtractors ) {
				throw log.cannotUseDefaultExtractorsInMultiExtractorChain();
			}
			noExtractors = false;
			currentExplicitExtractors.add( extractorName );
			return this;
		}

		/**
		 * Appends to the path a direct value extraction, not using any container extractors.
		 * @return {@code this}, for method chaining.
		 */
		public Builder noExtractors() {
			if ( !defaultExtractors ) {
				noExtractors = true;
			}
			return this;
		}

		/**
		 * Appends to the path a value extraction using the default container extractors.
		 * @return {@code this}, for method chaining.
		 */
		public Builder defaultExtractors() {
			if ( !currentExplicitExtractors.isEmpty() ) {
				throw log.cannotUseDefaultExtractorsInMultiExtractorChain();
			}
			noExtractors = false;
			defaultExtractors = true;
			return this;
		}

		/**
		 * @return A {@link ContainerExtractorPath} built from the given components.
		 */
		public ContainerExtractorPath build() {
			ContainerExtractorPath result;
			if ( !currentExplicitExtractors.isEmpty() ) {
				result = ContainerExtractorPath.explicitExtractors( currentExplicitExtractors );
			}
			else if ( noExtractors ) {
				result = ContainerExtractorPath.noExtractors();
			}
			else { // Default
				result = ContainerExtractorPath.defaultExtractors();
			}
			currentExplicitExtractors.clear();
			noExtractors = false;
			defaultExtractors = false;
			return result;
		}

		public boolean isUndefined() {
			// Empty if nothing was called
			return !noExtractors && !defaultExtractors && currentExplicitExtractors.isEmpty();
		}

		public void reset() {
			noExtractors = false;
			defaultExtractors = false;
			currentExplicitExtractors.clear();
		}

	}
}
