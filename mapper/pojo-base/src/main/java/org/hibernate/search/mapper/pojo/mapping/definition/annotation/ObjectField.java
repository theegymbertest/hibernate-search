/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.pojo.mapping.definition.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.search.engine.backend.types.ObjectStructure;
import org.hibernate.search.engine.environment.bean.BeanRetrieval;
import org.hibernate.search.mapper.pojo.extractor.mapping.annotation.ContainerExtraction;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.PropertyMapping;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.PropertyMappingAnnotationProcessorRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.processing.impl.ObjectFieldProcessor;

/**
 * Maps a property to an object field whose fields are the same as those defined in the property type.
 * <p>
 * This allows search queries on a single index to use data from multiple entities.
 * <p>
 * For example, let's consider this (incomplete) mapping:
 * <pre>{@code
 * {@literal @}Indexed
 * public class Book {
 *     {@literal @}GenericField
 *     private String title;
 *     {@literal @}ObjectField
 *     private List<Author> authors;
 * }
 * public class Author {
 *     {@literal @}GenericField
 *     private String firstName;
 *     {@literal @}GenericField
 *     private String lastName;
 *     private List<Book> books;
 * }
 * }</pre>
 * <p>
 * The names of authors are stored in different objects,
 * thus by default they would not be included in documents created
 * for {@code Book} entities.
 * But we added the {@code @ObjectField} annotation to the {@code authors} property,
 * so Hibernate Search will <em>embed</em> this data in a {@code authors} field
 * of documents created for {@code Book} entities.
 * <p>
 * How exactly this embedding will happen depends on the configured {@link #structure() structure}.
 * Let's consider this representation of the book "Leviathan Wakes":
 * <ul>
 *     <li>title = Leviathan Wakes</li>
 *     <li>authors =
 *         <ul>
 *             <li>(first element)
 *                 <ul>
 *                     <li>firstName = Daniel</li>
 *                     <li>lastName = Abraham</li>
 *                 </ul>
 *             </li>
 *             <li>(second element)
 *                 <ul>
 *                     <li>firstName = Ty</li>
 *                     <li>lastName = Frank</li>
 *                 </ul>
 *             </li>
 *         </ul>
 *     </li>
 * </ul>
 * <p>
 * With the default {@link ObjectStructure#FLATTENED flattened structure} (more efficient),
 * the document structure will be a little different from what one would expect:
 * <ul>
 *     <li>title = Leviathan Wakes</li>
 *     <li>authors.firstName =
 *         <ul>
 *             <li>(first element) Daniel</li>
 *             <li>(second element) Ty</li>
 *         </ul>
 *     </li>
 *     <li>authors.lastName =
 *         <ul>
 *             <li>(first element) Abraham</li>
 *             <li>(second element) Frank</li>
 *         </ul>
 *     </li>
 * </ul>
 * <p>
 * To get the original structure, the {@link ObjectStructure#NESTED nested structure} must be used,
 * but this has an impact on performance and how queries must be structured.
 * See the reference documentation for more information.
 */
@Documented
@Repeatable(ObjectField.List.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@PropertyMapping(processor = @PropertyMappingAnnotationProcessorRef(type = ObjectFieldProcessor.class, retrieval = BeanRetrieval.CONSTRUCTOR))
public @interface ObjectField {

	/**
	 * @return The name of the object field created to represent this {@code @ObjectField}.
	 * Defaults to the property name.
	 */
	String name() default "";

	/**
	 * The paths of index fields that should be included in the object field,
	 * relative to the object type that should be mapped to the object field.
	 * <p>
	 * This takes precedence over {@link #includeDepth()}.
	 * <p>
	 * By default, if neither {@code includePaths} nor {@link #includeDepth()} is defined,
	 * all index fields are included.
	 *
	 * @return The paths of index fields to include explicitly.
	 * Provided paths must be relative to the indexed-embedded element,
	 * i.e. they must not include the {@link #name()}.
	 */
	String[] includePaths() default {};

	/**
	 * The number of levels of objects that will have all their fields included by default.
	 * <p>
	 * {@code includeDepth} is the number of `@ObjectField` annotations that will be traversed
	 * and for which all fields of the object fields will be included,
	 * even if these fields are not included explicitly through {@code includePaths}:
	 * <ul>
	 * <li>{@code includeDepth=0} means fields of the root object are <strong>not</strong> included,
	 * nor is any field of deeper objects,
	 * unless these fields are included explicitly through {@link #includePaths()}.
	 * <li>{@code includeDepth=1} means fields of the root object <strong>are</strong> included,
	 * but <strong>not</strong> fields of deeper objects,
	 * unless these fields are included explicitly through {@link #includePaths()}.
	 * <li>And so on.
	 * </ul>
	 * The default value depends on the value of the {@link #includePaths()} attribute:
	 * if {@link #includePaths()} is empty, the default is {@code Integer.MAX_VALUE} (include all fields at every level)
	 * if {@link #includePaths()} is <strong>not</strong> empty, the default is {@code 0}
	 * (only include fields included explicitly).
	 *
	 * @return The number of levels of objects that will have all their fields included by default.
	 */
	int includeDepth() default -1;

	/**
	 * Whether the identifier of root objects should be included as an index field.
	 * <p>
	 * The index field will defined as if the following annotation was put on the identifier property
	 * of the mapped object type:
	 * {@code @GenericField(searchable = Searchable.YES, projectable = Projectable.YES)}.
	 * The name of the index field will be the name of the identifier property.
	 * Its bridge will be the identifier bridge applied to the identifier property using {@link DocumentId} if any,
	 * or the default value bridge for the property type by default.
	 * <p>
	 * If you need more advanced mapping (custom name, custom bridge, sortable, ...),
	 * define the field explicitly in the mapped object type by annotating the identifier property
	 * with {@link GenericField} or a similar field annotation,
	 * and make sure the field is included by {@code @ObjectField} by configuring
	 * {@link #includeDepth()} and/or {@link #includePaths()}.
	 *
	 * @return Whether the identifier of root objects should be included as an index field.
	 */
	boolean includeRootObjectId() default false;

	/**
	 * @return How the structure of the object field is preserved upon indexing.
	 * @see ObjectStructure
	 */
	ObjectStructure structure() default ObjectStructure.DEFAULT;

	/**
	 * @return A definition of container extractors to be applied to the property,
	 * allowing the definition of a (potentially multi-valued) object field for container elements.
	 * This is useful when the property is of container type,
	 * for example a {@code Map<TypeA, TypeB>}:
	 * defining the extraction as {@code @ContainerExtraction(BuiltinContainerExtractors.MAP_KEY)}
	 * allows referencing map keys instead of map values.
	 * By default, Hibernate Search will try to apply a set of extractors for common container types.
	 * @see ContainerExtraction
	 */
	ContainerExtraction extraction() default @ContainerExtraction;

	/**
	 * @return A type the mapped objects should be cast to.
	 * When relying on {@link #extraction() container extraction},
	 * the extracted values are cast, not the container.
	 * By default, no casting occurs.
	 */
	Class<?> targetType() default void.class;

	@Documented
	@Target({ ElementType.METHOD, ElementType.FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface List {
		ObjectField[] value();
	}

}
