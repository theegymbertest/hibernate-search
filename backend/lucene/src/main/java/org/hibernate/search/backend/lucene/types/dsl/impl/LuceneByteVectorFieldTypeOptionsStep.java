/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.types.dsl.impl;

import org.hibernate.search.backend.lucene.types.codec.impl.AbstractLuceneVectorFieldCodec;
import org.hibernate.search.backend.lucene.types.codec.impl.LuceneByteVectorCodec;
import org.hibernate.search.backend.lucene.types.codec.impl.Storage;
import org.hibernate.search.engine.backend.types.VectorSimilarity;

import org.apache.lucene.codecs.KnnVectorsFormat;

class LuceneByteVectorFieldTypeOptionsStep
		extends AbstractLuceneVectorFieldTypeOptionsStep<LuceneByteVectorFieldTypeOptionsStep, byte[]> {

	LuceneByteVectorFieldTypeOptionsStep(LuceneIndexFieldTypeBuildContext buildContext) {
		super( buildContext, byte[].class );
	}

	@Override
	protected LuceneByteVectorFieldTypeOptionsStep thisAsS() {
		return this;
	}

	@Override
	protected AbstractLuceneVectorFieldCodec<byte[]> createCodec(VectorSimilarity vectorSimilarity, int dimension,
			Storage storage, byte[] indexNullAsValue, KnnVectorsFormat knnVectorsFormat) {
		return new LuceneByteVectorCodec( vectorSimilarity, dimension, storage, indexNullAsValue, knnVectorsFormat );
	}

}
