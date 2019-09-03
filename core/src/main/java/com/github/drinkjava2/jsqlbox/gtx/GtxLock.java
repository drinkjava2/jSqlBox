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
	private Integer db; // DB sharding code, 0, 1, 2 ...

	@Id
	private String tb; // Table name or sharded table name like table_0

	@Id
	private String entityId; // entity entityId value

	private String entityTb; // Unsharded origin entity table name

	private String gid; // GTX Id
	private String topic; // topic is used for sharding

	public Integer getDb() {
		return db;
	}

	public void setDb(Integer db) {
		this.db = db;
	}

	public String getTb() {
		return tb;
	}

	public void setTb(String tb) {
		this.tb = tb;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getEntityTb() {
		return entityTb;
	}

	public void setEntityTb(String entityTb) {
		this.entityTb = entityTb;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

}