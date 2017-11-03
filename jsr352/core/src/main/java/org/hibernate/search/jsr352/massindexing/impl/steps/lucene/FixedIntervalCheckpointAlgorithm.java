/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.jsr352.massindexing.impl.steps.lucene;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.AbstractCheckpointAlgorithm;
import javax.inject.Inject;

import org.hibernate.search.jsr352.massindexing.impl.util.MassIndexingPartitionProperties;
import org.hibernate.search.jsr352.massindexing.impl.util.SerializationUtil;

public class FixedIntervalCheckpointAlgorithm extends AbstractCheckpointAlgorithm {

	@Inject
	@BatchProperty(name = MassIndexingPartitionProperties.CHECKPOINT_INTERVAL)
	private String serializedCheckpointInterval;

	private Integer checkpointInterval;

	private int isReadyCalls = 0;

	@Override
	public boolean isReadyToCheckpoint() throws Exception {
		lazyInit();

		++isReadyCalls;
		if ( isReadyCalls >= checkpointInterval ) {
			isReadyCalls = 0;
			return true;
		}
		else {
			return false;
		}
	}

	private void lazyInit() {
		if ( checkpointInterval == null ) {
			checkpointInterval = SerializationUtil.parseIntegerParameter(
					MassIndexingPartitionProperties.CHECKPOINT_INTERVAL, serializedCheckpointInterval );
		}
	}
}
