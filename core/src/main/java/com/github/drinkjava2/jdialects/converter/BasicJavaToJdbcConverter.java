package com.github.drinkjava2.jdialects.converter;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import com.github.drinkjava2.jdialects.Java8DateUtils;

public class BasicJavaToJdbcConverter implements JavaToJdbcConverter {
	public static final BasicJavaToJdbcConverter instance = new BasicJavaToJdbcConverter();

	@Override
	public Object convert(Object value) {
		if (value == null)
			return value;
		Class<?> vType = value.getClass();
		if (java.util.Date.class == vType)
			return new java.sql.Date(((Date) value).getTime());
		if (Character.class == vType)
			return ((Character) value).toString();
		if (BigInteger.class == vType)
			return ((BigInteger) value).longValue(); // 2020.10.1 change from int to long
		if (Boolean.class == vType)
			return ((Boolean) value).booleanValue();
		else if (Calendar.class.isAssignableFrom(vType))
			return new java.sql.Date(((Calendar) value).getTime().getTime());
		/*- JAVA8_BEGIN */
		else if (java.time.temporal.Temporal.class.isAssignableFrom(vType)) {
			if (java.time.LocalDate.class == vType)
				return Java8DateUtils.localDate2SqlDate((java.time.LocalDate) value);
			else if (java.time.LocalTime.class == vType)
				return Java8DateUtils.localTime2SqlTime((java.time.LocalTime) value);
			else if (java.time.OffsetTime.class == vType)
				return Java8DateUtils.offsetTime2SqlTime((java.time.OffsetTime) value);
			else if (java.time.Instant.class == vType)
				return Java8DateUtils.instant2SqlTimestamp((java.time.Instant) value);
			else if (java.time.LocalDateTime.class == vType)
				return Java8DateUtils.localDateTime2SqlTimestamp((java.time.LocalDateTime) value);
			else if (java.time.OffsetDateTime.class == vType)
				return Java8DateUtils.offsetDateTime2SqlTimestamp((java.time.OffsetDateTime) value);
			else if (java.time.ZonedDateTime.class == vType)
				return Java8DateUtils.zonedDateTime2SqlTimestamp((java.time.ZonedDateTime) value);
		}
		/* JAVA8_END */
		return value;
	}
}