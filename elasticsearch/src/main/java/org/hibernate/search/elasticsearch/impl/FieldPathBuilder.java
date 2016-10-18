/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.elasticsearch.impl;

import org.hibernate.search.exception.AssertionFailure;

/**
 * A non-threadsafe helper to extract path components from a string, one
 * component at a time.
 *
 * @author Yoann Rodiere
 */
class FieldPathBuilder implements Cloneable {

	private static final String PATH_COMPONENT_SEPARATOR = ".";

	/*
	 * Non-final for cloning only.
	 */
	private StringBuilder path = new StringBuilder();
	private int currentIndexInPath = 0;

	/**
	 * Append a string to the path for later consumption through {@link #nextComponent()}.
	 * @param pathPart A string that may or may not include path component
	 * separators (dots).
	 */
	public void append(String pathPart) {
		path.append( pathPart );
	}

	/**
	 * Append to the path the part of {@code otherPath} that is relative to the current path.
	 * <p>In other words, replace the current path with {@code otherPath} provided {@code otherPath}
	 * denotes a child element of the current path, while preserving the memory of the previously
	 * consumed path components.
	 * @param otherPath A path that must start with the current path.
	 */
	public void appendRelativePart(String otherPath) {
		String pathAsString = path.toString();
		if ( !otherPath.startsWith( pathAsString ) ) {
			throw new AssertionFailure( "The path '" + otherPath + "' is not contained within '" + pathAsString + "'" );
		}

		path.append( otherPath, path.length(), otherPath.length() );
	}

	/**
	 * Consumes one more component in the current path (if possible) and returns this component.
	 * @return The next complete path component, or null if it cannot be determined yet.
	 */
	public String nextComponent() {
		int nextSeparatorIndex = path.indexOf( PATH_COMPONENT_SEPARATOR, currentIndexInPath );
		if ( nextSeparatorIndex >= 0 ) {
			String childName = path.substring( currentIndexInPath, nextSeparatorIndex );
			currentIndexInPath = nextSeparatorIndex + 1 /* skip the dot */;
			return childName;
		}
		else {
			return null;
		}
	}

	/**
	 * Completes the path consumption by returning the last path component, i.e the remaining
	 * characters from the {@link #nextComponent() last consumed path components} to the end
	 * of the current path.
	 * @return The last path component
	 * @throws AssertionFailure If there is nothing to consume in the path, or if there is more
	 * than one component to consume.
	 */
	public String complete() {
		if ( currentIndexInPath >= path.length() ) {
			throw new AssertionFailure( "No path component remaining: " + toString() );
		}
		if ( path.indexOf( PATH_COMPONENT_SEPARATOR, currentIndexInPath ) >= 0 ) {
			throw new AssertionFailure( "Multiple path components remaining: " + toString() );
		}
		String lastComponent = path.substring( currentIndexInPath );
		currentIndexInPath = path.length();
		return lastComponent;
	}

	/**
	 * @param otherPath A path to make relative.
	 * @return The relative path from the currently consumed path (the components returned by {@link #nextComponent()}) to {@code otherPath}.
	 */
	public String makeRelative(String otherPath) {
		String pathAsString = path.toString();
		if ( !otherPath.startsWith( pathAsString ) ) {
			throw new AssertionFailure( "The path '" + otherPath + "' is not contained within '" + pathAsString + "'" );
		}

		return otherPath.substring( currentIndexInPath );
	}

	public void reset() {
		path.delete( 0, path.length() );
		currentIndexInPath = 0;
	}

	@Override
	public FieldPathBuilder clone() {
		try {
			FieldPathBuilder clone = (FieldPathBuilder) super.clone();
			clone.path = new StringBuilder( path );
			return clone;
		}
		catch (CloneNotSupportedException e) {
			throw new AssertionFailure( "Unexpected clone() failure", e );
		}
	}

	@Override
	public String toString() {
		return new StringBuilder( path ).insert( currentIndexInPath, "[]" ).toString();
	}

}