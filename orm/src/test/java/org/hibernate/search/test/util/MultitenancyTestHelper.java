/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.util;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.resource.transaction.spi.DdlTransactionIsolator;
import org.hibernate.search.util.logging.impl.Log;
import org.hibernate.search.util.logging.impl.LoggerFactory;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.testing.env.ConnectionProviderBuilder;
import org.hibernate.tool.schema.internal.HibernateSchemaManagementTool;
import org.hibernate.tool.schema.internal.SchemaCreatorImpl;
import org.hibernate.tool.schema.internal.SchemaDropperImpl;
import org.hibernate.tool.schema.internal.exec.GenerationTargetToDatabase;
import org.hibernate.tool.schema.internal.exec.JdbcContext;

/**
 * Utility to help setting up a test SessionFactory which uses multi-tenancy based
 * on multiple databases.
 *
 * @author Sanne Grinovero
 * @since 5.4
 */
public class MultitenancyTestHelper implements Closeable {

	private static final Log LOG = LoggerFactory.make();

	private final Set<String> tenantIds;
	private final boolean multitenancyEnabled;
	private final AbstractMultiTenantConnectionProvider multiTenantConnectionProvider;
	private final Map<String,DriverManagerConnectionProviderImpl> tenantSpecificConnectionProviders = new HashMap<>();
	private final List<Connection> tenantSpecificConnections = new ArrayList<>();

	public MultitenancyTestHelper(Set<String> tenantIds) {
		this.tenantIds = tenantIds;
		this.multitenancyEnabled = tenantIds != null && tenantIds.size() != 0;
		if ( multitenancyEnabled ) {
			multiTenantConnectionProvider = buildMultiTenantConnectionProvider();
		}
		else {
			multiTenantConnectionProvider = null;
		}
	}

	public void enableIfNeeded(StandardServiceRegistryBuilder registryBuilder) {
		registryBuilder.addService( MultiTenantConnectionProvider.class, multiTenantConnectionProvider );
	}

	private AbstractMultiTenantConnectionProvider buildMultiTenantConnectionProvider() {
		for ( String tenantId : tenantIds ) {
			DriverManagerConnectionProviderImpl connectionProvider = ConnectionProviderBuilder.buildConnectionProvider( tenantId );
			tenantSpecificConnectionProviders.put( tenantId, connectionProvider );
		}
		return new AbstractMultiTenantConnectionProvider() {
			@Override
			protected ConnectionProvider getAnyConnectionProvider() {
				//blatantly assuming there's at least one entry:
				return tenantSpecificConnectionProviders.entrySet().iterator().next().getValue();
			}

			@Override
			protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
				DriverManagerConnectionProviderImpl connectionProviderImpl = tenantSpecificConnectionProviders.get( tenantIdentifier );
				if ( connectionProviderImpl == null ) {
					throw new HibernateException( "Unknown tenant identifier" );
				}
				return connectionProviderImpl;
			}
		};
	}

	@Override
	public void close() {
		for ( Connection connection : tenantSpecificConnections ) {
			try {
				connection.close();
			}
			catch (SQLException e) {
				LOG.error( "Error while closing a tenant-specific connection", e );
			}
		}
		for ( DriverManagerConnectionProviderImpl connectionProvider : tenantSpecificConnectionProviders.values() ) {
			connectionProvider.stop();
		}
	}

	public void exportSchema(ServiceRegistryImplementor serviceRegistry, Metadata metadata, Map<String, Object> settings) {
		HibernateSchemaManagementTool tool = new HibernateSchemaManagementTool();
		tool.injectServices( serviceRegistry );
		final GenerationTargetToDatabase[] databaseTargets = createSchemaTargets( tool, settings );
		new SchemaDropperImpl( serviceRegistry ).doDrop(
				metadata,
				serviceRegistry,
				settings,
				true,
				databaseTargets
			);
		new SchemaCreatorImpl( serviceRegistry ).doCreation(
				metadata,
				serviceRegistry,
				settings,
				true,
				databaseTargets
			);
	}

	private GenerationTargetToDatabase[] createSchemaTargets(HibernateSchemaManagementTool tool, Map<String, Object> settings) {
		GenerationTargetToDatabase[] targets = new GenerationTargetToDatabase[tenantSpecificConnectionProviders.size()];
		int index = 0;

		for ( Entry<String, DriverManagerConnectionProviderImpl> entry : tenantSpecificConnectionProviders.entrySet() ) {
			ConnectionProvider connectionProvider = entry.getValue();
			Connection connection;
			try {
				connection = connectionProvider.getConnection();
			}
			catch (SQLException e) {
				throw new IllegalStateException( "Unexpected error when creating tenant-specific connections", e );
			}
			tenantSpecificConnections.add( connection );

			Map<String, Object> tenantSpecificSettings = new LinkedHashMap<>( settings );
			tenantSpecificSettings.put( AvailableSettings.HBM2DDL_CONNECTION, connection );

			JdbcContext jdbcContext = tool.resolveJdbcContext( tenantSpecificSettings );
			DdlTransactionIsolator ddlTransactionIsolator = tool.getDdlTransactionIsolator( jdbcContext );
			targets[index] = new GenerationTargetToDatabase( ddlTransactionIsolator );
			index++;
		}
		return targets;
	}

	public void forceConfigurationSettings(Map<String, Object> settings) {
		if ( multitenancyEnabled ) {
			settings.remove( org.hibernate.cfg.Environment.HBM2DDL_AUTO );
			settings.put( AvailableSettings.MULTI_TENANT, MultiTenancyStrategy.DATABASE.name() );
		}
	}

}
