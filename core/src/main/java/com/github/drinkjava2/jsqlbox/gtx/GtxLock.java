/*
 * Copyright (C) 2016 Original Author
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox.gtx;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;

/**
 * This is an entity class to save gtx locks
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxLock {
	@Id
	private Integer dbCode; // DB sharding code, 0, 1,2 ..., -1 means no sharding

	@Id
	private String tbName; // table name

	@Id
	private String entityId; // entity entityId value

	private String gtxId;

	public Integer getDbCode() {
		return dbCode;
	}

	public void setDbCode(Integer dbCode) {
		this.dbCode = dbCode;
	}

	public String getTbName() {
		return tbName;
	}

	public void setTbName(String tbName) {
		this.tbName = tbName;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getGtxId() {
		return gtxId;
	}

	public void setGtxId(String gtxId) {
		this.gtxId = gtxId;
	}

}