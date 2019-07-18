/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.backend.tck.testsupport.operations;

import static org.assertj.core.api.Assertions.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.search.engine.search.common.ValueConvert;
import org.hibernate.search.engine.search.dsl.aggregation.AggregationFinalStep;
import org.hibernate.search.engine.search.dsl.aggregation.SearchAggregationFactory;
import org.hibernate.search.engine.spatial.GeoPoint;
import org.hibernate.search.integrationtest.backend.tck.testsupport.operations.expectations.AggregationScenario;
import org.hibernate.search.integrationtest.backend.tck.testsupport.operations.expectations.SupportedSingleFieldAggregationExpectations;
import org.hibernate.search.integrationtest.backend.tck.testsupport.operations.expectations.UnsupportedSingleFieldAggregationExpectations;
import org.hibernate.search.integrationtest.backend.tck.testsupport.types.FieldTypeDescriptor;
import org.hibernate.search.integrationtest.backend.tck.testsupport.util.ExpectationsAlternative;
import org.hibernate.search.integrationtest.backend.tck.testsupport.util.TypeAssertionHelper;
import org.hibernate.search.util.common.data.Range;
import org.hibernate.search.util.impl.integrationtest.common.NormalizationUtils;

import org.assertj.core.api.Assertions;

public class RangeAggregationDescriptor extends AggregationDescriptor {

