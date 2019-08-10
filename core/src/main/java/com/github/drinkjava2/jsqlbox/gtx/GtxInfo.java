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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jtransactions.TxInfo;

/**
 * This is an entity class to store gtxId (global transaction ID) If no gtx
 * sharding key, there is only one gtxid table
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxInfo extends TxInfo {
	@Id
	protected String gtxId;

	@Column(insertable = false)
	protected Timestamp createTime;

	protected List<GtxLog> gtxLogList = null;

	protected List<GtxLock> gtxLockList = null;

	public static void config(TableModel t) {// This is jDialect's configuration method
		t.column("createTime").setDefaultValue("now ()");
	}

	public void saveGtxInfo(SqlBoxContext gtxCtx) {
		Long logId = 1L;
		for (GtxLog gtxLog : getGtxLogList()) {
			Object entity = gtxLog.getEntity();
			TableModel md = GtxUtils.entity2GtxLogModel(entity.getClass());
			md.getColumnByColName(GtxUtils.GTX_ID).setValue(gtxId);
			md.getColumnByColName(GtxUtils.GTX_LOG_ID).setValue(logId++);
			md.getColumnByColName(GtxUtils.GTX_TYPE).setValue(gtxLog.getLogType());
			gtxCtx.eInsert(entity, md);

		}
		Map<String, Object> locks = new HashMap<String, Object>();
		for (GtxLock lock : getGtxLockList()) {
			gtxCtx.eInsert(lock);
		}

	}

	public String getDebugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("gtxId=" + gtxId).append("\r");
		sb.append("createTime=" + createTime).append("\r");
		sb.append("gtxLockList=" + gtxLockList).append("\r");
		sb.append("gtxLogList=" + gtxLogList).append("\r");
		return sb.toString();
	}

	public List<GtxLock> getGtxLockList() {
		if (gtxLockList == null)
			gtxLockList = new ArrayList<GtxLock>();
		return gtxLockList;
	}

	public List<GtxLog> getGtxLogList() {
		if (gtxLogList == null)
			gtxLogList = new ArrayList<GtxLog>();
		return gtxLogList;
	}

	// getter & setter=========
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

	public void setGtxLogList(List<GtxLog> gtxLogList) {
		this.gtxLogList = gtxLogList;
	}

	public void setGtxLockList(List<GtxLock> gtxLockList) {
		this.gtxLockList = gtxLockList;
	}

}