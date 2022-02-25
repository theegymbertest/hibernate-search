/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.bridge;

import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.mapper.pojo.bridge.binding.ObjectBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.ObjectBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.ObjectBridgeWriteContext;
import org.hibernate.search.mapper.pojo.bridge.runtime.ObjectBridgeWriteContextExtension;

/**
 * A bridge between a Java object and an object element in the index (either the index root or an object field).
 * <p>
 * The {@code ObjectBridge} interface is a more powerful version of {@link ValueBridge}
 * that applies to a whole object instead of a single, atomic value,
 * and can contribute more than one index field, in particular.
 *
 * @param <T> The type on the POJO side of the bridge.
 */
public interface ObjectBridge<T> extends AutoCloseable {

	/**
	 * Write to fields in the given {@link DocumentElement},
	 * using the given {@code bridgedElement} as input and transforming it as necessary.
	 * <p>
	 * Writing to the {@link DocumentElement} should be done using
	 * {@link org.hibernate.search.engine.backend.document.IndexFieldReference}s retrieved
	 * when the bridge was {@link ObjectBinder#bind(ObjectBindingContext) bound}.
	 * <p>
	 * <strong>Warning:</strong> Reading from {@code bridgedElement} should be done with care.
	 * Any read that was not declared during {@link ObjectBinder#bind(ObjectBindingContext) binding}
	 * (by declaring dependencies using {@link ObjectBindingContext#dependencies()}
	 * or (advanced use) creating an accessor using {@link ObjectBindingContext#bridgedElement()})
	 * may lead to out-of-sync indexes,
	 * because Hibernate Search will consider the read property irrelevant to indexing
	 * and will not reindex entities when that property changes.
	 *
	 * @param target The {@link DocumentElement} to write to.
	 * @param bridgedElement The element this bridge is applied to, from which data should be read.
	 * @param context A context that can be
	 * {@link ObjectBridgeWriteContext#extension(ObjectBridgeWriteContextExtension) extended}
	 * to a more useful object, giving access to such things as a Hibernate ORM Session (if using the Hibernate ORM mapper).
	 */
	void write(DocumentElement target, T bridgedElement, ObjectBridgeWriteContext context);

	/**
	 * Close any resource before the bridge is discarded.
	 */
	@Override
	default void close() {
	}

}
