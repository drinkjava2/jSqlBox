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

import static com.github.drinkjava2.jdbpro.JDBPRO.param;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.ColumnListHandler;

import com.github.drinkjava2.jdbpro.log.DbProLog;
import com.github.drinkjava2.jdbpro.log.DbProLogFactory;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.Tail;
import com.github.drinkjava2.jtransactions.TransactionsException;
import com.github.drinkjava2.jtransactions.TxResult;
import com.github.drinkjava2.jtransactions.manual.ManualTxConnectionManager;

/**
 * GtxUnlockServ used to unlock GTX
 * 
 * @author Yong Zhu
 */
public abstract class GtxUnlockServ {// NOSONAR
	protected static final DbProLog logger = DbProLogFactory.getLog(GtxUnlockServ.class);
	private static final Map<String, String> gtxIdCache = new HashMap<String, String>();
	private static SqlBoxContext lockCtx;
	private static SqlBoxContext[] ctxs;

	private static void initContext(SqlBoxContext userCtx) {
		GtxConnectionManager lockCM = (GtxConnectionManager) userCtx.getConnectionManager();
		lockCtx = lockCM.getLockCtx();
		ctxs = new SqlBoxContext[userCtx.getMasters().length];
		for (int i = 0; i < userCtx.getMasters().length; i++) {
			SqlBoxContext userCtxArr = (SqlBoxContext) userCtx.getMasters()[i];
			ctxs[i] = new SqlBoxContext(userCtxArr.getDataSource());
			ctxs[i].setName(userCtxArr.getName());
			ctxs[i].setConnectionManager(new ManualTxConnectionManager());
			ctxs[i].setDbCode(userCtxArr.getDbCode());
			ctxs[i].setDialect(userCtxArr.getDialect());
			ctxs[i].setShardingTools(userCtxArr.getShardingTools());
			ctxs[i].setAllowShowSQL(userCtxArr.getAllowShowSQL());
			ctxs[i].setMasters(ctxs);
		}
		gtxIdCache.clear();
	}

	/**
	 * Unlock lock servers very intervalSecond
	 * 
	 * @param ctx
	 *            one DB SqlBoxContext
	 * @param intervalSecond
	 *            interval seconds to check and unlock
	 * @param maxLoopTimes
	 *            max loop times, if is 0 will never stop
	 */
	public static void start(SqlBoxContext ctx, long intervalSecond, long maxLoopTimes) {// NOSONAR
		initContext(ctx);
		long loop = 0;
		do {
			List<GtxId> gtxIdList = lockCtx.eFindAll(GtxId.class);
			for (GtxId gtxId : gtxIdList) {
				String id = gtxId.getGid();
				if (gtxIdCache.containsKey(id)) {
					if ("LOADED".equals(gtxIdCache.get(id))) {
						gtxIdCache.put(id, "TRY UNLOCK"); // only try once
						try {
							if (unlockOne(id)) {// second time unlock it
								gtxIdCache.remove(id);
								logger.info("Unlocked success for gtxid:" + id);
							} else {
								gtxIdCache.put(id, "UNLOCK FAIL"); // only try once
								logger.info("Unlock fail for gtxid:" + id);
							}
						} catch (Exception e) {
							gtxIdCache.put(id, "UNLOCK FAIL"); // only try once
							logger.warn("Unlock fail exception, for gtxid:" + id, e);
						}
					}
				} else
					gtxIdCache.put(id, "LOADED"); // first time cache the gtxId
			}
			try {
				Thread.sleep(intervalSecond * 1000);
			} catch (InterruptedException e) {// NOSONAR
				throw new TransactionsException(e);
			}
			loop++;
			if (loop > Long.MAX_VALUE)
				loop = 0;
		} while (maxLoopTimes <= 0 || loop < maxLoopTimes);
	}

	/**
	 * Force unlock a given gtxId, usually used on unit test only, return true if
	 * success, otherwise return false means require manually unlock
	 */
	public static boolean forceUnlock(SqlBoxContext ctx, String gtxId) {
		initContext(ctx);
		return unlockOne(gtxId);
	}

