/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.integration.wildfly.tika.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.hibernate.search.test.integration.wildfly.tika.detector.CustomDetector;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * @author Yoann Rodiere
 */
public class CustomParser implements Parser {

	// Parsing is stubbed.
	public static final String PARSED_CONTENT = "somecontent";

	@Override
	public Set<MediaType> getSupportedTypes(ParseContext context) {
		return Collections.singleton( CustomDetector.CUSTOM_MEDIA_TYPE );
	}

	@Override
	public void parse(InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context) throws IOException, SAXException, TikaException {
		handler.startDocument();
		char[] content = PARSED_CONTENT.toCharArray();
		handler.characters( content, 0, content.length );
		handler.endDocument();
	}

}
