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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextUtils;
import com.github.drinkjava2.jsqlbox.SqlBoxException;
import com.github.drinkjava2.jtransactions.TransactionsException;
import com.mysql.jdbc.Connection;

/**
 * Gtx public static methods
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public abstract class GtxUtils {// NOSONAR
	public static final String GTX_GID = "gtxgid"; // gtx_id + gtx_logno is a compound PKEY
	public static final String GTX_LOGNO = "gtxlogno";
	public static final String GTX_TYP = "gtxtyp";// Operate type, can be update/exist/existStrict/insert/delete
	public static final String GTX_DB = "gtxdb";// DB code No.
	public static final String GTX_TB = "gtxtb";// Table name

	public static final String INSERT = "INSERT";
	public static final String EXIST = "EXIST";
	public static final String EXISTSTRICT = "STRICT";
	public static final String DELETE = "DELETE";
	public static final String UPDATE = "UPDATE";

	/**
	 * According entity's sharding setting, create lock record in GtxInfo
	 */
	public static void reg(SqlBoxContext ctx, Object entity, String operType) {
		if (entity instanceof GtxId)
			return;
		GtxLog log = new GtxLog(operType, entity);
		ctx.getGtxInfo().getGtxLogList().add(log);
		GtxInfo gtxInfo = ctx.getGtxInfo();
		List<GtxLock> locks = gtxInfo.getGtxLockList();
		TableModel model = TableModelUtils.entity2ReadOnlyModel(entity.getClass());

		// calculate sharded Db Code if have
		Integer dbCode = SqlBoxContextUtils.getShardedDbCodeByBean(ctx, entity);
		TransactionsException.assureNotNull(dbCode, "dbCode can not determine for entity: " + entity);
		log.setGtxDB(dbCode);

		// calculate sharded table name if have
		String  table = SqlBoxContextUtils.getShardedTbByBean(ctx, entity);
		log.setGtxTB(table);

		// calculate gid value
		StringBuilder idSB = new StringBuilder();
		for (ColumnModel col : model.getPKeyColumns()) {
			if (idSB.length() > 0)
				idSB.append("|");
			idSB.append(SqlBoxContextUtils.readValueFromBeanFieldOrTail(col, entity));
		}
		String id = idSB.toString();

		boolean lockExisted = false;
		for (GtxLock lk : locks) // add locks in memory, if already have then do not add again
			if (lk.getDb().equals(dbCode) && lk.getTb().equalsIgnoreCase(table)
					&& lk.getEntityId().equalsIgnoreCase(id)) {
				lockExisted = true;
				break;
			}
		if (!lockExisted) {
			GtxLock lock = new GtxLock();
			lock.setDb(dbCode);
			lock.setTb(table);
			lock.setEntityId(id);
			lock.setGid(gtxInfo.getGtxId().getGid());
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
			Long logNo = 1L;
			for (GtxLog gtxLog : gtxInfo.getGtxLogList()) {
				Object entity = gtxLog.getEntity();
				TableModel md = GtxUtils.entity2GtxLogModel(entity.getClass());
				md.getColumnByColName(GtxUtils.GTX_GID).setValue(gtxInfo.getGtxId().getGid());
				md.getColumnByColName(GtxUtils.GTX_LOGNO).setValue(logNo++);
				md.getColumnByColName(GtxUtils.GTX_TYP).setValue(gtxLog.getLogType());
				md.getColumnByColName(GtxUtils.GTX_DB).setValue(gtxLog.getGtxDB());
				md.getColumnByColName(GtxUtils.GTX_TB).setValue(gtxLog.getGtxTB());
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
			String gid = gtxInfo.getGtxId().getGid();
			gtxCtx.nExecute("delete from gtxid where gid=?", gid);
			gtxCtx.nExecute("delete from gtxlock where gtxid=?", gid);
			Set<String> tableSet = new HashSet<String>();
			for (GtxLog gtxLog : gtxInfo.getGtxLogList()) {
				Object entity = gtxLog.getEntity();
				TableModel md = TableModelUtils.entity2ReadOnlyModel(entity.getClass());
				tableSet.add(md.getTableName().toLowerCase());
			}
			for (String table : tableSet)
				gtxCtx.nExecute("delete from " + table + " where gtxid=?", gid);
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
		t.column(GTX_GID).CHAR(32).id().setValue(null); // gtx_id + gtx_logno is a compound PKEY
		t.column(GTX_LOGNO).LONG().id().setValue(null);
		t.column(GTX_TYP).CHAR(6).setValue(null);
		t.column(GTX_DB).INTEGER().setValue(null);
		t.column(GTX_TB).VARCHAR(50).setValue(null);
		TableModel.sortColumns(t.getColumns());
		return t;
	}
}