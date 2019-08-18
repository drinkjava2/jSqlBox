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

import java.util.List;

import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextUtils;
import com.github.drinkjava2.jsqlbox.SqlBoxException;
import com.mysql.jdbc.Connection;

/**
 * Gtx public static methods
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public abstract class GtxUtils {// NOSONAR
	public static final String GTX_LOG_ID = "gtx_log_id";// log number, this is the P-Key of this log table
	public static final String GTX_ID = "gtx_id";
	public static final String GTX_TYPE = "gtx_type";// tx log type, can be update/exist/existStrict/insert/delete

	public static void logInsert(SqlBoxContext ctx, Object entity) {
		if (entity instanceof GtxId)
			return;
		ctx.getGtxInfo().getGtxLogList().add(new GtxLog("insert", entity));
		addLockInGtxInfo(ctx, entity);
	}

	public static void logExist(SqlBoxContext ctx, Object entity) {
		if (entity instanceof GtxId)
			return;
		ctx.getGtxInfo().getGtxLogList().add(new GtxLog("exist", entity));
		addLockInGtxInfo(ctx, entity);
	}

	public static void logExistStrict(SqlBoxContext ctx, Object entity) {
		if (entity instanceof GtxId)
			return;
		ctx.getGtxInfo().getGtxLogList().add(new GtxLog("existStrict", entity));
		addLockInGtxInfo(ctx, entity);
	}

	public static void logDelete(SqlBoxContext ctx, Object entity) {
		if (entity instanceof GtxId)
			return;
		ctx.getGtxInfo().getGtxLogList().add(new GtxLog("delete", entity));
		addLockInGtxInfo(ctx, entity);
	}

	public static void logUpdate(SqlBoxContext ctx, Object entity) {
		if (entity instanceof GtxId)
			return;
		ctx.getGtxInfo().getGtxLogList().add(new GtxLog("update", entity));
		addLockInGtxInfo(ctx, entity);
	}

	/**
	 * According entity's sharding setting, create lock record in GtxInfo
	 */
	public static void addLockInGtxInfo(SqlBoxContext ctx, Object entity) {
		GtxInfo gtxInfo = ctx.getGtxInfo();
		List<GtxLock> locks = gtxInfo.getGtxLockList();
		TableModel model = TableModelUtils.entity2ReadOnlyModel(entity.getClass());

		// calculate sharded Db Code if have
		Integer dbCode = SqlBoxContextUtils.getShardedDBCode(ctx, model, entity.getClass());
		if (dbCode == null)
			dbCode = -1;

		// calculate sharded table name if have
		Integer tbCode = SqlBoxContextUtils.getShardedTBCode(ctx, model, entity.getClass());
		String table = model.getTableName();
		if (tbCode != null)
			table += "_" + tbCode;

		// calculate id value
		StringBuilder idSB = new StringBuilder();
		for (ColumnModel col : model.getPKeyColsSortByColumnName()) {
			if (idSB.length() > 0)
				idSB.append("|");
			idSB.append(SqlBoxContextUtils.readValueFromBeanFieldOrTail(col, entity));
		}
		String id = idSB.toString();

		boolean lockExisted = false;
		for (GtxLock lk : locks) // add locks in memory, if already have then do not add again
			if (lk.getDbCode().equals(dbCode) && lk.getTbName().equalsIgnoreCase(table)
					&& lk.getEntityId().equalsIgnoreCase(id)) {
				lockExisted = true;
				break;
			}
		if (!lockExisted) {
			GtxLock lock = new GtxLock();
			lock.setDbCode(dbCode);
			lock.setTbName(table);
			lock.setEntityId(id);
			lock.setGtxId(gtxInfo.getGtxId().getId());
			locks.add(lock);
		}
	}

	/**
	 * Save GTX lock and log
	 */
	public static void saveLockAndLog(SqlBoxContext gtxCtx, GtxInfo gtxInfo) throws Exception {
		SqlBoxException.assureNotNull(gtxInfo.getGtxId(), "GtxId not set");
		gtxCtx.getConnectionManager().startTransaction(Connection.TRANSACTION_READ_COMMITTED);
		try {
			gtxCtx.eInsert(gtxInfo.getGtxId());
			Long logId = 1L;
			for (GtxLog gtxLog : gtxInfo.getGtxLogList()) {
				Object entity = gtxLog.getEntity();
				TableModel md = GtxUtils.entity2GtxLogModel(entity.getClass());
				md.getColumnByColName(GtxUtils.GTX_ID).setValue(gtxInfo.getGtxId().getId());
				md.getColumnByColName(GtxUtils.GTX_LOG_ID).setValue(logId++);
				md.getColumnByColName(GtxUtils.GTX_TYPE).setValue(gtxLog.getLogType());
				gtxCtx.eInsert(entity, md);
			}

			for (GtxLock lock : gtxInfo.getGtxLockList())
				gtxCtx.eInsert(lock);
			gtxCtx.getConnectionManager().commitTransaction();
		} catch (Exception e) {
			gtxCtx.getConnectionManager().rollbackTransaction();
			throw e;
		}
	}

	/** Delete GTX lock and log */
	public static void deleteLockAndLog(SqlBoxContext gtxCtx, GtxInfo gtxInfo) throws Exception {
		gtxCtx.getConnectionManager().startTransaction(Connection.TRANSACTION_READ_COMMITTED);
		try {
			gtxCtx.eDelete(gtxInfo.getGtxId());
			Long logId = 1L;
			for (GtxLog gtxLog : gtxInfo.getGtxLogList()) {
				Object entity = gtxLog.getEntity();
				TableModel md = GtxUtils.entity2GtxLogModel(entity.getClass());
				md.getColumnByColName(GtxUtils.GTX_ID).setValue(gtxInfo.getGtxId().getId());
				md.getColumnByColName(GtxUtils.GTX_LOG_ID).setValue(logId++);
				md.getColumnByColName(GtxUtils.GTX_TYPE).setValue(gtxLog.getLogType());
				gtxCtx.eDelete(entity, md);
			}
			for (GtxLock lock : gtxInfo.getGtxLockList())
				gtxCtx.eDelete(lock);
			gtxCtx.getConnectionManager().commitTransaction();
		} catch (Exception e) {
			gtxCtx.getConnectionManager().rollbackTransaction();
			throw e;
		}
	}

	/**
	 * Convert an entity class to gtxLog entity class, i.e., add some columns for it
	 */
	public static TableModel entity2GtxLogModel(Class<?> entityClass) {
		TableModel t = TableModelUtils.entity2Model(entityClass);
		t.setIdGenerators(null);
		t.setIndexConsts(null);
		t.setUniqueConsts(null);
		for (ColumnModel col : t.getColumns()) {
			col.setPkey(false);
			col.setIdGenerationType(null);
			col.setIdGeneratorName(null);
			col.setShardTable(null);
			col.setShardDatabase(null);
			col.setShardDatabase(null);
			col.setShardTable(null);
		}
		t.column(GTX_ID).VARCHAR(32).id().setValue(null);
		t.column(GTX_LOG_ID).LONG().id().setValue(null); // ID is assigned by outside
		t.column(GTX_TYPE).VARCHAR(16).setValue(null);

		return t;
	}
}