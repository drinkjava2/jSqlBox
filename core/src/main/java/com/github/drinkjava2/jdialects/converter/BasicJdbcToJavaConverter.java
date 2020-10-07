package com.github.drinkjava2.jdialects.converter;

public class BasicJdbcToJavaConverter implements JdbcToJavaConverter {
	public static final BasicJdbcToJavaConverter instance = new BasicJdbcToJavaConverter();

	@Override
	public Object convert(Object value) {
		return value;
	}
}