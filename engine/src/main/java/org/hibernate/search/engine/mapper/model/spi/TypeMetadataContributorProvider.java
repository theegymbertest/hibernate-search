/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.mapper.model.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A provider of type metadata contributors, taking into account explicit contributions,
 * implicit discovery and inheritance.
 */
public final class TypeMetadataContributorProvider {

	public static Builder builder() {
		return new Builder();
	}

	private final Map<MappableTypeModel, List<Object>> contributionByType;
	private final List<TypeMetadataDiscoverer> metadataDiscoverers;
	private final Set<MappableTypeModel> typesSubmittedToDiscoverers = new HashSet<>();

	private TypeMetadataContributorProvider(Builder builder) {
		this.contributionByType = builder.contributionByType;
		this.metadataDiscoverers = builder.metadataDiscoverers;
	}

	/**
	 * @param typeModel The model of a type to retrieve contributors for, including supertype contributors.
	 * @param contributorType The Java type of the type metadata contributor.
	 * @return A set of the Java types of the metadata contributors
	 * @param <C> The Java type of the type metadata contributor.
	 */
	public <C> Set<C> get(MappableTypeModel typeModel, Class<C> contributorType) {
		return typeModel.descendingSuperTypes()
				.map( this::getContributionIncludingAutomaticallyDiscovered )
				.filter( Objects::nonNull )
				.flatMap( List::stream )
				.map( o -> contributorType.isInstance( o ) ? contributorType.cast( o ) : null )
				.filter( Objects::nonNull )
				// Using a LinkedHashSet because the order matters.
				.collect( Collectors.toCollection( LinkedHashSet::new ) );
	}

	/**
	 * @return A set containing all the types that were contributed to so far.
	 */
	public Set<? extends MappableTypeModel> typesContributedTo() {
		// Use a LinkedHashSet for deterministic iteration
		return Collections.unmodifiableSet( new LinkedHashSet<>( contributionByType.keySet() ) );
	}

	private List<Object> getContributionIncludingAutomaticallyDiscovered(
			MappableTypeModel typeModel) {
		if ( !typesSubmittedToDiscoverers.contains( typeModel ) ) {
			// Allow automatic discovery of metadata the first time we encounter each type
			for ( TypeMetadataDiscoverer metadataDiscoverer : metadataDiscoverers ) {
				Optional<?> discoveredContributor = metadataDiscoverer.discover( typeModel );
				if ( discoveredContributor.isPresent() ) {
					contributionByType.computeIfAbsent( typeModel, ignored -> new ArrayList<>() )
							.add( discoveredContributor.get() );
				}
			}
			typesSubmittedToDiscoverers.add( typeModel );
		}
		return contributionByType.get( typeModel );
	}

	public static final class Builder {
		// Use a LinkedHashMap for deterministic iteration
		private final Map<MappableTypeModel, List<Object>> contributionByType = new LinkedHashMap<>();
		private final List<TypeMetadataDiscoverer> metadataDiscoverers = new ArrayList<>();

		private Builder() {
		}

		public void contributor(MappableTypeModel typeModel, Object contributor) {
			contributionByType.computeIfAbsent( typeModel, ignored -> new ArrayList<>() )
					.add( contributor );
		}

		public void discoverer(TypeMetadataDiscoverer metadataDiscoverer) {
			metadataDiscoverers.add( metadataDiscoverer );
		}

		public TypeMetadataContributorProvider build() {
			return new TypeMetadataContributorProvider( this );
		}
	}
}
