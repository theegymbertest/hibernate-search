/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.query.dsl.impl;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.hibernate.search.query.dsl.MinimumShouldMatchConditionContext;
import org.hibernate.search.query.dsl.MinimumShouldMatchContext;
import org.hibernate.search.query.dsl.MinimumShouldMatchNonEmptyContext;
import org.hibernate.search.util.logging.impl.Log;
import org.hibernate.search.util.logging.impl.LoggerFactory;

import org.apache.lucene.search.BooleanQuery;

final class MinimumShouldMatchContextImpl<N> implements MinimumShouldMatchContext<N>,
		MinimumShouldMatchConditionContext<N>, MinimumShouldMatchNonEmptyContext<N> {

	private static final Log log = LoggerFactory.make( MethodHandles.lookup() );

	private final N nextContext;

	private NavigableMap<Integer, MinimumShouldMatchConstraint> minimumShouldMatchConstraints;
	private int ignoreConstraintCeiling = 0;

	MinimumShouldMatchContextImpl(N nextContext) {
		this.nextContext = nextContext;
	}

	@Override
	public MinimumShouldMatchConditionContext<N> ifMoreThan(int ignoreConstraintCeiling) {
		if ( ignoreConstraintCeiling < 0 ) {
			throw log.mustBePositiveOrZero( "ignoreConstraintCeiling" );
		}
		this.ignoreConstraintCeiling = ignoreConstraintCeiling;
		return this;
	}

	@Override
	public MinimumShouldMatchNonEmptyContext<N> thenRequireNumber(int matchingClausesNumber) {
		addMinimumShouldMatchConstraint(
				ignoreConstraintCeiling,
				new MinimumShouldMatchConstraint( matchingClausesNumber, null )
		);
		return this;
	}

	@Override
	public MinimumShouldMatchNonEmptyContext<N> thenRequirePercent(int matchingClausesPercent) {
		addMinimumShouldMatchConstraint(
				ignoreConstraintCeiling,
				new MinimumShouldMatchConstraint( null, matchingClausesPercent )
		);
		return this;
	}

	void applyMinimum(BooleanQuery.Builder builder, int shouldClauseCount) {
		if ( minimumShouldMatchConstraints != null ) {
			int minimumShouldMatch;
			Map.Entry<Integer, MinimumShouldMatchConstraint> entry =
					minimumShouldMatchConstraints.lowerEntry( shouldClauseCount );
			if ( entry != null ) {
				minimumShouldMatch = entry.getValue().toMinimum( shouldClauseCount );
			}
			else {
				minimumShouldMatch = shouldClauseCount;
			}
			builder.setMinimumNumberShouldMatch( minimumShouldMatch );
		}
	}

	private void addMinimumShouldMatchConstraint(int ignoreConstraintCeiling,
			MinimumShouldMatchConstraint constraint) {
		if ( minimumShouldMatchConstraints == null ) {
			// We'll need to go through the data in ascending order, so use a TreeMap
			minimumShouldMatchConstraints = new TreeMap<>();
		}
		Object previous = minimumShouldMatchConstraints.put( ignoreConstraintCeiling, constraint );
		if ( previous != null ) {
			throw log.minimumShouldMatchConflictingConstraints( ignoreConstraintCeiling );
		}
	}

	@Override
	public N end() {
		return nextContext;
	}

	private static final class MinimumShouldMatchConstraint {
		private final Integer matchingClausesNumber;
		private final Integer matchingClausesPercent;

		MinimumShouldMatchConstraint(Integer matchingClausesNumber, Integer matchingClausesPercent) {
			this.matchingClausesNumber = matchingClausesNumber;
			this.matchingClausesPercent = matchingClausesPercent;
		}

		int toMinimum(int totalShouldClauseNumber) {
			int minimum;
			if ( matchingClausesNumber != null ) {
				if ( matchingClausesNumber >= 0 ) {
					minimum = matchingClausesNumber;
				}
				else {
					minimum = totalShouldClauseNumber + matchingClausesNumber;
				}
			}
			else {
				if ( matchingClausesPercent >= 0 ) {
					minimum = matchingClausesPercent * totalShouldClauseNumber / 100;
				}
				else {
					minimum = totalShouldClauseNumber + matchingClausesPercent * totalShouldClauseNumber / 100;
				}
			}

			if ( minimum < 1 || minimum > totalShouldClauseNumber ) {
				throw log.minimumShouldMatchMinimumOutOfBounds( minimum, totalShouldClauseNumber );
			}

			return minimum;
		}
	}
}
