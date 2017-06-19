/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.integration.wildfly.tika.model;

import java.net.URI;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.TikaBridge;

/**
 * @author Yoann Rodiere
 */
@Entity
@Indexed
public class EntityWithTikaBridge {

	@Id
	@GeneratedValue
	private Long id;

	@Field
	@TikaBridge
	private URI content;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public URI getContent() {
		return content;
	}

	public void setContent(URI content) {
		this.content = content;
	}

}
