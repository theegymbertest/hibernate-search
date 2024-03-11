/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.orm.loading.binding.impl;

import java.util.Map;

import org.hibernate.mapping.PersistentClass;
import org.hibernate.search.engine.environment.bean.BeanResolver;
import org.hibernate.search.mapper.orm.model.impl.DocumentIdSourceProperty;
import org.hibernate.search.mapper.pojo.loading.binding.spi.AbstractEntityLoadingBindingContext;
import org.hibernate.search.mapper.pojo.model.spi.PojoBootstrapIntrospector;
import org.hibernate.search.mapper.pojo.model.spi.PojoRawTypeModel;

public class HibernateOrmEntityLoadingBindingContext<E, I> extends AbstractEntityLoadingBindingContext<E, I> {

	final PersistentClass persistentClass;
	final DocumentIdSourceProperty<I> documentIdSourceProperty;

	public HibernateOrmEntityLoadingBindingContext(PojoBootstrapIntrospector introspector,
			PersistentClass persistentClass,
			PojoRawTypeModel<E> entityType, PojoRawTypeModel<I> identifierType,
			BeanResolver beanResolver, Map<String, ?> params) {
		super( introspector, entityType, identifierType, beanResolver, params );
	}
}
