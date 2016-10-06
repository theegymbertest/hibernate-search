/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

package org.hibernate.search.bridge.builtin.impl;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.StringBridge;
import org.hibernate.search.bridge.util.impl.String2FieldBridgeAdaptor;
import org.hibernate.search.engine.impl.nullencoding.KeywordBasedNullCodec;
import org.hibernate.search.engine.impl.nullencoding.NullMarkerCodec;

/**
 * @author Davide D'Alto
 */
public class NullEncodingFieldBridge implements FieldBridge, StringBridge, NullEncodingBridgeWrapper<StringBridge> {

	private final FieldBridge fieldBridge;
	private final StringBridge stringBridge;
	private final NullMarkerCodec nullTokenCodec;

	public NullEncodingFieldBridge(StringBridge bridge, String nullMarker) {
		this( bridge, new KeywordBasedNullCodec( nullMarker ) );
	}

	public NullEncodingFieldBridge(StringBridge bridge, NullMarkerCodec nullTokenCodec) {
		this.fieldBridge = bridge instanceof FieldBridge ?
				(FieldBridge) bridge : new String2FieldBridgeAdaptor( bridge );
		this.stringBridge = bridge;
		this.nullTokenCodec = nullTokenCodec;
	}

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if ( value == null ) {
			nullTokenCodec.encodeNullValue( name, document, luceneOptions );
		}
		else {
			fieldBridge.set( name, value, document, luceneOptions );
		}
	}

	@Override
	public String objectToString(Object object) {
		if ( object == null ) {
			return nullTokenCodec.nullRepresentedAsString();
		}
		return stringBridge.objectToString( object );
	}

	@Override
	public Query buildNullQuery(String fieldName) {
		return nullTokenCodec.createNullMatchingQuery( fieldName );
	}

	@Override
	public StringBridge unwrap() {
		return stringBridge;
	}

}
