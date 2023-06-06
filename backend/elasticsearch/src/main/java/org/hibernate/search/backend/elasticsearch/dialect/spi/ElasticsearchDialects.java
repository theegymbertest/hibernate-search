/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.dialect.spi;

import java.util.Optional;

import org.hibernate.search.backend.elasticsearch.ElasticsearchVersion;
import org.hibernate.search.backend.elasticsearch.dialect.impl.ElasticsearchDialectFactory;

/**
 * Utils for integrations that require advanced checks on Elasticsearch versions,
 * i.e. for two-phase bootstrap like in Quarkus.
 */
public final class ElasticsearchDialects {

	private ElasticsearchDialects() {
	}

	public static boolean isPreciseEnoughForBootstrap(Optional<ElasticsearchVersion> versionOptional) {
		return ElasticsearchDialectFactory.isPreciseEnoughForModelDialect(versionOptional);
	}

	public static boolean isPreciseEnoughForStart(Optional<ElasticsearchVersion> versionOptional) {
		return ElasticsearchDialectFactory.isPreciseEnoughForProtocolDialect(versionOptional);
	}

	public static boolean isVersionCheckImpossible(Optional<ElasticsearchVersion> versionOptional) {
		return ElasticsearchDialectFactory.isVersionCheckImpossible( versionOptional );
	}

}
