/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.backend.types;

/**
 * Defines a function to calculate the vector similarity, i.e. distance between two vectors.
 */
public enum VectorSimilarity {
	/**
	 * Use the backend-specific default.
	 */
	DEFAULT,
	/**
	 * L2 (Euclidean) norm. Distance is calculated as {@code d(x,y) = \sum_(i=1) ^(n) (x_i - y_i)^2 } and similarity function is {@code s = 1 / (1+d) }
	 */
	L2,
	/**
	 * Inner product (dot product in particular). Distance is calculated as {@code d(x,y) = \sum_(i=1) ^(n) x_i*y_i },
	 * similarity function may differ between backends.
	 */
	INNER_PRODUCT,
	/**
	 * Cosine similarity. Distance is calculated as {@code d(x,y) = 1 - \sum_(i=1) ^(n) x_i*y_i / ( \sqrt( \sum_(i=1) ^(n) x_i^2 ) \sqrt( \sum_(i=1) ^(n) y_i^2 ) },
	 * similarity function may differ between backends.
	 */
	COSINE;
}
