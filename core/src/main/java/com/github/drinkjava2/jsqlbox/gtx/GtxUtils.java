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

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.DbContextUtils;
import com.github.drinkjava2.jsqlbox.DbException;
import com.github.drinkjava2.jtransactions.TransactionsException;

/**
 * Gtx public static methods
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public abstract class GtxUtils {// NOSONAR
	public static final String GTXID = "gtxid"; // gtx_id + gtx_logno is a compound PKEY
	public static final String GTXLOGNO = "gtxlogno";
	public static final String GTXTYPE = "gtxtype";// Operate type, can be update/exist/existStrict/insert/delete
	public static final String GTXDB = "gtxdb";// DB code No.
	public static final String GTXTB = "gtxtb";// table name or sharded table name.
	public static final String GTXENTITY = "gtxentity";// entity class full name
	public static final String GTXTOPIC = "gtxtopic";// gtx topic used for lock server sharding

	// Below are possible GTXTYPE values
	public static final String INSERT = "INSERT";
	public static final String EXISTID = "EXISTID";
	public static final String EXISTSTRICT = "EXSTRICT";
	public static final String BEFORE = "BEFORE";
	public static final String DELETE = "DELETE";
	public static final String AFTER = "AFTER";

	private static String[] topicShardingParam = null;

	/**
	 * According entity's sharding setting, create lock record in GtxInfo
	 */
	public static void reg(DbContext dbCtx, Object entity, String operType) {
		if (entity instanceof GtxId)
			return;
		GtxLog log = new GtxLog(operType, entity);
		dbCtx.getGtxInfo().getGtxLogList().add(log);
		GtxInfo gtxInfo = dbCtx.getGtxInfo();
		List<GtxLock> locks = gtxInfo.getGtxLockList();
		TableModel model = TableModelUtils.entity2ReadOnlyModel(entity.getClass());

		// calculate sharded Db Code if have
		Integer dbCode = DbContextUtils.getShardedDbCodeByBean(dbCtx, entity);
		TransactionsException.assureNotNull(dbCode, "dbCode can not determine for entity: " + entity);
		log.setGtxDB(dbCode);

		// calculate sharded table name if have
		String shardedTB = DbContextUtils.getShardedTbByBean(dbCtx, entity);
		log.setGtxTB(shardedTB);

		// calculate id value
		StringBuilder idSB = new StringBuilder();
		for (ColumnModel col : model.getPKeyColumns()) {
			if (idSB.length() > 0)
				idSB.append("|");
			idSB.append(DbContextUtils.readValueFromBeanFieldOrTail(col, entity));
		}
		String id = idSB.toString();

		boolean lockExisted = false;
		for (GtxLock lk : locks) // add locks in memory, if already have then do not add again
			if (lk.getDb().equals(dbCode) && lk.getTb().equalsIgnoreCase(shardedTB)
					&& lk.getEntityId().equalsIgnoreCase(id)) {
				lockExisted = true;
				break;
			}
		if (!lockExisted) {
			GtxLock lock = new GtxLock();
			lock.setDb(dbCode);
			lock.setTb(shardedTB);
			lock.setEntityId(id);
			lock.setEntityTb(model.getTableName());
			lock.setGid(gtxInfo.getGtxId().getGid());
			locks.add(lock);
		}
	}

	/**
	 * Save GTX lock and log
	 */
	public static void saveLockAndLog(DbContext lockCtx, GtxInfo gtxInfo) throws Exception {
		DbException.assureNotNull(gtxInfo.getGtxId(), "GtxId not set");

		DbContext locker = lockCtx;
		if (gtxInfo.getLockDb() != null)
			locker = (DbContext) lockCtx.getMasters()[gtxInfo.getLockDb()];

		locker.getConnectionManager().startTransaction(Connection.TRANSACTION_READ_COMMITTED);
		try {
			locker.eInsert(gtxInfo.getGtxId());
			Long logNo = 1L;
			for (GtxLog gtxLog : gtxInfo.getGtxLogList()) {
				Object entity = gtxLog.getEntity();
				TableModel md = GtxUtils.entity2GtxLogModel(entity.getClass());
				md.getColumnByColName(GtxUtils.GTXID).setValue(gtxInfo.getGtxId().getGid());
				md.getColumnByColName(GtxUtils.GTXLOGNO).setValue(logNo++);
				md.getColumnByColName(GtxUtils.GTXTYPE).setValue(gtxLog.getLogType());
				md.getColumnByColName(GtxUtils.GTXDB).setValue(gtxLog.getGtxDB());
				md.getColumnByColName(GtxUtils.GTXTB).setValue(gtxLog.getGtxTB());
				md.getColumnByColName(GtxUtils.GTXENTITY).setValue(entity.getClass().getName());
				locker.eInsert(entity, md);
			}
			for (GtxLock lock : gtxInfo.getGtxLockList())
				locker.eInsert(lock);
			locker.getConnectionManager().commitTransaction();
		} catch (Exception e) {
			locker.getConnectionManager().rollbackTransaction();
			throw e;
		}
	}

	/** Delete GTX lock and log */
	public static void deleteLockAndLog(DbContext lockCtx, GtxInfo gtxInfo) throws Exception {
		DbContext locker = lockCtx;
		if (gtxInfo.getLockDb() != null)
			locker = (DbContext) lockCtx.getMasters()[gtxInfo.getLockDb()];
		locker.getConnectionManager().startTransaction(Connection.TRANSACTION_READ_COMMITTED);
		try {
			String gid = gtxInfo.getGtxId().getGid();
			locker.eDelete(gtxInfo.getGtxId());// delete GtxID! here will auto sharding
			locker.pExecute("delete from gtxlock where gid=?", gid, DB.shardDB(gid));
			Set<String> tableSet = new HashSet<String>();
			for (GtxLog gtxLog : gtxInfo.getGtxLogList()) {
				Object entity = gtxLog.getEntity();
				TableModel md = TableModelUtils.entity2ReadOnlyModel(entity.getClass());
				tableSet.add(md.getTableName().toLowerCase());
			}
			for (String table : tableSet)
				locker.iExecute("delete from ", table, " where ", GTXID, "=?", DB.param(gid),
						DB.shardDB(gid));
			locker.getConnectionManager().commitTransaction();
		} catch (Exception e) {
			locker.getConnectionManager().rollbackTransaction();
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
		t.column(GTXID).CHAR(32).id().setValueExist(true); // gtxid + gtxlogno is compound PKEY
		t.column(GTXLOGNO).LONG().id().setValueExist(true);
		t.column(GTXTYPE).CHAR(8).setValueExist(true);
		t.column(GTXDB).INTEGER().setValueExist(true);
		t.column(GTXTB).VARCHAR(64).setValueExist(true);// oracle limit is 30
		t.column(GTXENTITY).VARCHAR(250).setValueExist(true);
		ColumnModel topicCol = t.column(GTXTOPIC).VARCHAR(50).setValueExist(true);
		if (topicShardingParam != null)
			topicCol.setShardDatabase(topicShardingParam);
		TableModel.sortColumns(t.getColumns());
		return t;
	}
}