/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.pojo.mapping.definition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.search.engine.backend.types.ObjectStructure;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.engine.backend.types.Searchable;
import org.hibernate.search.integrationtest.mapper.pojo.smoke.AnnotationMappingSmokeIT;
import org.hibernate.search.integrationtest.mapper.pojo.smoke.ProgrammaticMappingSmokeIT;
import org.hibernate.search.integrationtest.mapper.pojo.testsupport.util.StartupStubBridge;
import org.hibernate.search.integrationtest.mapper.pojo.testsupport.util.rule.JavaBeanMappingSetupHelper;
import org.hibernate.search.mapper.javabean.mapping.SearchMapping;
import org.hibernate.search.mapper.javabean.session.SearchSession;
import org.hibernate.search.mapper.pojo.bridge.IdentifierBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.IdentifierBindingContext;
import org.hibernate.search.mapper.pojo.bridge.builtin.spatial.impl.GeoPointBridge;
import org.hibernate.search.mapper.pojo.bridge.builtin.spatial.impl.LatitudeMarker;
import org.hibernate.search.mapper.pojo.bridge.builtin.spatial.impl.LongitudeMarker;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.IdentifierBinderRef;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.IdentifierBridgeRef;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.IdentifierBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.IdentifierBridgeFromDocumentIdentifierContext;
import org.hibernate.search.mapper.pojo.bridge.runtime.IdentifierBridgeToDocumentIdentifierContext;
import org.hibernate.search.mapper.pojo.common.annotation.Param;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.AssociationInverseSide;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ObjectPath;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.PropertyValue;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.TypeMappingStep;
import org.hibernate.search.mapper.pojo.model.path.PojoModelPath;
import org.hibernate.search.util.common.SearchException;
import org.hibernate.search.util.common.impl.CollectionHelper;
import org.hibernate.search.util.impl.integrationtest.common.FailureReportUtils;
import org.hibernate.search.util.impl.integrationtest.common.rule.BackendMock;
import org.hibernate.search.util.impl.integrationtest.common.stub.backend.document.StubDocumentNode;
import org.hibernate.search.util.impl.test.annotation.TestForIssue;
import org.hibernate.search.util.impl.test.rule.StaticCounters;

import org.junit.Rule;
import org.junit.Test;

/**
 * Test common use cases of the {@code @ObjectField} annotation.
 * <p>
 * Does not test all the corner cases of filtering when using {@link ObjectField#includePaths()} and
 * {@link ObjectField#includeDepth()}, which are tested in a unit test in the engine module
 * (the test is named {@code ConfiguredIndexSchemaNestingContextTest} at the time of this writing).
 * <p>
 * Does not test uses of container value extractors (for now). Some of them are tested in
 * {@link AnnotationMappingSmokeIT} and {@link ProgrammaticMappingSmokeIT}.
 */
@SuppressWarnings("unused")
public class ObjectFieldBaseIT {

	private static final String INDEX_NAME = "IndexName";

	@Rule
	public BackendMock backendMock = new BackendMock();

	@Rule
	public JavaBeanMappingSetupHelper setupHelper = JavaBeanMappingSetupHelper.withBackendMock( MethodHandles.lookup(), backendMock );

	@Rule
	public StaticCounters counters = new StaticCounters();

