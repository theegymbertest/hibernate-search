/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.jsr352.massindexing.test.id;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Locale;

import javax.persistence.Embeddable;

/**
 * Primary key for {@link org.hibernate.search.jsr352.massindexing.test.entity.EntityWithComparableId}.
 *
 * @author Mincong Huang
 */
@Embeddable
public class ComparableDateId implements Serializable, Comparable<ComparableDateId> {

	private static final long serialVersionUID = -3941766084997859100L;

	private int year;

	private int month;

	private int day;

	public ComparableDateId() {

	}

	public ComparableDateId(LocalDate d) {
		year = d.getYear();
		month = d.getMonthValue();
		day = d.getDayOfMonth();
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getYear() {
		return year;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getMonth() {
		return month;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getDay() {
		return day;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + day;
		result = prime * result + month;
		result = prime * result + year;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		ComparableDateId that = (ComparableDateId) obj;
		if ( day != that.day ) {
			return false;
		}
		if ( month != that.month ) {
			return false;
		}
		if ( year != that.year ) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format( Locale.ROOT, "%04d-%02d-%02d", year, month, day );
	}

	@Override
	public int compareTo(ComparableDateId that) {
		if ( year != that.year ) {
			return year - that.year;
		}
		if ( month != that.month ) {
			return month - that.month;
		}
		return day - that.day;
	}

}
