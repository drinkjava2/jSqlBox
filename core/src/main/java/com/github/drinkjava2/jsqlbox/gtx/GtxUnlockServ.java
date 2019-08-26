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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdbpro.log.DbProLog;
import com.github.drinkjava2.jdbpro.log.DbProLogFactory;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.Tail;
import com.github.drinkjava2.jtransactions.TransactionsException;
import com.github.drinkjava2.jtransactions.TxResult;
import com.github.drinkjava2.jtransactions.grouptx.GroupTxConnectionManager;

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
			ctxArr[i].setConnectionManager(GroupTxConnectionManager.instance());
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
			Thread.sleep(intervalSecond * 1000);
		} while (true);
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
		return unlockOne(txResult.getGid());
	}

	private static boolean unlockOne(String gid) {
		GtxId lockGid = null; // First check and read if gtxId exist on Lock Server
		lockGid = lockCtx.eLoadByIdTry(GtxId.class, gid);
		if (lockGid == null) {
			logger.error("Can not access lock server");
			return false;
		}
		List<GtxLock> locks = lockCtx.eFindBySQL(GtxLock.class, "select entityTb from GtxLock where gid=?", param(gid),
				" group by entityTb");

		if (locks.size() > 0)
			executeUndo(locks, gid);

		lockCtx.eDeleteById(GtxId.class, gid); // if no error, means all unlocked, can remove gid

		return true;// So far, all databases are OK
	}

	private static TxResult executeUndo(List<GtxLock> locks, String gid) {
		List<Object> entities = new ArrayList<Object>();
		for (GtxLock l : locks) {
			List<Object> entityLst = (List<Object>) lockCtx.eFindBySQL(Tail.class, "select * from ", l.getEntityTb(),
					" where gtxid=?", param(gid));
			entities.addAll(entityLst);
		}
		System.out.println(entities.size());
		for (Object obj : entities) {
			System.out.println("Debug obj=:" + obj);
			System.out.println(((Tail) obj).getTail("gtxid"));
			System.out.println(((ActiveRecord) obj).tails());
		}

		SqlBoxContext ctx = ctxArr[0];
		ctx.startTrans();
		TxResult result = null;
		try {

			result = ctx.commitTrans();
		} catch (Exception e) {
			result = ctx.rollbackTrans();
			throw new TransactionsException(e);
		}
		return result;
	}

}