	/**
	 * Force unlock a given TxResult, usually used on unit test only, return true if
	 * success, otherwise return false means require manually unlock
	 */
	public static boolean forceUnlock(SqlBoxContext ctx, TxResult txResult) {
		initContext(ctx);
		try {
			return unlockOne(txResult.getGid());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static boolean unlockOne(String gid) {
		GtxId lockGid = null; // First check and read if gtxId exist on Lock Server
		lockGid = lockCtx.eLoadByIdTry(GtxId.class, gid);
		if (lockGid == null) {
			logger.error("Can not access lock server");
			return false;
		}
		List<List<Integer>> DbLstLst = lockCtx.iExecute("select distinct(db) from gtxlock where gid=?", param(gid),
				new ColumnListHandler<Integer>());
		List<Integer> dbList = DbLstLst.get(0);

		for (Integer db : dbList) {
			executeUndo(db, gid);
		}

		lockCtx.eDeleteById(GtxId.class, gid); // if no error, means all unlocked, can remove gid

		return true;// So far, all databases are OK
	}

	private static void executeUndo(Integer db, String gid) {
		String _gid = ctxs[db].pQueryForString("select gid from gtxid where gid=?", gid);
		if (!gid.equals(_gid))
			return;// no undo log or undo log already executed
		List<List<String>> tmp = lockCtx.iExecute("select distinct(entityTb) from gtxlock where gid=?", param(gid),
				" and db=?", param(db), new ColumnListHandler<String>());
		List<String> tbList = tmp.get(0);
		ctxs[db].startTrans();
		try {
			for (String tb : tbList) {
				List<Tail> oneRecord = lockCtx.eFindBySQL(Tail.class, "select * from ", tb, " where gtxdb=?", param(db),
						" and gtxid=?", param(gid), " order by GTXLOGNO desc");
				for (Tail tail : oneRecord) {
					undo(db, tb, tail);
				}
			}
			ctxs[db].commitTrans();
		} catch (Exception e) {
			ctxs[db].rollbackTrans();
			throw new TransactionsException(e);
		}
	}

	private static void undo(Integer db, String tb, Tail tail)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String gtxtyp = tail.getTail(GtxUtils.GTXTYPE);
		String entityClassName = tail.getTail(GtxUtils.GTXENTITY);
		Class<?> entityClass = Class.forName(entityClassName);
		Object entity = tailToEntityBean(tail, entityClass);

		if (GtxUtils.INSERT.equals(gtxtyp))
			ctxs[db].eDelete(entity);
		else if (GtxUtils.DELETE.equals(gtxtyp))
			ctxs[db].eInsert(entity);
		else if (GtxUtils.AFTER.equals(gtxtyp))
			ctxs[db].eExistStrict(entity);
		else if (GtxUtils.BEFORE.equals(gtxtyp))
			ctxs[db].eUpdate(entity);
		else if (GtxUtils.EXISTID.equals(gtxtyp))
			ctxs[db].eExist(entity);
		else if (GtxUtils.EXISTSTRICT.equals(gtxtyp))
			ctxs[db].eExistStrict(entity);
	}

	private static Object tailToEntityBean(Tail tail, Class<?> entityClass)
			throws InstantiationException, IllegalAccessException {
		Object entity = entityClass.newInstance();
		TableModel model = TableModelUtils.entity2ReadOnlyModel(entityClass);
		for (ColumnModel col : model.getColumns()) {
			String fieldName = col.getEntityField();
			if (tail.tails().containsKey(fieldName))
				try {
					Method writeMethod = ClassCacheUtils.getClassFieldWriteMethod(entityClass, fieldName);
					writeMethod.invoke(entity, tail.tails().get(fieldName));
				} catch (Exception e) {
					throw new TransactionsException("FieldName '" + fieldName + "' can not write.", e);
				}
		}
		return entity;
	}
}
