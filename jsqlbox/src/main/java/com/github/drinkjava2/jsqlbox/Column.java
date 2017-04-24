/**
* Copyright (C) 2016 Yong Zhu.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.github.drinkjava2.jsqlbox;

import com.github.drinkjava2.jsqlbox.id.IdGenerator;

/**
 * Column define, similar to JPA but added some fields
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @version 1.0.0
 * @since 1.0
 */
public class Column {
	// JPA also use below fields:
	private Boolean nullable = true;// readOnly, determined by Database
	private Boolean insertable = true;
	private Boolean updatable = true;
	private Boolean enable = true;// if set false will ignore any operation of
									// this field
	private int length = 255;
	private int precision = 0;
	private int scale = 0;
	// Below fields are for JSQLBox only:
	private String columnName = ""; // = columnDefinition in JPA
	private Boolean entityID = false;// = unique in JPA
	private Boolean autoIncreament = false;
	private Class<?> propertyType;
	private String propertyTypeName;
	private Object propertyValue;
	private String readMethodName;
	private String writeMethodName;
	private IdGenerator idGenerator;
	private String fieldID = "";

	public String getFieldID() {
		return fieldID;
	}

	public void setFieldID(String fieldID) {
		this.fieldID = fieldID;
	}

	public Boolean isNullable() {
		return nullable;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	public Boolean isInsertable() {
		return insertable;
	}

	public void setInsertable(Boolean insertable) {
		this.insertable = insertable;
	}

	public Boolean isUpdatable() {
		return updatable;
	}

	public void setUpdatable(Boolean updatable) {
		this.updatable = updatable;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
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

	public String getReadMethodName() {
		return readMethodName;
	}

	public void setReadMethodName(String readMethodName) {
		this.readMethodName = readMethodName;
	}

	public String getWriteMethodName() {
		return writeMethodName;
	}

	public void setWriteMethodName(String writeMethodName) {
		this.writeMethodName = writeMethodName;
	}

	public IdGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	public Object getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(Object propertyValue) {
		this.propertyValue = propertyValue;
	}

	public Boolean getAutoIncreament() {
		return autoIncreament;
	}

	public void setAutoIncreament(Boolean autoIncreament) {
		this.autoIncreament = autoIncreament;
	}

	public Boolean getEntityID() {
		return entityID;
	}

	public void setEntityID(Boolean entityID) {
		this.entityID = entityID;
	}

	public Boolean isEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public String getDebugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("nullable=" + nullable).append("\r\n");
		sb.append("insertable=" + insertable).append("\r\n");
		sb.append("updatable=" + updatable).append("\r\n");
		sb.append("length=" + length).append("\r\n");
		sb.append("precision=" + precision).append("\r\n");
		sb.append("scale=" + scale).append("\r\n");
		sb.append("columnName =" + columnName).append("\r\n");
		sb.append("entityID=" + entityID).append("\r\n");
		sb.append("autoIncreament=" + autoIncreament).append("\r\n");
		sb.append("propertyType=" + propertyType).append("\r\n");
		sb.append("propertyTypeName=" + propertyTypeName).append("\r\n");
		sb.append("propertyValue=" + propertyValue).append("\r\n");
		sb.append("readMethodName=" + readMethodName).append("\r\n");
		sb.append("writeMethodName=" + writeMethodName).append("\r\n");
		sb.append("idGenerator=" + idGenerator).append("\r\n");
		sb.append("fieldID=" + fieldID).append("\r\n");
		sb.append("\r\n");
		return sb.toString();
	}
}