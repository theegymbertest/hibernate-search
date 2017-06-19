/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.integration.wildfly.tika.detector;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;


/**
 * @author Yoann Rodiere
 */
public class CustomDetector implements Detector {

	public static final MediaType CUSTOM_MEDIA_TYPE = MediaType.text( "custom" );

	@Override
	public MediaType detect(InputStream input, Metadata metadata) throws IOException {
		return CUSTOM_MEDIA_TYPE;
	}

}
