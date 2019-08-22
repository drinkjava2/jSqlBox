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

import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * Utils to unlock lock servers
 * 
 * @author Yong Zhu
 */
public class UnlockUtils {
	private static final Map<String, String> gtxIdCache = new HashMap<String, String>();
	private static SqlBoxContext lockCtx;
	private static SqlBoxContext[] ctxs;

	private static void initContext(SqlBoxContext ctx) {
		GtxConnectionManager lockCM = (GtxConnectionManager) ctx.getConnectionManager();
		lockCtx = lockCM.getLockCtx();
		ctxs = new SqlBoxContext[ctx.getMasters().length];
		for (int i = 0; i < ctx.getMasters().length; i++) {
			SqlBoxContext oldctx = (SqlBoxContext) ctx.getMasters()[i];
			ctxs[i] = new SqlBoxContext(oldctx.getDataSource());
			ctxs[i].setName(oldctx.getName());
			ctxs[i].setDbCode(oldctx.getDbCode());
			ctxs[i].setDialect(oldctx.getDialect());
			ctxs[i].setShardingTools(oldctx.getShardingTools());
			ctxs[i].setAllowShowSQL(oldctx.getAllowShowSQL());
			ctxs[i].setMasters(ctxs);
		}
	}

	/** Unlock lock servers very minutesInterval */
	public static void unlock(SqlBoxContext ctx, long minutesInterval) {
		initContext(ctx);
		do {
			List<GtxId> gtxIdList = lockCtx.eFindAll(GtxId.class);
			for (GtxId gtxId : gtxIdList) {
				String id = gtxId.getId();
				if (gtxIdCache.containsKey(id)) {
					if ("Cached".equals(gtxIdCache.get(id))) {
						gtxIdCache.put(id, "Tried"); // only try once
						if (doUnlockOne(id)) // second time unlock it
							gtxIdCache.remove(id);
					}
				} else
					gtxIdCache.put(id, "Cached"); // first time only cache the gtxId
			}
			try {
				Thread.sleep(minutesInterval * 60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (true);
	}

	/** Force unlock a given gtxId, usually used on unit test */
	public static boolean forceUnlock(String gtxId, SqlBoxContext ctx) {
		initContext(ctx);
		doUnlockOne(gtxId);
		return true;
	}

	private static boolean doUnlockOne(String gtxId) {
		GtxId gid = lockCtx.eLoadById(GtxId.class, gtxId);
		System.out.println(gid.getId());
		return true;
	}

}
