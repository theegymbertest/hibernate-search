/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.orm.envers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.hibernate.search.util.impl.integrationtest.common.stub.backend.StubBackendUtils.reference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.SessionFactory;
import org.hibernate.envers.Audited;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.search.engine.backend.analysis.AnalyzerNames;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.cfg.HibernateOrmMapperSettings;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmMappingConfigurationContext;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmSearchMappingConfigurer;
import org.hibernate.search.mapper.orm.scope.SearchScope;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.mapper.pojo.bridge.IdentifierBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.IdentifierBridgeFromDocumentIdentifierContext;
import org.hibernate.search.mapper.pojo.bridge.runtime.IdentifierBridgeToDocumentIdentifierContext;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.TypeMappingStep;
import org.hibernate.search.util.impl.integrationtest.common.rule.BackendMock;
import org.hibernate.search.util.impl.integrationtest.common.rule.StubSearchWorkBehavior;
import org.hibernate.search.util.impl.integrationtest.mapper.orm.OrmSetupHelper;
import org.hibernate.search.util.impl.integrationtest.mapper.orm.OrmUtils;
import org.hibernate.search.util.impl.test.annotation.TestForIssue;
import org.hibernate.search.util.impl.test.rule.StaticCounters;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.assertj.core.api.InstanceOfAssertFactories;

@TestForIssue(jiraKey = { "HSEARCH-TODO" })
public class EnversIndexedAuditEntityIT {

	@Rule
	public BackendMock backendMock = new BackendMock();

	@Rule
	public OrmSetupHelper ormSetupHelper = OrmSetupHelper.withBackendMock( backendMock );

	@Rule
	public StaticCounters counters = new StaticCounters();

	private SessionFactory sessionFactory;

