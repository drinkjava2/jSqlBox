package com.github.drinkjava2.jsqlbox.jpa;

public class Column {
	// private String name = "";disable this
	private boolean unique = false;
	private boolean nullable = true;
	private boolean insertable = true;
	private boolean updatable = true;
	// private String columnDefinition = ""; disable this
	private int length = 255;
	private int precision = 0;
	private int scale = 0;
	// below fields are for JSQLBox
	private Class<?> propertyType;
	private Object value;

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Class<?> getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(Class<?> propertyType) {
		this.propertyType = propertyType;
	}

	public boolean isUnique() {
		return unique;
	}

	public Column setUnique(boolean unique) {
		this.unique = unique;
		return this;
	}

	public boolean isNullable() {
		return nullable;
	}

	public Column setNullable(boolean nullable) {
		this.nullable = nullable;
		return this;
	}

	public boolean isInsertable() {
		return insertable;
	}

	public Column setInsertable(boolean insertable) {
		this.insertable = insertable;
		return this;
	}

	public boolean isUpdatable() {
		return updatable;
	}

	public Column setUpdatable(boolean updatable) {
		this.updatable = updatable;
		return this;
	}

	public int getLength() {
		return length;
	}

	public Column setLength(int length) {
		this.length = length;
		return this;
	}

	public int getPrecision() {
		return precision;
	}

	public Column setPrecision(int precision) {
		this.precision = precision;
		return this;
	}

	public int getScale() {
		return scale;
	}

	public Column setScale(int scale) {
		this.scale = scale;
		return this;
	}

}