	@Test
	public void defaultAttributes() {
		class ObjectFieldLevel2 {
			@GenericField
			String level2Property;
		}
		class ObjectFieldLevel1 {
			@GenericField
			String level1Property;
			@ObjectField
			ObjectFieldLevel2 level2;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, String level1Value, String level2Value) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.level1Property = level1Value;
				this.level1.level2 = new ObjectFieldLevel2();
				this.level1.level2.level2Property = level2Value;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						.field( "level1Property", String.class )
						.objectField( "level2", b3 -> b3
								.field( "level2Property", String.class )
						)
				)
		);
		SearchMapping mapping = setupHelper.start()
				.withAnnotatedEntityTypes( IndexedEntity.class )
				.withAnnotatedTypes( ObjectFieldLevel1.class, ObjectFieldLevel2.class )
				.setup();
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, "level1Value", "level2Value" ),
				document -> document.objectField( "level1", b2 -> b2
						.field( "level1Property", "level1Value" )
						.objectField( "level2", b3 -> b3
								.field( "level2Property", "level2Value" )
						)
				)
		);
	}

	@Test
	public void name() {
		class ObjectFieldLevel1 {
			@GenericField
			String level1Property;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(name = "explicitName")
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, String value) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.level1Property = value;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "explicitName", b2 -> b2
						.field( "level1Property", String.class )
				)
		);
		SearchMapping mapping = setupHelper.start()
				.withAnnotatedEntityTypes( IndexedEntity.class )
				.withAnnotatedTypes( ObjectFieldLevel1.class )
				.setup();
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, "level1Value" ),
				document -> document
						.objectField( "explicitName", b2 -> b2
								.field( "level1Property", "level1Value" )
				)
		);
	}

	@Test
	public void name_invalid_dot() {
		class ObjectFieldLevel1 {
			@GenericField
			String level1Property;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(name = "invalid.withdot")
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, String value) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.level1Property = value;
			}
		}

		assertThatThrownBy(
				() -> setupHelper.start().setup( IndexedEntity.class )
		)
				.isInstanceOf( SearchException.class )
				.hasMessageMatching( FailureReportUtils.buildFailureReportPattern()
						.typeContext( IndexedEntity.class.getName() )
						.pathContext( ".level1" )
						.annotationContextAnyParameters( ObjectField.class )
						.failure( "Invalid index field name 'invalid.withdot': field names cannot contain a dot ('.')" )
						.build()
				);
	}

	@Test
	public void repeatedObjectField() {
		class Embedded {
			String forDefault;
			String flat;
			String nest;
			String common;

			@GenericField(name = "default")
			public String getForDefault() {
				return forDefault;
			}
			@GenericField
			public String getFlat() {
				return flat;
			}
			@GenericField
			public String getNest() {
				return nest;
			}
			@GenericField
			public String getCommon() {
				return common;
			}
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(name = "default", includePaths = {"default", "common"})
			@ObjectField(name = "flat", includePaths = {"flat", "common"},
					structure = ObjectStructure.FLATTENED)
			@ObjectField(name = "nest", includePaths = {"nest", "common"},
					structure = ObjectStructure.NESTED)
			Embedded embedded;
			public IndexedEntity(int id, String value) {
				this.id = id;
				this.embedded = new Embedded();
				this.embedded.forDefault = value;
				this.embedded.flat = value;
				this.embedded.nest = value;
				this.embedded.common = value;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> {
				b.objectField( "default", b2 -> {
					b2.field( "default", String.class );
					b2.field( "common", String.class );
				} );
				b.objectField( "flat", b2 -> {
					b2.objectStructure( ObjectStructure.FLATTENED );
					b2.field( "flat", String.class );
					b2.field( "common", String.class );
				} );
				b.objectField( "nest", b2 -> {
					b2.objectStructure( ObjectStructure.NESTED );
					b2.field( "nest", String.class );
					b2.field( "common", String.class );
				} );
			}
		);
		SearchMapping mapping = setupHelper.start()
				.withAnnotatedEntityTypes( IndexedEntity.class )
				.withAnnotatedTypes( Embedded.class )
				.setup();
		backendMock.verifyExpectationsMet();
	}

	/**
	 * Check @ObjectField on a multi-valued property
	 * results in the corresponding object field being automatically marked as multi-valued
	 * (and not its own fields).
	 */
	@Test
	@TestForIssue(jiraKey = "HSEARCH-3324")
	public void multiValued() {
		class ObjectFieldLevel1 {
			@GenericField
			String level1SingleValuedProperty;
			@GenericField
			List<String> level1MultiValuedProperty;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField
			List<ObjectFieldLevel1> level1 = new ArrayList<>();
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						.multiValued( true )
						.field( "level1SingleValuedProperty", String.class )
						.field( "level1MultiValuedProperty", String.class, b3 -> b3.multiValued( true ) )
				)
		);
		SearchMapping mapping = setupHelper.start()
				.withAnnotatedEntityTypes( IndexedEntity.class )
				.withAnnotatedTypes( ObjectFieldLevel1.class )
				.setup();
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> {
					IndexedEntity entity = new IndexedEntity();
					entity.id = id;
					ObjectFieldLevel1 level1_1 = new ObjectFieldLevel1();
					level1_1.level1SingleValuedProperty = "1";
					level1_1.level1MultiValuedProperty = Arrays.asList( "1_1", "1_2" );
					entity.level1.add( level1_1 );
					ObjectFieldLevel1 level1_2 = new ObjectFieldLevel1();
					level1_2.level1SingleValuedProperty = "2";
					level1_2.level1MultiValuedProperty = Arrays.asList( "2_1", "2_2" );
					entity.level1.add( level1_2 );
					return entity;
				},
				document -> document
						.objectField( "level1", b2 -> b2
								.field( "level1SingleValuedProperty", "1" )
								.field( "level1MultiValuedProperty", "1_1", "1_2" )
						)
						.objectField( "level1", b2 -> b2
								.field( "level1SingleValuedProperty", "2" )
								.field( "level1MultiValuedProperty", "2_1", "2_2" )
						)
		);
	}

	/**
	 * Check that the "includePaths" parameter is at least taken into account.
	 * <p>
	 * Details of how filtering handles all corner cases is tested in the engine (see this class' javadoc).
	 */
	@Test
	public void includePaths() {
		class ObjectFieldLevel1 {
			String ignoredProperty;
			String includedProperty;
			@GenericField
			public String getIgnoredProperty() {
				return ignoredProperty;
			}
			@GenericField
			public String getIncludedProperty() {
				return includedProperty;
			}
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includePaths = "includedProperty")
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, String ignoredProperty, String includedProperty) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.ignoredProperty = ignoredProperty;
				this.level1.includedProperty = includedProperty;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						.field( "includedProperty", String.class )
				)
		);
		SearchMapping mapping = setupHelper.start()
				.withAnnotatedEntityTypes( IndexedEntity.class )
				.withAnnotatedTypes( ObjectFieldLevel1.class )
				.setup();
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, "valueForIgnored", "valueForIncluded" ),
				document -> document.objectField( "level1", b2 -> b2
						.field( "includedProperty", "valueForIncluded" )
				)
		);
	}

	/**
	 * Check that an "includePaths" parameter that doesn't match anything is reported to the user.
	 */
	@Test
	@TestForIssue(jiraKey = "HSEARCH-3136")
	public void error_includePaths_nonMatched() {
		class ObjectFieldLevel1 {
			@GenericField
			String ignoredProperty;
			@GenericField
			String includedProperty;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includePaths = {"includedProperty", "nonMatchingPath"})
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, String ignoredProperty, String includedProperty) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.ignoredProperty = ignoredProperty;
				this.level1.includedProperty = includedProperty;
			}
		}

		assertThatThrownBy(
				() -> setupHelper.start()
						.withAnnotatedEntityTypes( IndexedEntity.class )
						.withAnnotatedTypes( ObjectFieldLevel1.class )
						.setup()
		)
				.isInstanceOf( SearchException.class )
				.hasMessageMatching( FailureReportUtils.buildFailureReportPattern()
						.typeContext( IndexedEntity.class.getName() )
						.failure(
								"An @ObjectField or @IndexedEmbedded defines includePaths filters that do not match anything",
								"Non-matching includePaths filters:",
								CollectionHelper.asLinkedHashSet( "nonMatchingPath" ).toString(),
								"Encountered field paths:",
								CollectionHelper.asLinkedHashSet( "ignoredProperty", "includedProperty" ).toString(),
								"Check the filters for typos, or remove them if they are not useful"
						)
						.build()
				);
	}

	/**
	 * Check that the "includeDepth" parameter is at least taken into account.
	 * <p>
	 * Details of how filtering handles all corner cases is tested in the engine (see this class' javadoc).
	 */
	@Test
	public void includeDepth() {
		class ObjectFieldLevel2 {
			String level2Property;
			@GenericField
			public String getLevel2Property() {
				return level2Property;
			}
		}
		class ObjectFieldLevel1 {
			String level1Property;
			ObjectFieldLevel2 level2;
			@GenericField
			public String getLevel1Property() {
				return level1Property;
			}
			@ObjectField
			public ObjectFieldLevel2 getLevel2() {
				return level2;
			}
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includeDepth = 1)
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, String level1Value, String level2Value) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.level1Property = level1Value;
				this.level1.level2 = new ObjectFieldLevel2();
				this.level1.level2.level2Property = level2Value;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						.field( "level1Property", String.class )
				)
		);
		SearchMapping mapping = setupHelper.start()
				.withAnnotatedEntityTypes( IndexedEntity.class )
				.withAnnotatedTypes( ObjectFieldLevel1.class, ObjectFieldLevel2.class )
				.setup();
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, "level1Value", "level2Value" ),
				document -> document.objectField( "level1", b2 -> b2
						.field( "level1Property", "level1Value" )
				)
		);
	}

	/**
	 * Check that the "structure" parameter is at least taken into account.
	 * <p>
	 * Details of how filtering handles all corner cases is tested in the engine (see this class' javadoc).
	 */
	@Test
	public void structure() {
		class ObjectFieldLevel1 {
			@GenericField
			String level1Property;
			public String getLevel1Property() {
				return level1Property;
			}
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(structure = ObjectStructure.NESTED)
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, String level1Value) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.level1Property = level1Value;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						.objectStructure( ObjectStructure.NESTED )
						.field( "level1Property", String.class )
				)
		);
		SearchMapping mapping = setupHelper.start()
				.withAnnotatedEntityTypes( IndexedEntity.class )
				.withAnnotatedTypes( ObjectFieldLevel1.class )
				.setup();
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, "level1Value" ),
				document -> document.objectField( "level1", b2 -> b2
						.field( "level1Property", "level1Value" )
				)
		);
	}

	/**
	 * Check that bridges whose contributed fields are all filtered out are never applied.
	 */
	@Test
	@TestForIssue(jiraKey = { "HSEARCH-3212", "HSEARCH-3213" })
	public void includePaths_excludesBridges() {
		StaticCounters.Key getLongitudeKey = StaticCounters.createKey();
		StaticCounters.Key getLatitudeKey = StaticCounters.createKey();

		class ObjectFieldLevel1 {
			String level1Property;
			public Double getLongitude() {
				StaticCounters.get().increment( getLongitudeKey );
				return null;
			}
			public Double getLatitude() {
				StaticCounters.get().increment( getLatitudeKey );
				return null;
			}
		}
		class IndexedEntity {
			Integer id;
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, String level1Property) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.level1Property = level1Property;
			}
		}

		StartupStubBridge.CounterKeys filteredOutBridgeCounterKeys = StartupStubBridge.createKeys();

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						.field( "level1IncludedField", String.class )
				)
		);
		SearchMapping mapping = setupHelper.start()
				.withConfiguration( b -> {
					b.addEntityType( IndexedEntity.class );
					TypeMappingStep indexedEntityMapping = b.programmaticMapping().type( IndexedEntity.class );
					indexedEntityMapping.indexed().index( INDEX_NAME );
					indexedEntityMapping.property( "id" ).documentId();
					indexedEntityMapping.property( "level1" )
							.objectField()
									.includePaths( "level1IncludedField" );
					TypeMappingStep ObjectFieldLevel1Mapping = b.programmaticMapping().type( ObjectFieldLevel1.class );
					ObjectFieldLevel1Mapping.binder( StartupStubBridge.binder( filteredOutBridgeCounterKeys ) );
					ObjectFieldLevel1Mapping.binder( new GeoPointBridge.Binder().fieldName( "location" ) );
					ObjectFieldLevel1Mapping.property( "latitude" ).marker( new LatitudeMarker.Binder() );
					ObjectFieldLevel1Mapping.property( "longitude" ).marker( new LongitudeMarker.Binder() );
					ObjectFieldLevel1Mapping.property( "level1Property" )
							.binder( StartupStubBridge.binder( filteredOutBridgeCounterKeys ) )
							.genericField( "level1IncludedField" )
							.genericField( "filteredOut" )
									.valueBinder( StartupStubBridge.binder( String.class, filteredOutBridgeCounterKeys ) );
				} )
				.setup();
		backendMock.verifyExpectationsMet();

		/*
		 * All the bridges that were filtered out should have been instantiated,
		 * but then immediately closed.
		 * We can't check the GeoPoint bridge here, because it doesn't use static counters
		 * like our stub bridges, but we will check it isn't executed below.
		 */
		assertEquals( 3, counters.get( filteredOutBridgeCounterKeys.instance ) );
		assertEquals( 0, counters.get( filteredOutBridgeCounterKeys.instance )
				- counters.get( filteredOutBridgeCounterKeys.close ) );
		assertEquals( 0, counters.get( filteredOutBridgeCounterKeys.instance )
				- counters.get( filteredOutBridgeCounterKeys.holderClose ) );

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, "level1Value" ),
				document -> document.objectField( "level1", b2 -> b2
						.field( "level1IncludedField", "level1Value" )
				)
		);

		// The bridges that were filtered out should not have been used.
		assertEquals( 0, counters.get( filteredOutBridgeCounterKeys.runtimeUse ) );
		assertEquals( 0, counters.get( getLatitudeKey ) );
		assertEquals( 0, counters.get( getLongitudeKey ) );
	}

	@Test
	@TestForIssue(jiraKey = "HSEARCH-3072")
	public void targetType() {
		abstract class ObjectFieldLevel1 {
			public abstract String getLevel1Property();
			public abstract void setLevel1Property(String level1Property);
		}
		class ObjectFieldLevel1Impl extends ObjectFieldLevel1 {
			String level1Property;
			@Override
			@GenericField
			public String getLevel1Property() {
				return level1Property;
			}
			@Override
			public void setLevel1Property(String level1Property) {
				this.level1Property = level1Property;
			}
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includeDepth = 1, targetType = ObjectFieldLevel1Impl.class)
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, String level1Value) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1Impl();
				this.level1.setLevel1Property( level1Value );
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						.field( "level1Property", String.class )
				)
		);
		SearchMapping mapping = setupHelper.start()
				.withAnnotatedEntityTypes( IndexedEntity.class )
				.withAnnotatedTypes( ObjectFieldLevel1.class )
				.setup();
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, "level1Value" ),
				document -> document.objectField( "level1", b2 -> b2
						.field( "level1Property", "level1Value" )
				)
		);
	}

	@Test
	@TestForIssue(jiraKey = "HSEARCH-3072")
	public void targetType_castException() {
		abstract class ObjectFieldLevel1 {
			public abstract String getLevel1Property();
			public abstract void setLevel1Property(String level1Property);
		}
		class ObjectFieldLevel1Impl extends ObjectFieldLevel1 {
			String level1Property;
			@Override
			@GenericField
			public String getLevel1Property() {
				return level1Property;
			}
			@Override
			public void setLevel1Property(String level1Property) {
				this.level1Property = level1Property;
			}
		}
		class InvalidTypeImpl extends ObjectFieldLevel1 {
			String level1Property;
			@Override
			@GenericField
			public String getLevel1Property() {
				return level1Property;
			}
			@Override
			public void setLevel1Property(String level1Property) {
				this.level1Property = level1Property;
			}
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includeDepth = 1, targetType = ObjectFieldLevel1Impl.class)
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, String level1Value) {
				this.id = id;
				// The actual instance has a type that cannot be cast to ObjectFieldLevel1Impl
				this.level1 = new InvalidTypeImpl();
				this.level1.setLevel1Property( level1Value );
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						.field( "level1Property", String.class )
				)
		);
		SearchMapping mapping = setupHelper.start()
				.withAnnotatedEntityTypes( IndexedEntity.class )
				.withAnnotatedTypes( ObjectFieldLevel1.class )
				.setup();
		backendMock.verifyExpectationsMet();

		assertThatThrownBy( () -> {
			try ( SearchSession session = mapping.createSession() ) {
				IndexedEntity entity1 = new IndexedEntity( 1, "level1Value" );

				session.indexingPlan().add( entity1 );
			}
		} )
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Exception while building document for entity 'IndexedEntity#1'" )
				.hasRootCauseInstanceOf( ClassCastException.class );
	}

	@Test
	@TestForIssue(jiraKey = "HSEARCH-4148")
	public void targetType_preserveGenericTypeContext() {
		abstract class ObjectFieldLevel1<T> {
			public abstract T getLevel1Property();
			public abstract void setLevel1Property(T level1Property);
		}
		class ObjectFieldLevel1Impl<T> extends ObjectFieldLevel1<T> {
			T level1Property;
			@Override
			@GenericField
			public T getLevel1Property() {
				return level1Property;
			}
			@Override
			public void setLevel1Property(T level1Property) {
				this.level1Property = level1Property;
			}
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includeDepth = 1, targetType = ObjectFieldLevel1Impl.class)
			ObjectFieldLevel1<String> level1;
			public IndexedEntity(int id, String level1Value) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1Impl<>();
				this.level1.setLevel1Property( level1Value );
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						.field( "level1Property", String.class )
				)
		);
		SearchMapping mapping = setupHelper.start()
				.withAnnotatedEntityTypes( IndexedEntity.class )
				.withAnnotatedTypes( ObjectFieldLevel1.class )
				.setup();
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, "level1Value" ),
				document -> document.objectField( "level1", b2 -> b2
						.field( "level1Property", "level1Value" )
				)
		);
	}

	@Test
	@TestForIssue(jiraKey = "HSEARCH-3071")
	public void includeRootObjectId() {
		class ObjectFieldLevel1 {
			@DocumentId
			String theId;
			@AssociationInverseSide(inversePath = @ObjectPath(@PropertyValue(propertyName = "level1")))
			Object containing;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includeRootObjectId = true)
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, String level1Id) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.theId = level1Id;
				this.level1.containing = this;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						.field( "theId", String.class, b3 -> b3
								.searchable( Searchable.YES ).projectable( Projectable.YES ) )
				)
		);
		SearchMapping mapping = setupHelper.start().setup( IndexedEntity.class, ObjectFieldLevel1.class );
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, "level1Id" ),
				document -> document.objectField( "level1", b2 -> b2
						.field( "theId", "level1Id" )
				)
		);
	}

	@Test
	@TestForIssue(jiraKey = "HSEARCH-3071")
	public void includeRootObjectId_nonEntity() {
		class ObjectFieldLevel1 {
			@DocumentId
			String theId;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includeRootObjectId = true)
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, String level1Id) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.theId = level1Id;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						.field( "theId", String.class, b3 -> b3
								.searchable( Searchable.YES ).projectable( Projectable.YES ) )
				)
		);
		SearchMapping mapping = setupHelper.start().setup( IndexedEntity.class );
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, "level1Id" ),
				document -> document.objectField( "level1", b2 -> b2
						.field( "theId", "level1Id" )
				)
		);
	}

	@Test
	@TestForIssue(jiraKey = "HSEARCH-3071")
	public void includeRootObjectId_fieldNameConflict() {
		class ObjectFieldLevel1 {
			@DocumentId
			String theId;
			@AssociationInverseSide(inversePath = @ObjectPath(@PropertyValue(propertyName = "level1")))
			Object containing;
			@GenericField(name = "theId")
			String someProperty;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includeRootObjectId = true)
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, String level1Id) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.theId = level1Id;
				this.level1.containing = this;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						// We'll just declare two fields with the same name,
						// and will expect the backend to raise an exception
						// (that is, when it's not a stub backend).
						.field( "theId", String.class )
						.field( "theId", String.class, b3 -> b3
								.searchable( Searchable.YES ).projectable( Projectable.YES ) )
				)
		);
		SearchMapping mapping = setupHelper.start().setup( IndexedEntity.class );
		backendMock.verifyExpectationsMet();
	}

	@Test
	@TestForIssue(jiraKey = "HSEARCH-3071")
	public void includeRootObjectId_multiValued() {
		class ObjectFieldLevel1 {
			@DocumentId
			String theId;
			@AssociationInverseSide(inversePath = @ObjectPath(@PropertyValue(propertyName = "level1")))
			Object containing;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includeRootObjectId = true)
			List<ObjectFieldLevel1> level1;
			public IndexedEntity(int id, String level1Id1, String level1Id2) {
				this.id = id;
				this.level1 = Arrays.asList( new ObjectFieldLevel1(), new ObjectFieldLevel1() );
				this.level1.get( 0 ).theId = level1Id1;
				this.level1.get( 0 ).containing = this;
				this.level1.get( 1 ).theId = level1Id2;
				this.level1.get( 1 ).containing = this;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						.multiValued( true )
						.field( "theId", String.class, b3 -> b3
								.searchable( Searchable.YES ).projectable( Projectable.YES ) )
				)
		);
		SearchMapping mapping = setupHelper.start().setup( IndexedEntity.class, ObjectFieldLevel1.class );
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, "level1Id1", "level1Id2" ),
				document -> document
						.objectField( "level1", b2 -> b2
								.field( "theId", "level1Id1" ) )
						.objectField( "level1", b2 -> b2
								.field( "theId", "level1Id2" ) )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HSEARCH-3071")
	public void includeRootObjectId_noIdentifierBridge() {
		class ObjectFieldLevel1 {
			@DocumentId
			Long theId;
			@AssociationInverseSide(inversePath = @ObjectPath(@PropertyValue(propertyName = "level1")))
			Object containing;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includeRootObjectId = true)
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, Long level1Id) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.theId = level1Id;
				this.level1.containing = this;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						// The ID is a long, and there is no custom bridge on the ID,
						// so the ID field will be a long, too
						.field( "theId", Long.class, b3 -> b3
								.searchable( Searchable.YES ).projectable( Projectable.YES ) )
				)
		);
		SearchMapping mapping = setupHelper.start().setup( IndexedEntity.class, ObjectFieldLevel1.class );
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, 4242L ),
				document -> document
						.objectField( "level1", b2 -> b2
								.field( "theId", 4242L ) )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HSEARCH-3071")
	public void includeRootObjectId_identifierBinder() {
		class ObjectFieldLevel1 {
			@DocumentId(identifierBinder = @IdentifierBinderRef(type = MyCustomIdentifierBinder.class))
			Long theId;
			@AssociationInverseSide(inversePath = @ObjectPath(@PropertyValue(propertyName = "level1")))
			Object containing;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includeRootObjectId = true)
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, Long level1Id) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.theId = level1Id;
				this.level1.containing = this;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						// The ID is a long, and there is an identifier bridge on the DocumentId,
						// so the ID field will be generated with the identifier bridge, and thus will be a String.
						.field( "theId", String.class, b3 -> b3
								.searchable( Searchable.YES ).projectable( Projectable.YES ) )
				)
		);
		SearchMapping mapping = setupHelper.start().expectCustomBeans()
				.setup( IndexedEntity.class, ObjectFieldLevel1.class );
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, 4242L ),
				document -> document
						.objectField( "level1", b2 -> b2
								.field( "theId", "4243" ) )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HSEARCH-3071")
	public void includeRootObjectId_identifierBridge() {
		class ObjectFieldLevel1 {
			@DocumentId(identifierBridge = @IdentifierBridgeRef(type = MyCustomIdentifierBinder.Bridge.class))
			Long theId;
			@AssociationInverseSide(inversePath = @ObjectPath(@PropertyValue(propertyName = "level1")))
			Object containing;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includeRootObjectId = true)
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, Long level1Id) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.theId = level1Id;
				this.level1.containing = this;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						// The ID is a long, and there is an identifier bridge on the DocumentId,
						// so the ID field will be generated with the identifier bridge, and thus will be a String.
						.field( "theId", String.class, b3 -> b3
								.searchable( Searchable.YES ).projectable( Projectable.YES ) )
				)
		);
		SearchMapping mapping = setupHelper.start().expectCustomBeans()
				.setup( IndexedEntity.class, ObjectFieldLevel1.class );
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, 4242L ),
				document -> document
						.objectField( "level1", b2 -> b2
								.field( "theId", "4243" ) )
		);
	}

	public static class MyCustomIdentifierBinder implements IdentifierBinder {
		@Override
		public void bind(IdentifierBindingContext<?> context) {
			assertThat( context.bridgedElement() ).isNotNull();
			assertThat( context.beanResolver() ).isNotNull();
			context.bridge( Long.class, new Bridge() );
		}

		public static class Bridge implements IdentifierBridge<Long> {
			@Override
			public String toDocumentIdentifier(Long propertyValue,
					IdentifierBridgeToDocumentIdentifierContext context) {
				assertThat( context ).isNotNull();
				return String.valueOf( propertyValue + 1 );
			}

			@Override
			public Long fromDocumentIdentifier(String documentIdentifier,
					IdentifierBridgeFromDocumentIdentifierContext context) {
				assertThat( context ).isNotNull();
				return Long.parseLong( documentIdentifier ) - 1;
			}
		}
	}

	@Test
	public void includeRootObjectId_identifierBridge_withParams_annotationMapping() {
		class ObjectFieldLevel1 {
			@DocumentId(identifierBinder = @IdentifierBinderRef(type = ParametricBinder.class,
					params = @Param(name = "stringBase", value = "3")))
			Long theId;
			@AssociationInverseSide(inversePath = @ObjectPath(@PropertyValue(propertyName = "level1")))
			Object containing;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includeRootObjectId = true)
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, Long level1Id) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.theId = level1Id;
				this.level1.containing = this;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						// The ID is a long, and there is an identifier bridge on the DocumentId,
						// so the ID field will be generated with the identifier bridge, and thus will be a String.
						.field( "theId", String.class, b3 -> b3
								.searchable( Searchable.YES ).projectable( Projectable.YES ) )
				)
		);
		SearchMapping mapping = setupHelper.start().expectCustomBeans()
				.setup( IndexedEntity.class, ObjectFieldLevel1.class );
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, 4242L ),
				document -> document
						.objectField( "level1", b2 -> b2
								.field( "theId", "4245" ) )
		);
	}

	@Test
	public void includeRootObjectId_identifierBridge_withParams_programmaticMapping() {
		class ObjectFieldLevel1 {
			Long theId;
			Object containing;
		}
		class IndexedEntity {
			Integer id;
			ObjectFieldLevel1 level1;
			public IndexedEntity(int id, Long level1Id) {
				this.id = id;
				this.level1 = new ObjectFieldLevel1();
				this.level1.theId = level1Id;
				this.level1.containing = this;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						// The ID is a long, and there is an identifier bridge on the DocumentId,
						// so the ID field will be generated with the identifier bridge, and thus will be a String.
						.field( "theId", String.class, b3 -> b3
								.searchable( Searchable.YES ).projectable( Projectable.YES ) )
				)
		);
		SearchMapping mapping = setupHelper.start().expectCustomBeans()
				.withConfiguration( builder -> {
					builder.addEntityType( IndexedEntity.class );
					builder.addEntityType( ObjectFieldLevel1.class );

					TypeMappingStep ObjectFieldLevel1 = builder.programmaticMapping()
							.type( ObjectFieldLevel1.class );
					ObjectFieldLevel1.property( "theId" ).documentId().identifierBinder( new ParametricBinder(),
							Collections.singletonMap( "base", 3 )
					);
					ObjectFieldLevel1.property( "containing" )
							.associationInverseSide( PojoModelPath.ofValue( "level1" ) );

					TypeMappingStep indexedEntity = builder.programmaticMapping()
							.type( IndexedEntity.class );
					indexedEntity.indexed().index( INDEX_NAME );
					indexedEntity.property( "id" ).documentId();
					indexedEntity.property( "level1" ).objectField().includeRootObjectId( true );
				} )
				.setup( IndexedEntity.class, ObjectFieldLevel1.class );
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, 4242L ),
				document -> document
						.objectField( "level1", b2 -> b2
								.field( "theId", "4245" ) )
		);
	}

	public static class ParametricBinder implements IdentifierBinder {
		@Override
		public void bind(IdentifierBindingContext<?> context) {
			assertThat( context.bridgedElement() ).isNotNull();
			assertThat( context.beanResolver() ).isNotNull();
			context.bridge( Long.class, new Bridge( extractBase( context ) ) );
		}

		@SuppressWarnings("uncheked")
		private static int extractBase(IdentifierBindingContext<?> context) {
			Optional<Object> optionalBase = context.paramOptional( "base" );
			if ( optionalBase.isPresent() ) {
				return (Integer) optionalBase.get();
			}

			String stringBase = (String) context.param( "stringBase" );
			return Integer.parseInt( stringBase );
		}

		public static class Bridge implements IdentifierBridge<Long> {
			private final int base;

			public Bridge(int base) {
				this.base = base;
			}

			@Override
			public String toDocumentIdentifier(Long propertyValue,
					IdentifierBridgeToDocumentIdentifierContext context) {
				assertThat( context ).isNotNull();
				return String.valueOf( propertyValue + base );
			}

			@Override
			public Long fromDocumentIdentifier(String documentIdentifier,
					IdentifierBridgeFromDocumentIdentifierContext context) {
				assertThat( context ).isNotNull();
				return Long.parseLong( documentIdentifier ) - base;
			}
		}
	}

	@Test
	@TestForIssue(jiraKey = "HSEARCH-3071")
	public void includeRootObjectId_targetType() {
		class ObjectFieldLevel1 {
			@DocumentId
			String theId;
			@AssociationInverseSide(inversePath = @ObjectPath(@PropertyValue(propertyName = "level1")))
			Object containing;
		}
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField(includeRootObjectId = true, targetType = ObjectFieldLevel1.class)
			Object level1;
			public IndexedEntity(int id, String level1Id) {
				this.id = id;
				ObjectFieldLevel1 level1 = new ObjectFieldLevel1();
				this.level1 = level1;
				level1.theId = level1Id;
				level1.containing = this;
			}
		}

		backendMock.expectSchema( INDEX_NAME, b -> b
				.objectField( "level1", b2 -> b2
						.field( "theId", String.class, b3 -> b3
								.searchable( Searchable.YES ).projectable( Projectable.YES ) )
				)
		);
		SearchMapping mapping = setupHelper.start().setup( IndexedEntity.class, ObjectFieldLevel1.class );
		backendMock.verifyExpectationsMet();

		doTestEmbeddedRuntime(
				mapping,
				id -> new IndexedEntity( id, "level1Id" ),
				document -> document.objectField( "level1", b2 -> b2
						.field( "theId", "level1Id" )
				)
		);
	}

	@Test
	@TestForIssue(jiraKey = "HSEARCH-899")
	public void invalid_wrongType() {
		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@GenericField
			String text;
			@ObjectField
			String invalid;
		}

		assertThatThrownBy( () -> setupHelper.start()
				.withAnnotatedEntityTypes( IndexedEntity.class )
				.setup() )
				.isInstanceOf( SearchException.class )
				.hasMessageMatching( FailureReportUtils.buildFailureReportPattern()
						.typeContext( IndexedEntity.class.getName() )
						.pathContext( ".invalid" )
						.failure( "Unable to index-embed type '" + String.class.getName() + "': no index mapping"
								+ " (@GenericField, @FullTextField, custom bridges, ...) is defined for that type." )
						.build()
				);
	}

	@Test
	@TestForIssue(jiraKey = "HSEARCH-899")
	public void invalid_emptyNested() {
		class ValidNested {
			String text;

			@GenericField
			public String getText() {
				return text;
			}
		}

		class EmptyNested {
		}

		@Indexed(index = INDEX_NAME)
		class IndexedEntity {
			@DocumentId
			Integer id;
			@ObjectField
			ValidNested valid;
			@ObjectField
			EmptyNested invalid;
		}

		assertThatThrownBy( () -> setupHelper.start()
				.withAnnotatedEntityTypes( IndexedEntity.class )
				.setup() )
				.isInstanceOf( SearchException.class )
				.hasMessageMatching( FailureReportUtils.buildFailureReportPattern()
						.typeContext( IndexedEntity.class.getName() )
						.pathContext( ".invalid" )
						.failure( "Unable to index-embed type '" + EmptyNested.class.getName() + "': no index mapping"
								+ " (@GenericField, @FullTextField, custom bridges, ...) is defined for that type." )
						.build()
				);
	}

	private <E> void doTestEmbeddedRuntime(SearchMapping mapping,
			Function<Integer, E> newEntityFunction,
			Consumer<StubDocumentNode.Builder> expectedDocumentContributor) {
		try ( SearchSession session = mapping.createSession() ) {
			E entity1 = newEntityFunction.apply( 1 );

			session.indexingPlan().add( entity1 );

			backendMock.expectWorks( INDEX_NAME )
					.add( "1", expectedDocumentContributor );
		}
		backendMock.verifyExpectationsMet();
	}
}