	@Before
	public void setup() {
		backendMock.expectSchema( MyAuditedEntity.AUDIT_ENTITY_INDEX_NAME, b -> b
				.field( "text", String.class, f -> f.analyzerName( AnalyzerNames.DEFAULT ) )
		);

		sessionFactory = ormSetupHelper.start()
				.withProperty(
						HibernateOrmMapperSettings.MAPPING_CONFIGURER, new HibernateOrmSearchMappingConfigurer() {
							@Override
							public void configure(HibernateOrmMappingConfigurationContext context) {
								TypeMappingStep auditType = context.programmaticMapping()
										.type( MyAuditedEntity.AUDIT_ENTITY_NAME );
								auditType.indexed().index( MyAuditedEntity.AUDIT_ENTITY_INDEX_NAME );
								auditType.property( "originalId" )
										.documentId()
										.identifierBridge( AuditEntityIdentifierBridge.ofSinglePropertyId(
												"id", Integer.class,
												new IdentifierBridge<Integer>() {
													@Override
													public String toDocumentIdentifier(Integer propertyValue,
															IdentifierBridgeToDocumentIdentifierContext context) {
														return propertyValue.toString();
													}

													@Override
													public Integer fromDocumentIdentifier(String documentIdentifier,
															IdentifierBridgeFromDocumentIdentifierContext context) {
														return Integer.parseInt( documentIdentifier );
													}
												}
										) );
								auditType.property( "text" ).fullTextField();
							}
						} )
				.setup( MyAuditedEntity.class );
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void test() {
		// Initial insert
		OrmUtils.withinTransaction( sessionFactory, session -> {
			MyAuditedEntity entity = new MyAuditedEntity();
			entity.setId( 1 );
			entity.setText( "initial" );

			session.persist( entity );

			backendMock.expectWorks( MyAuditedEntity.AUDIT_ENTITY_INDEX_NAME )
					.add( "1_1", b -> b
							.field( "text", "initial" )
					)
					.createdThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
		checkSearchLoading( 1, 1, "initial" );

		// Update the entity
		OrmUtils.withinTransaction( sessionFactory, session -> {
			MyAuditedEntity entity = session.getReference( MyAuditedEntity.class, 1 );
			entity.setText( "updated" );

			backendMock.expectWorks( MyAuditedEntity.AUDIT_ENTITY_INDEX_NAME )
					.add( "1_2", b -> b
							.field( "text", "updated" )
					)
					.createdThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
		checkSearchLoading( 1, 2, "updated" );

		// Delete the entity
		OrmUtils.withinTransaction( sessionFactory, session -> {
			MyAuditedEntity entity = session.getReference( MyAuditedEntity.class, 1 );
			session.delete( entity );

			backendMock.expectWorks( MyAuditedEntity.AUDIT_ENTITY_INDEX_NAME )
					.add( "1_3", b -> b
							.field( "text", null )
					)
					.createdThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
		checkSearchLoading( 1, 3, null );
	}

	private void checkSearchLoading(int entityId, int lastRevId, String latestText) {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			backendMock.expectSearchObjects(
					Arrays.asList( MyAuditedEntity.AUDIT_ENTITY_INDEX_NAME ),
					b -> b.limit( 2 ), // fetchSingleHit() (see below) sets the limit to 2 to check if there really is a single hit
					StubSearchWorkBehavior.of(
							1L,
							reference( MyAuditedEntity.AUDIT_ENTITY_NAME, entityId + "_" + lastRevId )
					)
			);
			SearchSession searchSession = Search.session( session );
			SearchScope<Map> scope = searchSession.scope( Map.class, MyAuditedEntity.AUDIT_ENTITY_NAME );
			Optional<Map> loadedEntity = searchSession.search( scope )
					.where( f -> f.matchAll() )
					.fetchSingleHit();
			assertThat( loadedEntity )
					.as( "loaded audit entity after search" )
					.get()
					.asInstanceOf( InstanceOfAssertFactories.MAP )
					.contains( entry( "text", latestText ) );
		} );
	}

	@Entity(name = MyAuditedEntity.NAME)
	@Audited(withModifiedFlag = true)
	public static final class MyAuditedEntity {

		static final String NAME = "audited";
		static final String AUDIT_ENTITY_NAME = MyAuditedEntity.class.getName() + "_AUD";
		static final String AUDIT_ENTITY_INDEX_NAME = "audited_audit";

		@Id
		private Integer id;

		@Basic
		@GenericField
		private String text;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}

	private static final class AuditEntityIdentifierBridge<T> implements IdentifierBridge<Map> {
		private static final char SEPARATOR = '_';

		public static <T> AuditEntityIdentifierBridge<T> ofSinglePropertyId(String idPropertyName, Class<T> entityIdType,
				IdentifierBridge<T> entityIdBridge) {
			return new AuditEntityIdentifierBridge<>( idPropertyName, entityIdType, entityIdBridge );
		}

		private final String idPropertyName;
		private final Class<T> entityIdType;
		private final IdentifierBridge<T> entityIdBridge;

		private AuditEntityIdentifierBridge(String idPropertyName, Class<T> entityIdType, IdentifierBridge<T> entityIdBridge) {
			this.idPropertyName = idPropertyName;
			this.entityIdType = entityIdType;
			this.entityIdBridge = entityIdBridge;
		}

		@Override
		public String toDocumentIdentifier(Map propertyValue,
				IdentifierBridgeToDocumentIdentifierContext context) {
			T entityId = entityIdType.cast( propertyValue.get( idPropertyName ) );
			String entityIdAsString = entityIdBridge.toDocumentIdentifier( entityId, context );

			DefaultRevisionEntity rev = (DefaultRevisionEntity) propertyValue.get( "REV" );

			return entityIdAsString + SEPARATOR + rev.getId();
		}

		@Override
		public Map fromDocumentIdentifier(String documentIdentifier,
				IdentifierBridgeFromDocumentIdentifierContext context) {
			int separatorIndex = documentIdentifier.lastIndexOf( SEPARATOR );

			T entityId = entityIdBridge.fromDocumentIdentifier( documentIdentifier.substring( 0, separatorIndex ),
					context );

			DefaultRevisionEntity rev = new DefaultRevisionEntity();
			int revId = Integer.parseInt( documentIdentifier.substring( separatorIndex + 1 ) );
			rev.setId( revId );

			Map map = new HashMap();
			map.put( "REV", rev );
			map.put( idPropertyName, entityId );
			return map;
		}
	}
}
