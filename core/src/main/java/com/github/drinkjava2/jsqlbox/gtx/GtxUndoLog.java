/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jsqlbox.gtx;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * A POJO will used to store GTX undo log
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@Table(name = "gtx_log")
public class GtxUndoLog {
	@Id
	private String id; // id for this GTX log
	private String gtxLockId; // Global transaction id
	private String entityClass; // entity class name
	private String sqlType; // SQL method types, can be INSERT/DELETE/....
	private String fieldNames; // field names of entity bean

	public static void config(TableModel model) {
		for (int i = 1; i <= 30; i++) {
			model.column("c" + i).STRING(400);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGtxLockId() {
		return gtxLockId;
	}

	public void setGtxLockId(String gtxLockId) {
		this.gtxLockId = gtxLockId;
	}

	public String getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}

	public String getSqlType() {
		return sqlType;
	}

	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}

	public String getFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(String fieldNames) {
		this.fieldNames = fieldNames;
	}

}