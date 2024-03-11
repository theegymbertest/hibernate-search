/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.orm.loading.binding.impl;

import org.hibernate.mapping.PersistentClass;
import org.hibernate.search.mapper.orm.HibernateOrmExtension;
import org.hibernate.search.mapper.orm.loading.impl.HibernateOrmEntityIdEntityLoadingStrategy;
import org.hibernate.search.mapper.orm.loading.impl.HibernateOrmNonEntityIdPropertyEntityLoadingStrategy;
import org.hibernate.search.mapper.orm.loading.spi.HibernateOrmEntityLoadingStrategy;
import org.hibernate.search.mapper.orm.model.impl.DocumentIdSourceProperty;
import org.hibernate.search.mapper.pojo.loading.binding.EntityLoadingBinder;
import org.hibernate.search.mapper.pojo.loading.binding.EntityLoadingBindingContext;

public class HibernateOrmEntityLoadingBinder implements EntityLoadingBinder {

	public HibernateOrmEntityLoadingBinder() {
	}

	@Override
	public void bind(EntityLoadingBindingContext context) {
		var ormContext = (HibernateOrmEntityLoadingBindingContext<?, ?>) context;
		bind( context, createLoadingStrategy( ormContext.persistentClass, ormContext.documentIdSourceProperty ) );
	}

	private <E, I> void bind(EntityLoadingBindingContext context, HibernateOrmEntityLoadingStrategy<E, I> strategy) {
		if ( strategy != null ) {
			context.selectionLoadingStrategy( strategy.rootEntityClass(), strategy );
			context.massLoadingStrategy( strategy.rootEntityClass(), strategy );
		}
	}

	public <I> HibernateOrmEntityLoadingStrategy<?, ?> createLoadingStrategy(
			PersistentClass persistentClass, DocumentIdSourceProperty<I> documentIdSourceProperty) {
		if ( documentIdSourceProperty != null ) {
			var idProperty = persistentClass.getIdentifierProperty();
			if ( idProperty != null && documentIdSourceProperty.name.equals( idProperty.getName() ) ) {
				return HibernateOrmEntityIdEntityLoadingStrategy
						.create( persistentClass );
			}
			else {
				// The entity ID is not the property used to generate the document ID
				// We need to use a criteria query to load entities from the document IDs
				return HibernateOrmNonEntityIdPropertyEntityLoadingStrategy
						.create( persistentClass, documentIdSourceProperty );
			}
		}
		else {
			// No loading. Can only happen for contained types, which may not be loadable.
			return null;
		}
	}
}
