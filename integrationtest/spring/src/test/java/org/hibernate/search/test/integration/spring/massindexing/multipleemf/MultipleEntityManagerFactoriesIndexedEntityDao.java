/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.integration.spring.massindexing.multipleemf;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.search.test.integration.spring.massindexing.model.AbstractIndexedEntityDao;

import org.springframework.stereotype.Repository;

@Repository
public class MultipleEntityManagerFactoriesIndexedEntityDao extends AbstractIndexedEntityDao {

	@PersistenceContext(unitName = MultipleEntityManagerFactoriesConfiguration.PRIMARY_PERSISTENCE_UNIT_NAME)
	private EntityManager entityManager;

	@Override
	protected EntityManager getEntityManager() {
		return entityManager;
	}
}
