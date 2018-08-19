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
import java.util.Set;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.drinkjava2.jdbpro.JDBPRO;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.PaginHandler;

/**
 * JSQLBOX store some public static methods, usually used for static import to
 * simplify programming
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
public abstract class JSQLBOX extends JDBPRO {// NOSONAR
	public static final SqlOption USE_TEMPLATE = SqlOption.USE_TEMPLATE;
	public static final SqlOption EXECUTE = SqlOption.EXECUTE;
	public static final SqlOption UPDATE = SqlOption.UPDATE;
	public static final SqlOption INSERT = SqlOption.INSERT;
	public static final SqlOption QUERY = SqlOption.QUERY;
	public static final SqlOption USE_AUTO = SqlOption.USE_AUTO;
	public static final SqlOption USE_MASTER = SqlOption.USE_MASTER;
	public static final SqlOption USE_SLAVE = SqlOption.USE_SLAVE;
	public static final SqlOption USE_BOTH = SqlOption.USE_BOTH;
	public static final SqlOption IGNORE_NULL = SqlOption.IGNORE_NULL;
	public static final SqlOption AUTO_SQL = SqlOption.AUTO_SQL;

	/** Shortcut method equal to SqlBoxContext.getGlobalSqlBoxContext() */
	public static SqlBoxContext gctx() {
		if (SqlBoxContext.getGlobalSqlBoxContext() == null)
			throw new SqlBoxException(
					"Global SqlBoxContext needed, please use SqlBoxContext.setGlobalSqlBoxContext() method to set a global default SqlBoxContext");
		return SqlBoxContext.getGlobalSqlBoxContext();
	}

	/** Build a PaginHandler handler */
	public static PaginHandler pagin(int pageNumber, int pageSize) {
		return new PaginHandler(pageNumber, pageSize);
	}

	/** Create a model configurations with iven alias name */
	public static SqlItem alias(String... alias) {
		return new SqlItem(SqlOption.ALIAS, (Object[]) alias);
	}

	/** For EntityNet Query use, see user manual */
	public static SqlItem give(String from, String to, String fieldName) {
		return new SqlItem(SqlOption.GIVE, from, to, fieldName);
	}

	/** For EntityNet Query use, see user manual */
	public static SqlItem give(String from, String to) {
		return new SqlItem(SqlOption.GIVE, from, to);
	}

	/** For EntityNet Query use, see user manual */
	public static SqlItem giveBoth(String from, String to) {
		return new SqlItem(SqlOption.GIVE_BOTH, from, to);
	}

	/** Disable PaginHandler */
	public static SqlItem noPagin() {
		return new SqlItem(SqlOption.DISABLE_HANDLERS, PaginHandler.class);
	}

	/** Build a SHARD_TABLE type sqlItem */
	public static SqlItem shardTB(Object... shardvalues) {
		if (shardvalues.length == 0)
			throw new SqlBoxException("shardTB() method need at least 1 parameter");
		else if (shardvalues.length == 1)
			return new SqlItem(SqlOption.SHARD_TABLE, shardvalues[0]);
		else if (shardvalues.length == 2)
			return new SqlItem(SqlOption.SHARD_TABLE, shardvalues[0], shardvalues[1]);
		throw new SqlBoxException("shardTB() method allow at most 2 parameter");
	}

	/** Build a SHARD_DATABASE type sqlItem */
	public static SqlItem shardDB(Object... shardvalues) {
		if (shardvalues.length == 0)
			throw new SqlBoxException("shardDB() method need at least 1 parameter");
		else if (shardvalues.length == 1)
			return new SqlItem(SqlOption.SHARD_DATABASE, shardvalues[0]);
		else if (shardvalues.length == 2)
			return new SqlItem(SqlOption.SHARD_DATABASE, shardvalues[0], shardvalues[1]);
		throw new SqlBoxException("shardTB() method allow at most 2 parameter");
	}

	/** Build a SHARD_TABLE type sqlItem */
	public static Object[] shard(Object... shardvalues) {
		return new Object[] { shardTB(shardvalues), shardDB(shardvalues) };
	}

	//@formatter:off
	protected void entity_series_methods_from_SqlBoxContext_____________________() {}// NOSONAR 

	public static <T> List<T> iQueryForEntityList(Object... optionItems) {return  gctx().iQueryForEntityList(optionItems);}
	public static <T> List<T> pQueryForEntityList(Object... optionItems) {return  gctx().pQueryForEntityList(optionItems);}
	public static <T> List<T> tQueryForEntityList(Object... optionItems) {return  gctx().tQueryForEntityList(optionItems);}
	public static <T> T entityInsert(T entity, Object... optionItems) {return  gctx().entityInsert(entity, optionItems);}
	public static <T> T entityUpdate(Object entity, Object... optionItems) {return  gctx().entityUpdate(entity, optionItems);}
	public static int entityUpdateTry(Object entity, Object... optionItems) {return  gctx().entityUpdateTry(entity, optionItems);}
	public static void entityDelete(Object entity, Object... optionItems) { gctx().entityDelete(entity, optionItems);}
	public static int entityDeleteTry(Object entity, Object... optionItems) {return gctx().entityDeleteTry(entity, optionItems);}
	public static void entityDeleteById(Class<?> entityClass, Object id, Object... optionItems) {gctx().entityDeleteById(entityClass, id, optionItems);;}
	public static int entityDeleteByIdTry(Class<?> entityClass, Object id, Object... optionItems) {return  gctx().entityDeleteByIdTry(entityClass, id, optionItems);}
	public static boolean entityExist(Object entity, Object... optionItems) {return  gctx().entityExist(entity, optionItems);}
	public static boolean entityExistById(Class<?> entityClass, Object id, Object... optionItems) {return  gctx().entityExistById(entityClass, id, optionItems);}
	public static int entityCountAll(Class<?> entityClass, Object... optionItems) {return  gctx().entityCountAll(entityClass, optionItems);}
	public static <T> T entityLoad(T entity, Object... optionItems) {return  gctx().entityLoad(entity, optionItems);}
	public static int entityLoadTry(Object entity, Object... optionItems) {return  gctx().entityLoadTry(entity, optionItems);}
	public static <T> T entityLoadById(Class<T> entityClass, Object entityId, Object... optionItems) {return  gctx().entityLoadById(entityClass, entityId, optionItems);}
	public static <T> T entityLoadByIdTry(Class<T> entityClass, Object entityId, Object... optionItems) {return  gctx().entityLoadByIdTry(entityClass, entityId, optionItems);}
	public static <T> List<T> entityFindAll(Class<T> entityClass, Object... optionItems) {return  gctx().entityFindAll(entityClass, optionItems);}
	public static <T> List<T> entityFindByIds(Class<T> entityClass, Iterable<?> ids, Object... optionItems) {return  gctx().entityFindByIds(entityClass, ids, optionItems);}
	public static <T> List<T> entityFindBySQL(Object... optionItems) {return  gctx().entityFindBySQL(optionItems);}
	public static <T> List<T> entityFindBySample(Object sampleBean, Object... optionItems) {return  gctx().entityFindBySample(sampleBean, optionItems);}
	public static EntityNet entityAutoNet(Class<?>... entityClass) {return  gctx().entityAutoNet(entityClass);}
	public static <E> E entityFindRelatedOne(Object entity, Object... sqlItems) {return  gctx().entityFindRelatedOne(entity, sqlItems);}
	public static <E> List<E> entityFindRelatedList(Object entityOrIterable, Object... sqlItems) {return  gctx().entityFindRelatedList(entityOrIterable, sqlItems);}
	public static <E> Set<E> entityFindRelatedSet(Object entity, Object... sqlItems) {return  gctx().entityFindRelatedSet(entity, sqlItems);}
	public static <E> Map<Object, E> entityFindRelatedMap(Object entity, Object... sqlItems) {return  gctx().entityFindRelatedMap(entity, sqlItems);}

	
	
	
	protected void pint_series_methods_from_DbPro_____________________() {}// NOSONAR 	
	
	public static <T> T iQuery(Object... inlineSQL) {return  gctx().iQuery(inlineSQL);}
	public static <T> T iQueryForObject(Object... inlineSQL) {return gctx().iQueryForObject(inlineSQL);}
	public static long iQueryForLongValue(Object... inlineSQL) {return gctx().iQueryForLongValue(inlineSQL);}
	public static String iQueryForString(Object... inlineSQL) {return gctx().iQueryForString(inlineSQL);}
	public static List<Map<String, Object>> iQueryForMapList(Object... items) {return gctx().iQueryForMapList(items);}
	public static int iUpdate(Object... inlineSQL) {return gctx().iUpdate(inlineSQL);}
	public static <T> T iInsert(Object... inlineSQL) {return gctx().iInsert(inlineSQL);}
	public static <T> T iExecute(Object... inlineSQL) {return gctx().iExecute(inlineSQL); }
	public static <T> List<T> iQueryForEntityList(Class<T> entityClass, Object... inlineSQL) {return gctx().iQueryForEntityList(entityClass, inlineSQL); }
	 
	public static <T> T pQuery(Object... inlineSQL) {return gctx().pQuery(inlineSQL);}
	public static <T> T pQueryForObject(Object... inlineSQL) {return gctx().pQueryForObject(inlineSQL);}
	public static long pQueryForLongValue(Object... inlineSQL) {return gctx().pQueryForLongValue(inlineSQL);}
	public static String pQueryForString(Object... inlineSQL) {return gctx().pQueryForString(inlineSQL);}
	public static List<Map<String, Object>> pQueryForMapList(Object... items) {return gctx().pQueryForMapList(items);}
	public static int pUpdate(Object... inlineSQL) {return gctx().pUpdate(inlineSQL);}
	public static <T> T pInsert(Object... inlineSQL) {return gctx().pInsert(inlineSQL);}
	public static <T> T pExecute(Object... inlineSQL) {return gctx().pExecute(inlineSQL); } 
	public static <T> List<T> pQueryForEntityList(Class<T> entityClass, Object... inlineSQL) {return gctx().pQueryForEntityList(entityClass, inlineSQL); }
	

	public static <T> T tQuery(Object... items) {return gctx().tQuery(items);}
	public static <T> T tQueryForObject(Object... items) {return gctx().tQueryForObject(items);}
	public static long tQueryForLongValue(Object... items) {return gctx().tQueryForLongValue(items);}
	public static String tQueryForString(Object... items) {return gctx().tQueryForString(items);}
	public static List<Map<String, Object>> tQueryForMapList(Object... items) {return gctx().tQueryForMapList(items);}
	public static int tUpdate(Object... items) {return gctx().tUpdate(items);}
	public static <T> T tInsert(Object... items) {return gctx().tInsert(items);}
	public static <T> T tExecute(Object... items) {return gctx().tExecute(items);}
	public static <T> List<T> tQueryForEntityList(Class<T> entityClass, Object... inlineSQL) {return gctx().tQueryForEntityList(entityClass, inlineSQL); }

	
	public static <T> T nQuery(Connection conn, ResultSetHandler<T> rsh, String sql, Object... params) {return gctx().nQuery(conn, rsh, sql, params);}
	public static <T> T nQueryForObject(Connection conn, String sql, Object... params) {return gctx().nQueryForObject(conn, sql, params);}
	public static String nQueryForString(Connection conn, String sql, Object... params) {return gctx().nQueryForString(conn, sql, params);}
	public static long nQueryForLongValue(Connection conn, String sql, Object... params) {return gctx().nQueryForLongValue(conn, sql, params);}
	public static List<Map<String, Object>> nQueryForMapList(Connection conn, String sql, Object... params) {return gctx().nQueryForMapList(conn, sql, params);}
	public static int nUpdate(Connection conn, String sql, Object... params) {return gctx().nUpdate(conn, sql, params);}
	public static <T> T nInsert(Connection conn, ResultSetHandler<T> rsh, String sql, Object... params) {return gctx().nInsert(conn, rsh, sql, params);}
	public static int nExecute(Connection conn, String sql, Object... params) {return gctx().nExecute(conn, sql, params);}
	public static <T> List<T> nExecute(Connection conn, ResultSetHandler<T> rsh, String sql, Object... params) {return gctx().nExecute(conn, rsh, sql, params);}
	public static <T> T nQuery(ResultSetHandler<T> rsh, String sql, Object... params) {return gctx().nQuery(rsh, sql, params);}
	public static <T> T nQueryForObject(String sql, Object... params) {return gctx().nQueryForObject(sql, params);}
	public static String nQueryForString(String sql, Object... params) {return gctx().nQueryForString(sql, params);}
	public static long nQueryForLongValue(String sql, Object... params) {return gctx().nQueryForLongValue(sql, params);}
	public static List<Map<String, Object>> nQueryForMapList(String sql, Object... params) {return gctx().nQueryForMapList(sql, params);}
	public static int nUpdate(String sql, Object... params) {return gctx().nUpdate(sql, params);}
	public static <T> T nInsert(@SuppressWarnings("rawtypes") ResultSetHandler rsh, String sql, Object... params) {return gctx().nInsert(rsh, sql, params);}
	public static int nExecute(String sql, Object... params) {return gctx().nExecute(sql, params);}
	public static <T> List<T> nExecute(@SuppressWarnings("rawtypes") ResultSetHandler rsh, String sql, Object... params) {return gctx().nExecute(rsh, sql, params);}
	 
}