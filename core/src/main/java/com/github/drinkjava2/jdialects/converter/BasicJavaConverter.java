package com.github.drinkjava2.jdialects.converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import com.github.drinkjava2.jdialects.Java8DateUtils;
import com.github.drinkjava2.jsqlbox.DbException;

public class BasicJavaConverter implements JavaConverter {

	public static final BasicJavaConverter instance = new BasicJavaConverter();

	@Override
	public Object convert(Object value, Class<?> targetType) {
		if (value == null)
			return null;
		if (targetType == String.class)
			return value.toString();
		Class<?> vType = value.getClass();
		if (targetType.isAssignableFrom(vType))
			return value;
		if (vType == BigDecimal.class) {
			if (targetType == Integer.class || targetType == int.class)
				return ((BigDecimal) value).intValue();
			if (targetType == Long.class || targetType == long.class)
				return ((BigDecimal) value).longValue();
			if (targetType == Byte.class || targetType == byte.class)
				return ((BigDecimal) value).byteValue();
			if (targetType == Double.class || targetType == double.class)
				return ((BigDecimal) value).doubleValue();
			if (targetType == Float.class || targetType == float.class)
				return ((BigDecimal) value).floatValue();
			if (targetType == Short.class || targetType == short.class)
				return ((BigDecimal) value).shortValue();
			if (targetType == Boolean.class || targetType == boolean.class)
				return ((BigDecimal) value).byteValue() != 0;
		}
		if (vType == BigInteger.class) {
			if (targetType == Integer.class || targetType == int.class)
				return ((BigInteger) value).intValue();
			if (targetType == Long.class || targetType == long.class)
				return ((BigInteger) value).longValue();
			if (targetType == Byte.class || targetType == byte.class)
				return ((BigInteger) value).byteValue();
			if (targetType == Double.class || targetType == double.class)
				return ((BigInteger) value).doubleValue();
			if (targetType == Float.class || targetType == float.class)
				return ((BigInteger) value).floatValue();
			if (targetType == Short.class || targetType == short.class)
				return ((BigInteger) value).shortValue();
			if (targetType == Boolean.class || targetType == boolean.class)
				return ((BigInteger) value).byteValue() != 0;
		} else if (vType == Integer.class) {
			if (targetType == int.class)
				return ((Integer) value).intValue();
			if (targetType == Long.class || targetType == long.class)
				return ((Integer) value).longValue();
			if (targetType == Byte.class || targetType == byte.class)
				return ((Integer) value).byteValue();
			if (targetType == Double.class || targetType == double.class)
				return ((Integer) value).doubleValue();
			if (targetType == Float.class || targetType == float.class)
				return ((Integer) value).floatValue();
			if (targetType == Short.class || targetType == short.class)
				return ((Integer) value).shortValue();
		} else if (vType == Long.class) {
			if (targetType == Integer.class || targetType == int.class)
				return ((Long) value).intValue();
			if (targetType == long.class)
				return ((Long) value).longValue();
			if (targetType == Byte.class || targetType == byte.class)
				return ((Long) value).byteValue();
			if (targetType == Double.class || targetType == double.class)
				return ((Long) value).doubleValue();
			if (targetType == Float.class || targetType == float.class)
				return ((Long) value).floatValue();
			if (targetType == Short.class || targetType == short.class)
				return ((Long) value).shortValue();
			if (targetType == BigInteger.class)
				return BigInteger.valueOf((Long) value);
		} else if (vType == Double.class) {
			if (targetType == Integer.class || targetType == int.class)
				return ((Double) value).intValue();
			if (targetType == Long.class || targetType == long.class)
				return ((Double) value).longValue();
			if (targetType == Byte.class || targetType == byte.class)
				return ((Double) value).byteValue();
			if (targetType == double.class)
				return ((Double) value).doubleValue();
			if (targetType == Float.class || targetType == float.class)
				return ((Double) value).floatValue();
			if (targetType == Short.class || targetType == short.class)
				return ((Double) value).shortValue();
		} else if (vType == Float.class) {
			if (targetType == Integer.class || targetType == int.class)
				return ((Float) value).intValue();
			if (targetType == Long.class || targetType == long.class)
				return ((Float) value).longValue();
			if (targetType == Byte.class || targetType == byte.class)
				return ((Float) value).byteValue();
			if (targetType == Double.class || targetType == double.class)
				return ((Float) value).doubleValue();
			if (targetType == float.class)
				return ((Float) value).floatValue();
			if (targetType == Short.class || targetType == short.class)
				return ((Float) value).shortValue();
		} else if (vType == Short.class) {
			if (targetType == Integer.class || targetType == int.class)
				return ((Short) value).intValue();
			if (targetType == Long.class || targetType == long.class)
				return ((Short) value).longValue();
			if (targetType == Byte.class || targetType == byte.class)
				return ((Short) value).byteValue();
			if (targetType == Double.class || targetType == double.class)
				return ((Short) value).doubleValue();
			if (targetType == Float.class || targetType == float.class)
				return ((Short) value).floatValue();
			if (targetType == short.class)
				return ((Short) value).shortValue();
		} else if (vType == Byte.class) {
			if (targetType == Integer.class || targetType == int.class)
				return ((Byte) value).intValue();
			if (targetType == Long.class || targetType == long.class)
				return ((Byte) value).longValue();
			if (targetType == byte.class)
				return ((Byte) value).byteValue();
			if (targetType == Double.class || targetType == double.class)
				return ((Byte) value).doubleValue();
			if (targetType == Float.class || targetType == float.class)
				return ((Byte) value).floatValue();
			if (targetType == Short.class || targetType == short.class)
				return ((Byte) value).shortValue();
		} else if (vType == java.sql.Date.class) { // no need convert java.util.Date
			if (targetType == Timestamp.class)
				return new Timestamp(((java.sql.Date) value).getTime());
			if (targetType == java.sql.Time.class)
				return new java.sql.Time(((java.sql.Date) value).getTime());
			if (targetType == Calendar.class) {
				Calendar c = Calendar.getInstance();
				c.setTime((java.sql.Date) value);
				return c;
			}
		} else if (vType == java.sql.Time.class) {
			if (targetType == Timestamp.class)
				return new Timestamp(((java.sql.Time) value).getTime());
			if (targetType == java.sql.Date.class)
				return new java.sql.Date(((java.sql.Time) value).getTime());
		} else if (vType == Timestamp.class) {
			if (targetType == java.sql.Date.class)
				return new java.sql.Date(((Timestamp) value).getTime());
			if (targetType == java.sql.Time.class)
				return new java.sql.Time(((Timestamp) value).getTime());
			if (targetType == Calendar.class) {
				Calendar c = Calendar.getInstance();
				c.setTime(new java.util.Date(((Timestamp) value).getTime()));
				return c;
			}
		} else if (vType == java.util.Date.class) {
			if (targetType == Timestamp.class)
				return new Timestamp(((java.util.Date) value).getTime());
			if (targetType == java.sql.Date.class)
				return new java.util.Date(((java.util.Date) value).getTime());
			if (targetType == java.sql.Time.class)
				return new java.sql.Time(((java.util.Date) value).getTime());
			if (targetType == Calendar.class) {
				Calendar c = Calendar.getInstance();
				c.setTime((java.util.Date) value);
				return c;
			}
		} else if (vType == String.class) {
			if (targetType == char.class || targetType == Character.class) {
				return ((String) value).length() > 0 ? ((String) value).charAt(0) : '\u0000';
			}
		} else if (vType == Boolean.class) {
			return ((Boolean) value).booleanValue();
		}

		/*- JAVA8_BEGIN */
		if (vType == java.sql.Date.class) {
			if (targetType == java.time.LocalDate.class)
				return Java8DateUtils.sqlDate2localDate((java.sql.Date) value);
			if (targetType == java.time.LocalDateTime.class)
				return Java8DateUtils.sqlDate2localDateTime((java.sql.Date) value);
		} else if (vType == java.sql.Time.class) {
			if (targetType == java.time.LocalTime.class)
				return Java8DateUtils.sqlTime2LocalTime((java.sql.Time) value);
			if (targetType == java.time.OffsetTime.class)
				return Java8DateUtils.sqlTime2OffsetTime((java.sql.Time) value);
		} else if (vType == Timestamp.class) {
			if (targetType == java.time.LocalDate.class)
				return Java8DateUtils.sqlTimestamp2LocalDate((Timestamp) value);
			if (targetType == java.time.LocalTime.class)
				return Java8DateUtils.sqlTimestamp2LocalTime((Timestamp) value);
			if (targetType == java.time.Instant.class)
				return Java8DateUtils.sqlTimestamp2instant((Timestamp) value);
			if (targetType == java.time.LocalDateTime.class)
				return Java8DateUtils.sqlTimestamp2LocalDateTime((Timestamp) value);
			if (targetType == java.time.OffsetDateTime.class)
				return Java8DateUtils.sqlTimestamp2OffsetDateTime((Timestamp) value);
			if (targetType == java.time.ZonedDateTime.class)
				return Java8DateUtils.sqlTimestamp2ZonedDateTime((Timestamp) value);
			if (targetType == java.time.OffsetTime.class)
				return Java8DateUtils.sqlTimestamp2OffsetTime((Timestamp) value);
		} else if (vType == Date.class) {
			if (targetType == java.time.LocalDate.class)
				return Java8DateUtils.date2LocalDate((Date) value);
			if (targetType == java.time.LocalDateTime.class)
				return Java8DateUtils.date2LocalDateTime((Date) value);
		}
		/* JAVA8_END */
		String oracleTip = "oracle.sql.TIMESTAMP".equals(vType.getName()) // NOSONAR
				? "\nBelow setting may solve this Oracle JDBC compliant issue:\n" + "System.getProperties().setProperty(\"oracle.jdbc.J2EE13Compliant\", \"true\");"
				: "";
		throw new DbException("Can not convert jdbc type: '" + value.getClass() + "' with value '" + value + "' to jave type:" + targetType + oracleTip);
	}

}