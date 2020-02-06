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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Java8 date utils
 * 
 * @author Yong Zhu
 * @since 4.0.0
 */
public abstract class Java8DateUtils {// NOSONAR
	static ZoneId zoneId = ZoneId.systemDefault();

	public static ZoneId getZoneId() {
		return zoneId;
	}

	public static void setZoneId(ZoneId zoneId) {
		Java8DateUtils.zoneId = zoneId;
	}

	public static LocalDate timeStamp2LocalDate(Timestamp t) {
		Instant instant = new Date(t.getTime()).toInstant();
		return instant.atZone(zoneId).toLocalDate();
	}

	public static LocalDateTime timeStamp2LocalDateTime(Timestamp t) {
		Instant instant = new Date(t.getTime()).toInstant();
		return instant.atZone(zoneId).toLocalDateTime();
	}

	public static LocalDate date2LocalDate(java.util.Date date) {
		Instant instant = new Date(date.getTime()).toInstant();
		return instant.atZone(zoneId).toLocalDate();
	}

	public static LocalDateTime date2LocalDateTime(Date date) {
		Instant instant = date.toInstant();
		return instant.atZone(zoneId).toLocalDateTime();
	}

	public static Date localDateTime2Date(LocalDateTime localDateTime) {
		ZonedDateTime zdt = localDateTime.atZone(zoneId);
		return Date.from(zdt.toInstant());
	}
}
