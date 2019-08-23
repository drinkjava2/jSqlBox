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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdbpro.log.DbProLog;
import com.github.drinkjava2.jdbpro.log.DbProLogFactory;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

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
	}

	/**
	 * Unlock lock servers very intervalSecond
	 * 
	 * @throws InterruptedException
	 */
	public static void unlock(SqlBoxContext ctx, long intervalSecond) throws InterruptedException {
		initContext(ctx);
		do {
			List<GtxId> gtxIdList = lockCtx.eFindAll(GtxId.class);
			for (GtxId gtxId : gtxIdList) {
				String id = gtxId.getGid();
				if (gtxIdCache.containsKey(id)) {
					if ("Cached".equals(gtxIdCache.get(id))) {
						gtxIdCache.put(id, "Tried"); // only try once
						if (doUnlockOne(id)) // second time unlock it
							gtxIdCache.remove(id);
					}
				} else
					gtxIdCache.put(id, "Cached"); // first time cache the gtxId
			}
			Thread.sleep(intervalSecond * 1000);
		} while (true);
	}

	/**
	 * Force unlock a given gtxId, usually used on unit test, return true if
	 * success, otherwise return false means require manually unlock
	 */
	public static boolean forceUnlock(String gtxId, SqlBoxContext ctx) {
		initContext(ctx);
		doUnlockOne(gtxId);
		return true;
	}

	private static boolean doUnlockOne(String gtxId) {
		GtxId gid = lockCtx.eLoadByIdTry(GtxId.class, gtxId);
		if (gid == null) {
			logger.warn("Try to unlock an un-exist gtxId");
			return true;
		}
		System.out.println(gid);
		return true;
	}

}
