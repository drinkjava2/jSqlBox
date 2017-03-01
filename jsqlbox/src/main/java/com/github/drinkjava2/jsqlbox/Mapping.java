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

/**
 * This class is for store Sql Mapping
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class Mapping {
	MappingType mappingType;
	Object thisEntity;
	String thisField;
	String thisPropertyName;
	Object otherEntity;
	String otherfield;
	String otherPropertyName;

	public MappingType getMappingType() {
		return mappingType;
	}

	public void setMappingType(MappingType mappingType) {
		this.mappingType = mappingType;
	}

	public Object getThisEntity() {
		return thisEntity;
	}

	public void setThisEntity(Object thisEntity) {
		this.thisEntity = thisEntity;
	}

	public String getThisField() {
		return thisField;
	}

	public void setThisField(String thisField) {
		this.thisField = thisField;
	}

	public String getThisPropertyName() {
		return thisPropertyName;
	}

	public void setThisPropertyName(String thisPropertyName) {
		this.thisPropertyName = thisPropertyName;
	}

	public Object getOtherEntity() {
		return otherEntity;
	}

	public void setOtherEntity(Object otherEntity) {
		this.otherEntity = otherEntity;
	}

	public String getOtherfield() {
		return otherfield;
	}

	public void setOtherfield(String otherfield) {
		this.otherfield = otherfield;
	}

	public String getOtherPropertyName() {
		return otherPropertyName;
	}

	public void setOtherPropertyName(String otherPropertyName) {
		this.otherPropertyName = otherPropertyName;
	}

	/**
	 * Get debug info of Mapping instance
	 */
	public String getDebugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("============").append("\r\n");
		sb.append("mappingType=" + mappingType).append("\r\n");
		sb.append("thisEntity=" + thisEntity).append("\r\n");
		sb.append("thisField=" + thisField).append("\r\n");
		sb.append("thisPropertyName=" + thisPropertyName).append("\r\n");
		sb.append("otherEntity=" + otherEntity).append("\r\n");
		sb.append("otherfield=" + otherfield).append("\r\n");
		sb.append("otherPropertyName=" + otherPropertyName).append("\r\n");
		return sb.toString();
	}
}
