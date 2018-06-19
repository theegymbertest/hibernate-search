/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

package org.hibernate.search.query.dsl;

import org.apache.lucene.search.Query;

/**
 * Represents a boolean query that can contains one or more elements to join
 *
 * @author Emmanuel Bernard
 */
public interface BooleanJunction<T extends BooleanJunction> extends QueryCustomization<T>, Termination {
	/**
	 * The boolean query results should match the subquery
	 * @param query the query to match (nulls are ignored)
	 * @return a {@link BooleanJunction}
	 */
	BooleanJunction should(Query query);

	/**
	 * The boolean query results must (or must not) match the subquery
	 * Call the .not() method to ensure results of the boolean query do NOT match the subquery.
	 *
	 * @param query the query to match (nulls are ignored)
	 * @return a {@link MustJunction}
	 */
	MustJunction must(Query query);

	/**
	 * @return true if no restrictions have been applied
	 */
	boolean isEmpty();

	/**
	 * Add a default <a href="MinimumShouldMatchContext.html#minimumshouldmatch">"minimumShouldMatch" constraint</a>.
	 *
	 * @param matchingClausesNumber A definition of the number of "should" clauses that have to match.
	 * If positive, it is the number of clauses that have to match.
	 * See <a href="MinimumShouldMatchContext.html#minimumshouldmatch-minimum">Definition of the minimum</a>
	 * for details and possible values, in particular negative values.
	 * @return {@code this}, for method chaining.
	 */
	default BooleanJunction minimumShouldMatchNumber(int matchingClausesNumber) {
		return minimumShouldMatch()
				.ifMoreThan( 0 ).thenRequireNumber( matchingClausesNumber )
				.end();
	}

	/**
	 * Add a default <a href="MinimumShouldMatchContext.html#minimumshouldmatch">"minimumShouldMatch" constraint</a>.
	 *
	 * @param matchingClausesPercent A definition of the number of "should" clauses that have to match, as a percentage.
	 * If positive, it is the percentage of the total number of "should" clauses that have to match.
	 * See <a href="MinimumShouldMatchContext.html#minimumshouldmatch-minimum">Definition of the minimum</a>
	 * for details and possible values, in particular negative values.
	 * @return {@code this}, for method chaining.
	 */
	default BooleanJunction minimumShouldMatchPercent(int matchingClausesPercent) {
		return minimumShouldMatch()
				.ifMoreThan( 0 ).thenRequirePercent( matchingClausesPercent )
				.end();
	}

	/**
	 * Start defining the minimum number of "should" constraints that have to match
	 * in order for the boolean predicate to match.
	 * <p>
	 * See {@link MinimumShouldMatchContext}.
	 *
	 * @return A {@link MinimumShouldMatchContext} allowing to define constraints.
	 */
	MinimumShouldMatchContext<? extends BooleanJunction> minimumShouldMatch();
}
