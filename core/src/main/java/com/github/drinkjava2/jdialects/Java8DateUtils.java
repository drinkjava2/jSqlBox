/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jdialects;

/*- JAVA8_BEGIN */
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime; 
import java.util.Date; 
 
public abstract class Java8DateUtils {// NOSONAR 

	private static final ZoneId zoneId = ZoneId.systemDefault();

	public static LocalDate date2LocalDate(java.util.Date date) {
		Instant instant = new Date(date.getTime()).toInstant();
		return instant.atZone(zoneId).toLocalDate();
	}

	public static LocalDateTime date2LocalDateTime(Date date) {
		Instant instant = date.toInstant();
		return instant.atZone(zoneId).toLocalDateTime();
	}

	public static Date localDate2Date(LocalDate localDate) {
		ZonedDateTime zdt = localDate.atStartOfDay(zoneId);
		return Date.from(zdt.toInstant());
	}

	public static Date localDateTime2Date(LocalDateTime localDateTime) {
		ZonedDateTime zdt = localDateTime.atZone(zoneId);
		return new Date(Date.from(zdt.toInstant()).getTime());
	}

	// below to conver java8 date types to sql type

	public static java.sql.Date localDate2SqlDate(LocalDate localDate) {
		ZonedDateTime zdt = localDate.atStartOfDay(zoneId);
		return new java.sql.Date(Date.from(zdt.toInstant()).getTime());
	}

	public static java.sql.Time localTime2SqlTime(LocalTime localTime) {
		return java.sql.Time.valueOf(localTime);
	}

	public static java.sql.Time offsetTime2SqlTime(OffsetTime offsetTime) {
		return java.sql.Time.valueOf(offsetTime.toLocalTime());
	}

	public static java.sql.Timestamp instant2SqlTimestamp(Instant instant) {
		return new java.sql.Timestamp(Date.from(instant).getTime());
	}

	public static java.sql.Timestamp localDateTime2SqlTimestamp(LocalDateTime localDateTime) {
		return Timestamp.valueOf(localDateTime);
	}

	public static java.sql.Timestamp offsetDateTime2SqlTimestamp(OffsetDateTime offsetDateTime) {
		return Timestamp.valueOf(offsetDateTime.toLocalDateTime());
	}

	public static java.sql.Timestamp zonedDateTime2SqlTimestamp(ZonedDateTime zonedDateTime) {
		return Timestamp.valueOf(zonedDateTime.toLocalDateTime());
	}

	// below to conver sql date types to java8 types
	public static LocalDate sqlDate2localDate(java.sql.Date date) {
		Instant instant = new Date(date.getTime()).toInstant();
		return instant.atZone(zoneId).toLocalDate();
	}

	public static LocalDateTime sqlDate2localDateTime(java.sql.Date date) {
		Instant instant = new Date(date.getTime()).toInstant();
		return instant.atZone(zoneId).toLocalDateTime();
	}

	public static LocalTime sqlTime2LocalTime(java.sql.Time time) {
		return time.toLocalTime();
	}

	// extra
	public static LocalDateTime sqlTime2LocalDateTime(java.sql.Time time) {
		Instant instant = new Date(time.getTime()).toInstant();
		return instant.atZone(zoneId).toLocalDateTime();
	}

	public static OffsetTime sqlTime2OffsetTime(java.sql.Time time) {
		Instant instant = new Date(time.getTime()).toInstant();
		return OffsetTime.ofInstant(instant, zoneId);
	}

	public static OffsetTime sqlTimestamp2OffsetTime(Timestamp t) {
		return OffsetTime.ofInstant(t.toInstant(), zoneId);
	}

	public static Instant sqlTimestamp2instant(Timestamp t) {
		return t.toInstant();
	}

	public static LocalDateTime sqlTimestamp2LocalDateTime(Timestamp t) {
		return t.toLocalDateTime();
	}

	public static OffsetDateTime sqlTimestamp2OffsetDateTime(Timestamp t) {
		return OffsetDateTime.ofInstant(t.toInstant(), zoneId);
	}

	public static ZonedDateTime sqlTimestamp2ZonedDateTime(Timestamp t) {
		return ZonedDateTime.ofInstant(t.toInstant(), zoneId);
	}

	public static LocalDate sqlTimestamp2LocalDate(Timestamp t) {
		Instant instant = new Date(t.getTime()).toInstant();
		return instant.atZone(zoneId).toLocalDate();
	}

	public static LocalTime sqlTimestamp2LocalTime(Timestamp t) {
		Instant instant = new Date(t.getTime()).toInstant();
		return instant.atZone(zoneId).toLocalTime();
	} 
}
/* JAVA8_END */