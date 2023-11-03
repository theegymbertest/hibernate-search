/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.types.dsl.impl;

import java.lang.invoke.MethodHandles;

import org.hibernate.search.backend.lucene.logging.impl.Log;
import org.hibernate.search.backend.lucene.lowlevel.codec.impl.HibernateSearchKnnVectorsFormat;
import org.hibernate.search.backend.lucene.types.codec.impl.AbstractLuceneVectorFieldCodec;
import org.hibernate.search.backend.lucene.types.codec.impl.Storage;
import org.hibernate.search.backend.lucene.types.dsl.LuceneVectorFieldTypeOptionsStep;
import org.hibernate.search.backend.lucene.types.impl.LuceneIndexValueFieldType;
import org.hibernate.search.backend.lucene.types.predicate.impl.LuceneExistsPredicate;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.engine.backend.types.VectorSimilarity;
import org.hibernate.search.engine.search.predicate.spi.PredicateTypeKeys;
import org.hibernate.search.util.common.AssertionFailure;
import org.hibernate.search.util.common.logging.impl.LoggerFactory;

import org.apache.lucene.codecs.KnnVectorsFormat;

/**
 * @param <S> The "self" type (the actual exposed type of this step).
 * @param <F> The type of field values.
 */
abstract class AbstractLuceneVectorFieldTypeOptionsStep<S extends AbstractLuceneVectorFieldTypeOptionsStep<?, F>, F>
		extends AbstractLuceneIndexFieldTypeOptionsStep<S, F>
		implements LuceneVectorFieldTypeOptionsStep<S, F> {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );
	private static final int MAX_BEAM_WIDTH = 3200;
	private static final int MAX_MAX_CONNECTIONS = 512;

	protected VectorSimilarity vectorSimilarity = VectorSimilarity.DEFAULT;
	protected int dimension;
	protected Integer beamWidth = MAX_MAX_CONNECTIONS;
	protected Integer maxConnections = 16;
	private Projectable projectable = Projectable.DEFAULT;
	private F indexNullAsValue = null;

	AbstractLuceneVectorFieldTypeOptionsStep(LuceneIndexFieldTypeBuildContext buildContext, Class<F> valueType, int dimension) {
		super( buildContext, valueType );
		this.dimension = dimension;

		if ( this.dimension < 1 ) {
			throw log.vectorPropertyUnsupportedValue( "dimension", this.dimension, MAX_BEAM_WIDTH );
		}
		if ( this.dimension > HibernateSearchKnnVectorsFormat.DEFAULT_MAX_DIMENSIONS ) {
			log.vectorPropertyOutOfRecommendedRange( "dimension", this.dimension, 1,
					HibernateSearchKnnVectorsFormat.DEFAULT_MAX_DIMENSIONS );
		}
	}

	@Override
	public S projectable(Projectable projectable) {
		this.projectable = projectable;
		return thisAsS();
	}

	@Override
	public S vectorSimilarity(VectorSimilarity vectorSimilarity) {
		this.vectorSimilarity = vectorSimilarity;
		return thisAsS();
	}

	@Override
	public S beamWidth(int beamWidth) {
		if ( beamWidth < 1 || beamWidth > MAX_BEAM_WIDTH ) {
			throw log.vectorPropertyUnsupportedValue( "beamWidth", beamWidth, MAX_BEAM_WIDTH );
		}
		this.beamWidth = beamWidth;
		return thisAsS();
	}

	@Override
	public S maxConnections(int maxConnections) {
		if ( maxConnections < 1 || maxConnections > MAX_MAX_CONNECTIONS ) {
			throw log.vectorPropertyUnsupportedValue( "maxConnections", maxConnections, MAX_MAX_CONNECTIONS );
		}
		if ( maxConnections > 100 ) {
			log.vectorPropertyOutOfRecommendedRange( "maxConnections", maxConnections, 2, 100 );
		}
		this.maxConnections = maxConnections;
		return thisAsS();
	}

	@Override
	public S indexNullAs(F indexNullAsValue) {
		this.indexNullAsValue = indexNullAsValue;
		return thisAsS();
	}

	@Override
	public LuceneIndexValueFieldType<F> toIndexFieldType() {
		VectorSimilarity resolvedVectorSimilarity = resolveDefault( vectorSimilarity );
		boolean resolvedProjectable = resolveDefault( projectable );

		Storage storage = resolvedProjectable ? Storage.ENABLED : Storage.DISABLED;

		AbstractLuceneVectorFieldCodec<F, ?> codec = createCodec( resolvedVectorSimilarity, dimension, storage,
				indexNullAsValue, new HibernateSearchKnnVectorsFormat( maxConnections, beamWidth ) );
		builder.codec( codec );
		builder.queryElementFactory( PredicateTypeKeys.EXISTS, new LuceneExistsPredicate.DocValuesOrNormsBasedFactory<>() );

		return builder.build();
	}

	protected abstract AbstractLuceneVectorFieldCodec<F, ?> createCodec(VectorSimilarity vectorSimilarity, int dimension,
			Storage storage, F indexNullAsValue, KnnVectorsFormat knnVectorsFormat);

	protected static VectorSimilarity resolveDefault(VectorSimilarity vectorSimilarity) {
		switch ( vectorSimilarity ) {
			case DEFAULT:
			case L2:
				return VectorSimilarity.L2;
			case INNER_PRODUCT:
				return VectorSimilarity.INNER_PRODUCT;
			case COSINE:
				return VectorSimilarity.COSINE;
			default:
				throw new AssertionFailure( "Unexpected value for Similarity: " + vectorSimilarity );
		}
	}

	protected static boolean resolveDefault(Projectable projectable) {
		switch ( projectable ) {
			case DEFAULT:
			case NO:
				return false;
			case YES:
				return true;
			default:
				throw new AssertionFailure( "Unexpected value for Projectable: " + projectable );
		}
	}

}
