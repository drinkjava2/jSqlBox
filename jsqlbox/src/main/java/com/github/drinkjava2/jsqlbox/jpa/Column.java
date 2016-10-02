package com.github.drinkjava2.jsqlbox.jpa;

public class Column {
	String name = "";
	boolean unique = false;
	boolean nullable = true;
	boolean insertable = true;
	boolean updatable = true;
	String columnDefinition = "";
	int length = 255;
	int precision = 0;
	int scale = 0;

	public String getName() {
		return name;
	}

	public Column setName(String name) {
		this.name = name;
		return this;
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

	public String getColumnDefinition() {
		return columnDefinition;
	}

	public Column setColumnDefinition(String columnDefinition) {
		this.columnDefinition = columnDefinition;
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