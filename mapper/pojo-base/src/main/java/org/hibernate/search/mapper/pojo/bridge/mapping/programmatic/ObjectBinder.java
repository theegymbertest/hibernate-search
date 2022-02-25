/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.bridge.mapping.programmatic;

import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.mapper.pojo.bridge.ObjectBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.ObjectBindingContext;
import org.hibernate.search.mapper.pojo.bridge.runtime.ObjectBridgeWriteContext;

/**
 * A binder from a Java object to an object element in the index (either the index root or an object field).
 * <p>
 * This binder takes advantage of provided metadata
 * to pick, configure and create a {@link ObjectBridge}.
 *
 * @see ObjectBridge
 */
public interface ObjectBinder {

	/**
	 * Binds a Java object type to an object element in the index.
	 * <p>
	 * The context passed in parameter provides various information about the Java object type being bound.
	 * Implementations are expected to take advantage of that information
	 * and to call one of the {@code bridge(...)} methods on the context
	 * to set the bridge.
	 * <p>
	 * Implementations are also expected to declare dependencies, i.e. the properties
	 * that will later be used in the
	 * {@link ObjectBridge#write(DocumentElement, Object, ObjectBridgeWriteContext)} method,
	 * using {@link ObjectBindingContext#dependencies()}.
	 * Failing that, Hibernate Search will not reindex entities properly when an indexed property is modified.
	 *
	 * @param context A context object providing information about the object being bound,
	 * and expecting a call to one of its {@code bridge(...)} methods.
	 */
	void bind(ObjectBindingContext context);

}
