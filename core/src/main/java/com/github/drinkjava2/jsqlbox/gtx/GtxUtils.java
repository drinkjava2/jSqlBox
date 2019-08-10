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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * Gtx public static methods
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxUtils {
	public static final String GTX_LOG_ID = "gtx_log_id";// log number, this is the P-Key of this log table
	public static final String GTX_ID = "gtx_id";
	public static final String GTX_TYPE = "gtx_type";// tx log type, can be update/exist/existStrict/insert/delete

	protected static final Map<Class<?>, TableModel> globalGtxTableModelCache = new ConcurrentHashMap<Class<?>, TableModel>();

	public static void logInsert(SqlBoxContext ctx, Object entity) {
		ctx.getGtxInfo().getGtxLogList().add(new GtxLog("insert", entity));
		addLock(ctx, entity);
	}

	public static void logExist(SqlBoxContext ctx, Object entity) {
		ctx.getGtxInfo().getGtxLogList().add(new GtxLog("exist", entity));
		addLock(ctx, entity);
	}

	public static void logExistStrict(SqlBoxContext ctx, Object entity) {
		ctx.getGtxInfo().getGtxLogList().add(new GtxLog("existStrict", entity));
		addLock(ctx, entity);
	}

	public static void logDelete(SqlBoxContext ctx, Object entity) {
		ctx.getGtxInfo().getGtxLogList().add(new GtxLog("delete", entity));
		addLock(ctx, entity);
	}

	public static void logUpdate(SqlBoxContext ctx, Object entity) {
		ctx.getGtxInfo().getGtxLogList().add(new GtxLog("update", entity));
		addLock(ctx, entity);
	}

	public static void addLock(SqlBoxContext ctx, Object entity) {
		List<GtxLock> locks = ctx.getGtxInfo().getGtxLockList();
		boolean found = false;
		String db = ctx.getName();
		
		
		
		String table = "";
		Object id = "";
		
		
		for (GtxLock lk : locks)
			if (lk.getDb().equals(db) || lk.getTable().equals(table) || lk.getId().equals(id)) {
				found = true;
				break;
			}
		if (!found) {
			GtxLock lock = new GtxLock();
			lock.setDb(ctx.getName());
			lock.setTable(""); 
		}

	}

	/**
	 * Convert an entity class to gtxLog entity class, i.e., add some columns for it
	 */
	public static TableModel entity2GtxLogModel(Class<?> entityClass) {
		TableModel model = globalGtxTableModelCache.get(entityClass);
		if (model != null)
			return model;
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
		t.column(GTX_ID).VARCHAR(32);
		t.column(GTX_TYPE).VARCHAR(14);
		t.column(GTX_LOG_ID).LONG().id(); // ID is assigned by outside
		globalGtxTableModelCache.put(entityClass, t);
		return t;
	}
}