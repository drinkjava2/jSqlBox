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
package com.github.drinkjava2.jsqlbox.entitynet;

/**
 * ParentRelationShip of node determined by 3 dimensions: fkeyColumns, refId,
 * refTable
 * 
 * For example: <br/>
 * teacher_firstName_lastname, Sam_Zhu , Teacher.class <br/>
 * teacher_firstName_lastname, Tom_Li , SuperTeacher.class <br/>
 * teacher_firstName_lastname, Jeff_Wang , SuperTeacher.class <br/>
 * 
 * @author Yong Zhu 
 * @since 1.0.0
 */
public class ParentRelation {
	String refColumns;
	String parentTable;
	String parentId;

	public ParentRelation(String columns, String parentId, String parentTable) {
		EntityNetException.assureNotEmpty(columns, "In ParentRelation, columns can not be empty");
		EntityNetException.assureNotEmpty(parentId, "In ParentRelation, parentId can not be empty");
		EntityNetException.assureNotEmpty(parentTable, "In ParentRelation, parentTable can not be empty");
		this.refColumns = columns;
		this.parentId = parentId;
		this.parentTable = parentTable;
	}

	public boolean sameas(ParentRelation p) {
		return this.refColumns.equalsIgnoreCase(p.getRefColumns())
				&& this.getParentTable().equalsIgnoreCase(p.getParentTable())
				&& this.parentId.equalsIgnoreCase(p.getParentId());
	}

	public String getRefColumns() {
		return refColumns;
	}

	public void setRefColumns(String refColumns) {
		this.refColumns = refColumns;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getParentTable() {
		return parentTable;
	}

	public void setParentTable(String parentTable) {
		this.parentTable = parentTable;
	}

}
