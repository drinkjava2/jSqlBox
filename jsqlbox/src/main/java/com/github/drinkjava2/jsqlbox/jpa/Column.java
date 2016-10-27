package com.github.drinkjava2.jsqlbox.jpa;

import java.lang.reflect.Method;

public class Column {
	private String name = "";
	private boolean unique = false;
	private boolean nullable = true;
	private boolean insertable = true;
	private boolean updatable = true;
	private String columnDefinition = "";
	private int length = 255;
	private int precision = 0;
	private int scale = 0;
	// below fields are for JSQLBox
	private boolean primeKey = false;
	private String foreignKey = "";
	private Class<?> propertyType;
	private String propertyTypeName;
	private Method readMethod = null;
	private Method writeMethod = null;
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isUnique() {
		return unique;
	}
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	public boolean isNullable() {
		return nullable;
	}
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	public boolean isInsertable() {
		return insertable;
	}
	public void setInsertable(boolean insertable) {
		this.insertable = insertable;
	}
	public boolean isUpdatable() {
		return updatable;
	}
	public void setUpdatable(boolean updatable) {
		this.updatable = updatable;
	}
	public String getColumnDefinition() {
		return columnDefinition;
	}
	public void setColumnDefinition(String columnDefinition) {
		this.columnDefinition = columnDefinition;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getPrecision() {
		return precision;
	}
	public void setPrecision(int precision) {
		this.precision = precision;
	}
	public int getScale() {
		return scale;
	}
	public void setScale(int scale) {
		this.scale = scale;
	}
	public boolean isPrimeKey() {
		return primeKey;
	}
	public void setPrimeKey(boolean primeKey) {
		this.primeKey = primeKey;
	}
	public String getForeignKey() {
		return foreignKey;
	}
	public void setForeignKey(String foreignKey) {
		this.foreignKey = foreignKey;
	}
	public Class<?> getPropertyType() {
		return propertyType;
	}
	public void setPropertyType(Class<?> propertyType) {
		this.propertyType = propertyType;
	}
	public String getPropertyTypeName() {
		return propertyTypeName;
	}
	public void setPropertyTypeName(String propertyTypeName) {
		this.propertyTypeName = propertyTypeName;
	}
	public Method getReadMethod() {
		return readMethod;
	}
	public void setReadMethod(Method readMethod) {
		this.readMethod = readMethod;
	}
	public Method getWriteMethod() {
		return writeMethod;
	}
	public void setWriteMethod(Method writeMethod) {
		this.writeMethod = writeMethod;
	}
 

}