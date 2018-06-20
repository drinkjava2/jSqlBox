/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.JDBPRO;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.handler.PaginHandler;

/**
 * JSQLBOX store some public static methods, usually used for static import to
 * simplify programming
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
public abstract class JSQLBOX extends JDBPRO {// NOSONAR
	/** Shortcut method equal to SqlBoxContext.getGlobalSqlBoxContext() */
	public static SqlBoxContext gctx() {
		if (SqlBoxContext.getGlobalSqlBoxContext() == null)
			throw new SqlBoxException(
					"Global SqlBoxContext needed, please use SqlBoxContext.setGlobalSqlBoxContext() method to set a global default SqlBoxContext");
		return SqlBoxContext.getGlobalSqlBoxContext();
	}

	public static PaginHandler pagin(int pageNumber, int pageSize) {
		return new PaginHandler(pageNumber, pageSize);
	}

	/** Create a model configurations, */
	public static TableModel model(Object model) {
		return SqlBoxContextUtils.configToModel(model);
	}

	/** Create model configurations */
	public static SqlItem model(Object... modelsAndAlias) {
		return new SqlItem(SqlOption.MODEL, modelsAndAlias);
	}

	/** Create a model configurations with given alias name */
	public static SqlItem modelAlias(Object... modelsAndAlias) {
		return new SqlItem(SqlOption.MODEL_ALIAS, modelsAndAlias);
	}

	/** Create a model configurations with given alias name */
	public static SqlItem modelAutoAlias(Object... modelsAndAlias) {
		return new SqlItem(SqlOption.MODEL_AUTO_ALIAS, modelsAndAlias);
	}

	public static SqlItem give(String from, String to, String fieldName) {
		return new SqlItem(SqlOption.GIVE, from, to, fieldName);
	}

	public static SqlItem give(String from, String to) {
		return new SqlItem(SqlOption.GIVE, from, to);
	}

	public static SqlItem giveBoth(String from, String to) {
		return new SqlItem(SqlOption.GIVE_BOTH, from, to);
	}

	public static SqlItem noPagin() {
		return new SqlItem(SqlOption.DISABLE_HANDLERS, PaginHandler.class);
	}

	public static SqlItem shardTB(Object entityOrClass, Object... shardvalues) {
		if (shardvalues.length == 0)
			return new SqlItem(SqlOption.SHARD_TABLE, entityOrClass);
		else if (shardvalues.length == 1)
			return new SqlItem(SqlOption.SHARD_TABLE, entityOrClass, shardvalues[0]);
		else
			return new SqlItem(SqlOption.SHARD_TABLE, entityOrClass, shardvalues[1]);
	}

	public static SqlItem shardDB(Object entityOrClass, Object... shardvalues) {
		if (shardvalues.length == 0)
			return new SqlItem(SqlOption.SHARD_DATABASE, entityOrClass);
		else if (shardvalues.length == 1)
			return new SqlItem(SqlOption.SHARD_DATABASE, entityOrClass, shardvalues[0]);
		else
			return new SqlItem(SqlOption.SHARD_DATABASE, entityOrClass, shardvalues[1]);
	}

	//@formatter:off
		protected void gXxxxCrudMethods_____________________() {}// NOSONAR 
		
		public static void gInsert(Object entity, Object... optionalSqlItems) { gctx().insert(entity, optionalSqlItems); }  
		public static int gUpdate(Object entity, Object... optionalSqlItems) { return gctx().update(entity, optionalSqlItems); } 
		public static  void gDelete(Object entity, Object... optionalSqlItems) { gctx().delete(entity, optionalSqlItems); } 
		public static <T> T gLoad(Object entity, Object... optionalSqlItems) { return gctx().load(entity, optionalSqlItems); } 
		public static <T> List<T> gLoadAll(Class<T> entityClass, Object... optionalSqlItems) { return gctx().loadAll(entityClass, optionalSqlItems); } 
		public static <T> T gLoadById(Class<T> entityClass, Object entityId, Object... optionalSqlItems) {return gctx().loadById(entityClass, entityId, optionalSqlItems);}
		public static <T> T gLoadByQuery(Class<T> entityClass, Object... sqlItems) { return gctx().loadByQuery(entityClass, sqlItems); }
		
		protected void gxXxxxStylePublicStaticMethods_____________________() {}// NOSONAR 
		public static <T> T giQuery(Object... inlineSQL) {return  gctx().iQuery(inlineSQL);}
		public static <T> T giQueryForObject(Object... inlineSQL) {return gctx().iQueryForObject(inlineSQL);}
		public static long giQueryForLongValue(Object... inlineSQL) {return gctx().iQueryForLongValue(inlineSQL);}
		public static String giQueryForString(Object... inlineSQL) {return gctx().iQueryForString(inlineSQL);}
		public static List<Map<String, Object>> giQueryForMapList(Object... items) {return gctx().iQueryForMapList(items);}
		public static int giUpdate(Object... inlineSQL) {return gctx().iUpdate(inlineSQL);}
		public static <T> T giInsert(Object... inlineSQL) {return gctx().iInsert(inlineSQL);}
		public static <T> T giExecute(Object... inlineSQL) {return gctx().iExecute(inlineSQL); }
		public static <T> List<T> giQueryForEntityList(Class<T> entityClass, Object... inlineSQL) {return gctx().iQueryForEntityList(entityClass, inlineSQL); }
		 
		public static <T> T gpQuery(Object... inlineSQL) {return gctx().pQuery(inlineSQL);}
		public static <T> T gpQueryForObject(Object... inlineSQL) {return gctx().pQueryForObject(inlineSQL);}
		public static long gpQueryForLongValue(Object... inlineSQL) {return gctx().pQueryForLongValue(inlineSQL);}
		public static String gpQueryForString(Object... inlineSQL) {return gctx().pQueryForString(inlineSQL);}
		public static List<Map<String, Object>> gpQueryForMapList(Object... items) {return gctx().pQueryForMapList(items);}
		public static int gpUpdate(Object... inlineSQL) {return gctx().pUpdate(inlineSQL);}
		public static <T> T gpInsert(Object... inlineSQL) {return gctx().pInsert(inlineSQL);}
		public static <T> T gpExecute(Object... inlineSQL) {return gctx().pExecute(inlineSQL); } 
		public static <T> List<T> gpQueryForEntityList(Class<T> entityClass, Object... inlineSQL) {return gctx().pQueryForEntityList(entityClass, inlineSQL); }
		

		public static <T> T gtQuery(Object... items) {return gctx().tQuery(items);}
		public static <T> T gtQueryForObject(Object... items) {return gctx().tQueryForObject(items);}
		public static long gtQueryForLongValue(Object... items) {return gctx().tQueryForLongValue(items);}
		public static String gtQueryForString(Object... items) {return gctx().tQueryForString(items);}
		public static List<Map<String, Object>> gtQueryForMapList(Object... items) {return gctx().tQueryForMapList(items);}
		public static int gtUpdate(Object... items) {return gctx().tUpdate(items);}
		public static <T> T gtInsert(Object... items) {return gctx().tInsert(items);}
		public static <T> T gtExecute(Object... items) {return gctx().tExecute(items);}
		public static <T> List<T> gtQueryForEntityList(Class<T> entityClass, Object... inlineSQL) {return gctx().tQueryForEntityList(entityClass, inlineSQL); }

		
		public static <T> T gnQuery(Connection conn, ResultSetHandler<T> rsh, String sql, Object... params) {return gctx().nQuery(conn, rsh, sql, params);}
		public static <T> T gnQueryForObject(Connection conn, String sql, Object... params) {return gctx().nQueryForObject(conn, sql, params);}
		public static String gnQueryForString(Connection conn, String sql, Object... params) {return gctx().nQueryForString(conn, sql, params);}
		public static long gnQueryForLongValue(Connection conn, String sql, Object... params) {return gctx().nQueryForLongValue(conn, sql, params);}
		public static List<Map<String, Object>> gnQueryForMapList(Connection conn, String sql, Object... params) {return gctx().nQueryForMapList(conn, sql, params);}
		public static int gnUpdate(Connection conn, String sql, Object... params) {return gctx().nUpdate(conn, sql, params);}
		public static <T> T gnInsert(Connection conn, ResultSetHandler<T> rsh, String sql, Object... params) {return gctx().nInsert(conn, rsh, sql, params);}
		public static int gnExecute(Connection conn, String sql, Object... params) {return gctx().nExecute(conn, sql, params);}
		public static <T> List<T> gnExecute(Connection conn, ResultSetHandler<T> rsh, String sql, Object... params) {return gctx().nExecute(conn, rsh, sql, params);}
		public static <T> T gnQuery(ResultSetHandler<T> rsh, String sql, Object... params) {return gctx().nQuery(rsh, sql, params);}
		public static <T> T gnQueryForObject(String sql, Object... params) {return gctx().nQueryForObject(sql, params);}
		public static String gnQueryForString(String sql, Object... params) {return gctx().nQueryForString(sql, params);}
		public static long gnQueryForLongValue(String sql, Object... params) {return gctx().nQueryForLongValue(sql, params);}
		public static List<Map<String, Object>> gnQueryForMapList(String sql, Object... params) {return gctx().nQueryForMapList(sql, params);}
		public static int gnUpdate(String sql, Object... params) {return gctx().nUpdate(sql, params);}
		public static <T> T gnInsert(@SuppressWarnings("rawtypes") ResultSetHandler rsh, String sql, Object... params) {return gctx().nInsert(rsh, sql, params);}
		public static int gnExecute(String sql, Object... params) {return gctx().nExecute(sql, params);}
		public static <T> List<T> gnExecute(@SuppressWarnings("rawtypes") ResultSetHandler rsh, String sql, Object... params) {return gctx().nExecute(rsh, sql, params);}
	 
}