	@Override
	public <F> ExpectationsAlternative<
					SupportedSingleFieldAggregationExpectations<F>,
					UnsupportedSingleFieldAggregationExpectations
			> getSingleFieldAggregationExpectations(FieldTypeDescriptor<F> typeDescriptor) {
		if ( String.class.equals( typeDescriptor.getJavaType() )
				|| GeoPoint.class.equals( typeDescriptor.getJavaType() )
				|| Boolean.class.equals( typeDescriptor.getJavaType() ) ) {
			// Range aggregations are not supported on text, GeoPoint or boolean fields.
			return ExpectationsAlternative.unsupported( unsupportedExpectations( typeDescriptor ) );
		}

		List<F> ascendingValues = typeDescriptor.getAscendingUniqueTermValues();
		List<F> mainIndexDocumentFieldValues = new ArrayList<>();
		List<F> otherIndexDocumentFieldValues = new ArrayList<>();
		List<List<F>> multiValuedIndexDocumentFieldValues = new ArrayList<>();
		Map<Range<F>, Integer> mainIndexExpected = new LinkedHashMap<>();
		Map<Range<F>, Integer> mainAndOtherIndexExpected = new LinkedHashMap<>();
		Map<Range<F>, Integer> noIndexedValueExpected = new LinkedHashMap<>();
		Map<Range<F>, Integer> multiValuedIndexExpected = new LinkedHashMap<>();

		mainIndexDocumentFieldValues.addAll( ascendingValues.subList( 0, 6 ) );
		// Make sure some documents have the same value; this allows us to check HSEARCH-1929 in particular.
		mainIndexDocumentFieldValues.add( ascendingValues.get( 1 ) );
		mainIndexDocumentFieldValues.add( ascendingValues.get( 2 ) );
		mainIndexDocumentFieldValues.add( ascendingValues.get( 4 ) );
		mainIndexDocumentFieldValues.add( ascendingValues.get( 5 ) );

		otherIndexDocumentFieldValues.add( ascendingValues.get( 1 ) );
		otherIndexDocumentFieldValues.add( ascendingValues.get( 6 ) );

		mainIndexExpected.put( Range.of( ascendingValues.get( 0 ), ascendingValues.get( 2 ) ), 3 );
		mainIndexExpected.put( Range.of( ascendingValues.get( 2 ), ascendingValues.get( 5 ) ), 5 );
		mainIndexExpected.put( Range.atLeast( ascendingValues.get( 5 ) ), 2 );

		mainAndOtherIndexExpected.put( Range.of( ascendingValues.get( 0 ), ascendingValues.get( 2 ) ), 4 );
		mainAndOtherIndexExpected.put( Range.of( ascendingValues.get( 2 ), ascendingValues.get( 5 ) ), 5 );
		mainAndOtherIndexExpected.put( Range.atLeast( ascendingValues.get( 5 ) ), 3 );

		noIndexedValueExpected.put( Range.of( ascendingValues.get( 0 ), ascendingValues.get( 2 ) ), 0 );
		noIndexedValueExpected.put( Range.of( ascendingValues.get( 2 ), ascendingValues.get( 5 ) ), 0 );
		noIndexedValueExpected.put( Range.atLeast( ascendingValues.get( 5 ) ), 0 );

		// Dataset and expectations for the multi-valued index
		// Single-valued documents
		multiValuedIndexDocumentFieldValues.add( Arrays.asList( ascendingValues.get( 0 ) ) );
		multiValuedIndexDocumentFieldValues.add( Arrays.asList( ascendingValues.get( 2 ) ) );
		multiValuedIndexDocumentFieldValues.add( Arrays.asList( ascendingValues.get( 5 ) ) );
		multiValuedIndexDocumentFieldValues.add(
				// Document matching two different buckets
				Arrays.asList( ascendingValues.get( 1 ), ascendingValues.get( 6 ) )
		);
		multiValuedIndexDocumentFieldValues.add(
				// Document matching the same bucket twice
				Arrays.asList( ascendingValues.get( 3 ), ascendingValues.get( 4 ) )
		);

		multiValuedIndexExpected.put( Range.of( ascendingValues.get( 0 ), ascendingValues.get( 2 ) ), 2 );
		multiValuedIndexExpected.put( Range.of( ascendingValues.get( 2 ), ascendingValues.get( 5 ) ), 2 );
		multiValuedIndexExpected.put( Range.atLeast( ascendingValues.get( 5 ) ), 2 );

		return ExpectationsAlternative.supported( new SupportedSingleFieldAggregationExpectations<F>(
				mainIndexDocumentFieldValues,
				otherIndexDocumentFieldValues,
				multiValuedIndexDocumentFieldValues
		) {
			@Override
			public <T> AggregationScenario<Map<Range<T>, Integer>> withFieldType(TypeAssertionHelper<F, T> helper) {
				return doCreate( mainIndexExpected, helper );
			}

			@Override
			public <T> AggregationScenario<Map<Range<T>, Integer>> withFieldTypeOnMainAndOtherIndex(
					TypeAssertionHelper<F, T> helper) {
				return doCreate( mainAndOtherIndexExpected, helper );
			}

			@Override
			public AggregationScenario<?> withoutMatch(FieldTypeDescriptor<F> typeDescriptor) {
				return doCreate( noIndexedValueExpected, TypeAssertionHelper.identity( typeDescriptor ) );
			}

			@Override
			public AggregationScenario<?> onMultiValuedIndex(FieldTypeDescriptor<F> typeDescriptor) {
				return doCreate( multiValuedIndexExpected, TypeAssertionHelper.identity( typeDescriptor ) );
			}

			private <T> AggregationScenario<Map<Range<T>, Integer>> doCreate(Map<Range<F>, Integer> expectedResult,
					TypeAssertionHelper<F, T> helper) {
				return new AggregationScenario<Map<Range<T>, Integer>>() {
					@Override
					public AggregationFinalStep<Map<Range<T>, Integer>> setup(SearchAggregationFactory factory, String fieldPath) {
						return factory.range().field( fieldPath, helper.getJavaClass() )
								.range( helper.create( ascendingValues.get( 0 ) ),
										helper.create( ascendingValues.get( 2 ) ) )
								.range( helper.create( ascendingValues.get( 2 ) ),
										helper.create( ascendingValues.get( 5 ) ) )
								.range( helper.create( ascendingValues.get( 5 ) ),
										null );
					}

					@Override
					public AggregationFinalStep<Map<Range<T>, Integer>> setupWithConverterSetting(SearchAggregationFactory factory,
							String fieldPath, ValueConvert convert) {
						return factory.range().field( fieldPath, helper.getJavaClass(), convert )
								.range( helper.create( ascendingValues.get( 0 ) ),
										helper.create( ascendingValues.get( 2 ) ) )
								.range( helper.create( ascendingValues.get( 2 ) ),
										helper.create( ascendingValues.get( 5 ) ) )
								.range( helper.create( ascendingValues.get( 5 ) ),
										null );
					}

					@Override
					public void check(Map<Range<T>, Integer> aggregationResult) {
						@SuppressWarnings("unchecked")
						Map.Entry<Range<T>, Integer>[] expectedEntries = NormalizationUtils.normalize( expectedResult )
								.entrySet().stream()
								.map( e -> entry(
										e.getKey().map( helper::create ),
										e.getValue()
								) )
								.toArray( Map.Entry[]::new );
						@SuppressWarnings("unchecked")
						Map.Entry<Range<T>, Integer>[] actualEntries = NormalizationUtils.normalize( aggregationResult )
								.entrySet().toArray( new Map.Entry[0] );
						// Don't check the order, this is tested separately
						Assertions.assertThat( actualEntries ).containsOnly( expectedEntries );
					}
				};
			}
		} );
	}

	private <F> UnsupportedSingleFieldAggregationExpectations unsupportedExpectations(FieldTypeDescriptor<F> typeDescriptor) {
		return (factory, fieldPath) -> factory.range().field( fieldPath, typeDescriptor.getJavaType() );
	}

}
