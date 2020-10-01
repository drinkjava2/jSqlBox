package com.github.drinkjava2.jdialects;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import com.github.drinkjava2.jsqlbox.DbException;

public class BasicJdbcTypeConverter implements JdbcTypeConverter {
	@Override
	public Object jdbcValue2JavaValue(Object value, Class<?> javaType) {
		if (value == null)
			return null;
		if (javaType == String.class)
			return value.toString();
		Class<?> vType = value.getClass();
		if (javaType.isAssignableFrom(vType))
			return value;
		if (vType == BigDecimal.class) {
			if (javaType == Integer.class || javaType == int.class)
				return ((BigDecimal) value).intValue();
			if (javaType == Long.class || javaType == long.class)
				return ((BigDecimal) value).longValue();
			if (javaType == Byte.class || javaType == byte.class)
				return ((BigDecimal) value).byteValue();
			if (javaType == Double.class || javaType == double.class)
				return ((BigDecimal) value).doubleValue();
			if (javaType == Float.class || javaType == float.class)
				return ((BigDecimal) value).floatValue();
			if (javaType == Short.class || javaType == short.class)
				return ((BigDecimal) value).shortValue();
			if (javaType == Boolean.class || javaType == boolean.class)
				return ((BigDecimal) value).byteValue() != 0;
		}
		if (vType == BigInteger.class) {
			if (javaType == Integer.class || javaType == int.class)
				return ((BigInteger) value).intValue();
			if (javaType == Long.class || javaType == long.class)
				return ((BigInteger) value).longValue();
			if (javaType == Byte.class || javaType == byte.class)
				return ((BigInteger) value).byteValue();
			if (javaType == Double.class || javaType == double.class)
				return ((BigInteger) value).doubleValue();
			if (javaType == Float.class || javaType == float.class)
				return ((BigInteger) value).floatValue();
			if (javaType == Short.class || javaType == short.class)
				return ((BigInteger) value).shortValue();
			if (javaType == Boolean.class || javaType == boolean.class)
				return ((BigInteger) value).byteValue() != 0;
		} else if (vType == Integer.class) {
			if (javaType == int.class)
				return ((Integer) value).intValue();
			if (javaType == Long.class || javaType == long.class)
				return ((Integer) value).longValue();
			if (javaType == Byte.class || javaType == byte.class)
				return ((Integer) value).byteValue();
			if (javaType == Double.class || javaType == double.class)
				return ((Integer) value).doubleValue();
			if (javaType == Float.class || javaType == float.class)
				return ((Integer) value).floatValue();
			if (javaType == Short.class || javaType == short.class)
				return ((Integer) value).shortValue();
		} else if (vType == Long.class) {
			if (javaType == Integer.class || javaType == int.class)
				return ((Long) value).intValue();
			if (javaType == long.class)
				return ((Long) value).longValue();
			if (javaType == Byte.class || javaType == byte.class)
				return ((Long) value).byteValue();
			if (javaType == Double.class || javaType == double.class)
				return ((Long) value).doubleValue();
			if (javaType == Float.class || javaType == float.class)
				return ((Long) value).floatValue();
			if (javaType == Short.class || javaType == short.class)
				return ((Long) value).shortValue();
			if (javaType == BigInteger.class)
				return BigInteger.valueOf((Long) value);
		} else if (vType == Double.class) {
			if (javaType == Integer.class || javaType == int.class)
				return ((Double) value).intValue();
			if (javaType == Long.class || javaType == long.class)
				return ((Double) value).longValue();
			if (javaType == Byte.class || javaType == byte.class)
				return ((Double) value).byteValue();
			if (javaType == double.class)
				return ((Double) value).doubleValue();
			if (javaType == Float.class || javaType == float.class)
				return ((Double) value).floatValue();
			if (javaType == Short.class || javaType == short.class)
				return ((Double) value).shortValue();
		} else if (vType == Float.class) {
			if (javaType == Integer.class || javaType == int.class)
				return ((Float) value).intValue();
			if (javaType == Long.class || javaType == long.class)
				return ((Float) value).longValue();
			if (javaType == Byte.class || javaType == byte.class)
				return ((Float) value).byteValue();
			if (javaType == Double.class || javaType == double.class)
				return ((Float) value).doubleValue();
			if (javaType == float.class)
				return ((Float) value).floatValue();
			if (javaType == Short.class || javaType == short.class)
				return ((Float) value).shortValue();
		} else if (vType == Short.class) {
			if (javaType == Integer.class || javaType == int.class)
				return ((Short) value).intValue();
			if (javaType == Long.class || javaType == long.class)
				return ((Short) value).longValue();
			if (javaType == Byte.class || javaType == byte.class)
				return ((Short) value).byteValue();
			if (javaType == Double.class || javaType == double.class)
				return ((Short) value).doubleValue();
			if (javaType == Float.class || javaType == float.class)
				return ((Short) value).floatValue();
			if (javaType == short.class)
				return ((Short) value).shortValue();
		} else if (vType == Byte.class) {
			if (javaType == Integer.class || javaType == int.class)
				return ((Byte) value).intValue();
			if (javaType == Long.class || javaType == long.class)
				return ((Byte) value).longValue();
			if (javaType == byte.class)
				return ((Byte) value).byteValue();
			if (javaType == Double.class || javaType == double.class)
				return ((Byte) value).doubleValue();
			if (javaType == Float.class || javaType == float.class)
				return ((Byte) value).floatValue();
			if (javaType == Short.class || javaType == short.class)
				return ((Byte) value).shortValue();
		} else if (vType == java.sql.Date.class) { // no need convert java.util.Date
			if (javaType == Timestamp.class)
				return new Timestamp(((java.sql.Date) value).getTime());
			if (javaType == java.sql.Time.class)
				return new java.sql.Time(((java.sql.Date) value).getTime());
			if (javaType == Calendar.class) {
				Calendar c = Calendar.getInstance();
				c.setTime((java.sql.Date) value);
				return c;
			}
		} else if (vType == java.sql.Time.class) {
			if (javaType == Timestamp.class)
				return new Timestamp(((java.sql.Time) value).getTime());
			if (javaType == java.sql.Date.class)
				return new java.sql.Date(((java.sql.Time) value).getTime());
		} else if (vType == Timestamp.class) {
			if (javaType == java.sql.Date.class)
				return new java.sql.Date(((Timestamp) value).getTime());
			if (javaType == java.sql.Time.class)
				return new java.sql.Time(((Timestamp) value).getTime());
			if (javaType == Calendar.class) {
				Calendar c = Calendar.getInstance();
				c.setTime(new java.util.Date(((Timestamp) value).getTime()));
				return c;
			}
		} else if (vType == java.util.Date.class) {
			if (javaType == Timestamp.class)
				return new Timestamp(((java.util.Date) value).getTime());
			if (javaType == java.sql.Date.class)
				return new java.util.Date(((java.util.Date) value).getTime());
			if (javaType == java.sql.Time.class)
				return new java.sql.Time(((java.util.Date) value).getTime());
			if (javaType == Calendar.class) {
				Calendar c = Calendar.getInstance();
				c.setTime((java.util.Date) value);
				return c;
			}
		} else if (vType == String.class) {
			if (javaType == char.class || javaType == Character.class) {
				return ((String) value).length() > 0 ? ((String) value).charAt(0) : '\u0000';
			}
		} else if (vType == Boolean.class) {
			return ((Boolean) value).booleanValue();
		}

		/*- JAVA8_BEGIN */
		if (vType == java.sql.Date.class) {
			if (javaType == java.time.LocalDate.class)
				return Java8DateUtils.sqlDate2localDate((java.sql.Date) value);
			if (javaType == java.time.LocalDateTime.class)
				return Java8DateUtils.sqlDate2localDateTime((java.sql.Date) value);
		} else if (vType == java.sql.Time.class) {
			if (javaType == java.time.LocalTime.class)
				return Java8DateUtils.sqlTime2LocalTime((java.sql.Time) value);
			if (javaType == java.time.OffsetTime.class)
				return Java8DateUtils.sqlTime2OffsetTime((java.sql.Time) value);
		} else if (vType == Timestamp.class) {
			if (javaType == java.time.LocalDate.class)
				return Java8DateUtils.sqlTimestamp2LocalDate((Timestamp) value);
			if (javaType == java.time.LocalTime.class)
				return Java8DateUtils.sqlTimestamp2LocalTime((Timestamp) value);
			if (javaType == java.time.Instant.class)
				return Java8DateUtils.sqlTimestamp2instant((Timestamp) value);
			if (javaType == java.time.LocalDateTime.class)
				return Java8DateUtils.sqlTimestamp2LocalDateTime((Timestamp) value);
			if (javaType == java.time.OffsetDateTime.class)
				return Java8DateUtils.sqlTimestamp2OffsetDateTime((Timestamp) value);
			if (javaType == java.time.ZonedDateTime.class)
				return Java8DateUtils.sqlTimestamp2ZonedDateTime((Timestamp) value);
			if (javaType == java.time.OffsetTime.class)
				return Java8DateUtils.sqlTimestamp2OffsetTime((Timestamp) value);
		} else if (vType == Date.class) {
			if (javaType == java.time.LocalDate.class)
				return Java8DateUtils.date2LocalDate((Date) value);
			if (javaType == java.time.LocalDateTime.class)
				return Java8DateUtils.date2LocalDateTime((Date) value);
		}
		/* JAVA8_END */
		String oracleTip = "oracle.sql.TIMESTAMP".equals(vType.getName()) // NOSONAR
				? "\nBelow setting may solve this Oracle JDBC compliant issue:\n" + "System.getProperties().setProperty(\"oracle.jdbc.J2EE13Compliant\", \"true\");"
				: "";
		throw new DbException("Can not convert jdbc type: '" + value.getClass() + "' with value '" + value + "' to jave type:" + javaType + oracleTip);
	}

	@Override
	public Object javaValue2JdbcValue(Object value) {
		if (value == null)
			return value;
		Class<?> vType = value.getClass();
		if (java.util.Date.class == vType)
			return new java.sql.Date(((Date) value).getTime());
		if (Character.class == vType)
			return ((Character) value).toString();
		if (BigInteger.class == vType)
			return ((BigInteger) value).longValue(); //2020.10.1 change from int to long
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