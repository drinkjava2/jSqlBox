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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;

/**
 * This is an entity class to store gtxId (global transaction ID)
 * If no gtx sharding key, there is only one gtxid table 
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxId {
	@Id
	private String gtxId;
	private Timestamp createTime;
	private Integer logIndex = 0;
	private List<GtxLock> gtxLockList = new ArrayList<GtxLock>();

	public String getGtxId() {
		return gtxId;
	}

	public void setGtxId(String gtxId) {
		this.gtxId = gtxId;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Integer getLogIndex() {
		return logIndex;
	}

	public void setLogIndex(Integer logIndex) {
		this.logIndex = logIndex;
	}

	public List<GtxLock> getGtxLockList() {
		return gtxLockList;
	}

	public void setGtxLockList(List<GtxLock> gtxLockList) {
		this.gtxLockList = gtxLockList;
	}

}