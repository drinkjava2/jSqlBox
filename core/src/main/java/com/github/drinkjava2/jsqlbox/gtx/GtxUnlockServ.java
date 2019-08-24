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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.ColumnListHandler;

import com.github.drinkjava2.jdbpro.log.DbProLog;
import com.github.drinkjava2.jdbpro.log.DbProLogFactory;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jtransactions.TransactionsException;
import com.github.drinkjava2.jtransactions.TxResult;

/**
 * GtxUnlockServ used to unlock GTX
 * 
 * @author Yong Zhu
 */
public abstract class GtxUnlockServ {// NOSONAR
	protected static final DbProLog logger = DbProLogFactory.getLog(GtxUnlockServ.class);
	private static final Map<String, String> gtxIdCache = new HashMap<String, String>();
	private static SqlBoxContext lockCtx;
	private static SqlBoxContext[] ctxArr;

	private static void initContext(SqlBoxContext userCtx) {
		GtxConnectionManager lockCM = (GtxConnectionManager) userCtx.getConnectionManager();
		lockCtx = lockCM.getLockCtx();
		ctxArr = new SqlBoxContext[userCtx.getMasters().length];
		for (int i = 0; i < userCtx.getMasters().length; i++) {
			SqlBoxContext userCtxArr = (SqlBoxContext) userCtx.getMasters()[i];
			ctxArr[i] = new SqlBoxContext(userCtxArr.getDataSource());
			ctxArr[i].setName(userCtxArr.getName());
			ctxArr[i].setDbCode(userCtxArr.getDbCode());
			ctxArr[i].setDialect(userCtxArr.getDialect());
			ctxArr[i].setShardingTools(userCtxArr.getShardingTools());
			ctxArr[i].setAllowShowSQL(userCtxArr.getAllowShowSQL());
			ctxArr[i].setMasters(ctxArr);
		}
		gtxIdCache.clear();
	}

	/**
	 * Unlock lock servers very intervalSecond
	 * 
	 * @throws InterruptedException
	 */
	public static void start(SqlBoxContext ctx, long intervalSecond) throws InterruptedException {// NOSONAR
		initContext(ctx);
		do {
			List<GtxId> gtxIdList = lockCtx.eFindAll(GtxId.class);
			for (GtxId gtxId : gtxIdList) {
				String id = gtxId.getGid();
				if (gtxIdCache.containsKey(id)) {
					if ("LOADED".equals(gtxIdCache.get(id))) {
						gtxIdCache.put(id, "TRY UNLOCK"); // only try once
						try {
							if (doUnlock(id)) {// second time unlock it
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
			Thread.sleep(intervalSecond * 1000);
		} while (true);
	}

	/**
	 * Force unlock a given gtxId, usually used on unit test only, return true if
	 * success, otherwise return false means require manually unlock
	 */
	public static boolean forceUnlock(SqlBoxContext ctx, String gtxId) {
		initContext(ctx);
		return doUnlock(gtxId);
	}

	/**
	 * Force unlock a given TxResult, usually used on unit test only, return true if
	 * success, otherwise return false means require manually unlock
	 */
	public static boolean forceUnlock(SqlBoxContext ctx, TxResult txResult) {
		initContext(ctx);
		return doUnlock(txResult.getGid());
	}

	private static boolean doUnlock(String gid) {
		GtxId lockGid = null; // First check and read if gtxId exist on Lock Server
		List<Integer> dbs;
		try {
			lockGid = lockCtx.eLoadByIdTry(GtxId.class, gid);
		} catch (Exception e) {
			logger.error("Can not access lock server", e);
			return false;
		}
		if (lockGid == null) {
			logger.warn("Try to unlock an un-exist gid:" + lockGid);
			return true;
		} else {
			lockCtx.eUpdate(lockGid.setUnlockTry(lockGid.getUnlockTry() + 1));
			List<List<Integer>> result = lockCtx.iExecute("select distinct(db) from gtxlock where gid=?", param(gid),
					new ColumnListHandler<Integer>());
			dbs = result.get(0);
		}

		try {
			for (int i = 0; i < dbs.size(); i++) {
				executeUndo(dbs.get(i), gid);
			}
		} catch (Exception e) {
			logger.error("Can not execute undo operate", e);
			return false;
		}
		// So far, all databases are OK
		return true;
	}

	private static void executeUndo(int dbCode, String gid) {
		SqlBoxContext ctx = ctxArr[dbCode];
		//List<Object> entityLog=lockCtx.eFindAll(entityClass, optionItems)
		ctx.startTrans();
		try {
			
			ctx.commitTrans();
		} catch (Exception e) {
			ctx.rollbackTrans();
			throw new TransactionsException(e);
		}

	}

}
