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

import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.JDBPRO;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.PaginHandler;
import com.github.drinkjava2.jtransactions.TxResult;

/**
 * DB store some public static methods, usually used for static import to
 * simplify programming
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
public abstract class DB extends JDBPRO {// NOSONAR
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
	public static final SqlOption IGNORE_EMPTY = SqlOption.IGNORE_EMPTY;
	public static final SqlOption AUTO_SQL = SqlOption.AUTO_SQL;
	public static final SqlItem TAIL = new SqlItem(SqlOption.TAIL);

	/** Shortcut method equal to DbContext.getGlobalDbContext() */
	public static DbContext gctx() {
		return DbContext.getGlobalDbContext();
	}

	public static boolean tx(TxBody txBody) {
		return DbContext.getGlobalDbContext().tryTx(txBody);
	}

	public static TxResult getLastTxResult() {
		return ImprovedQueryRunner.getLastTxResult();
	}

	/** Build a TAIL SqlItem, tableName is the only parameter */
	public static SqlItem tail(Object... tableName) {
		return new SqlItem(SqlOption.TAIL, tableName);
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
			throw new DbException("shardTB() method need at least 1 parameter");
		else if (shardvalues.length == 1)
			return new SqlItem(SqlOption.SHARD_TABLE, shardvalues[0]);
		else if (shardvalues.length == 2)
			return new SqlItem(SqlOption.SHARD_TABLE, shardvalues[0], shardvalues[1]);
		throw new DbException("shardTB() method allow at most 2 parameter");
	}

	/** Build a SHARD_DATABASE type sqlItem */
	public static SqlItem shardDB(Object... shardvalues) {
		if (shardvalues.length == 0)
			throw new DbException("shardDB() method need at least 1 parameter");
		else if (shardvalues.length == 1)
			return new SqlItem(SqlOption.SHARD_DATABASE, shardvalues[0]);
		else if (shardvalues.length == 2)
			return new SqlItem(SqlOption.SHARD_DATABASE, shardvalues[0], shardvalues[1]);
		throw new DbException("shardTB() method allow at most 2 parameter");
	}

	/** Build a SHARD_TABLE type sqlItem */
	public static Object[] shard(Object... shardvalues) {
		return new Object[] { shardTB(shardvalues), shardDB(shardvalues) };
	}

	//@formatter:off
	
	//Entity series methods from DbContext
	
	//eFindXxxx method if not found any entity, will return empty List or null value
	public static <T> List<T> eFindAll(Class<T> entityClass, Object... items) {return gctx().eFindAll(entityClass, items);}
	public static <T> List<T> eFindBySample(Object sampleBean, Object... items) {return gctx().eFindBySample(sampleBean, items);}
	public static <T> List<T> eFindBySQL(Object... items) {return gctx().eFindBySQL(items);}
	public static <T> T eFindOneBySQL(Object... items) {return gctx().eFindOneBySQL(items);} 
	 
 	//eLoadXxx method if load fail, will throw DbException
	public static <T> T eLoad(T entity, Object... items) {return gctx().eLoad(entity, items);} 
	public static <T> T eLoadById(Class<T> entityClass, Object entityId, Object... items) {return gctx().eLoadById(entityClass, entityId, items);}
    public static <T> T eLoadByIdTry(Class<T> entityClass, Object entityId, Object... items) {return gctx().eLoadByIdTry(entityClass, entityId, items);}
    public static <T> T eLoadBySQL(Object... items) {return gctx().eLoadBySQL(items);}

	public static <T> T eInsert(T entity, Object... items) {return gctx().eInsert(entity, items);} 
    public static <T> T eUpdate(Object entity, Object... items) {return gctx().eUpdate(entity, items);}
	public static boolean eExist(Object entity, Object... items) {return gctx().eExist(entity, items);}
	public static boolean eExistById(Class<?> entityClass, Object id, Object... items) {return gctx().eExistById(entityClass, id, items);}
	public static int eCountAll(Class<?> entityClass, Object... items) {return gctx().eCountAll(entityClass, items);}
	public static int eDeleteByIdTry(Class<?> entityClass, Object id, Object... items) {return gctx().eDeleteByIdTry(entityClass, id, items);}
	public static int eDeleteTry(Object entity, Object... items) {return gctx().eDeleteTry(entity, items);}
	public static int eLoadTry(Object entity, Object... items) {return gctx().eLoadTry(entity, items);}
	public static int eUpdateTry(Object entity, Object... items) {return gctx().eUpdateTry(entity, items);}
	public static void eDelete(Object entity, Object... items) { gctx().eDelete(entity, items);}
	public static void eDeleteById(Class<?> entityClass, Object id, Object... items) {gctx().eDeleteById(entityClass, id, items);}
	public static EntityNet eAutoNet(Class<?>... entityClass) {return  gctx().autoNet(entityClass);}
	public static <T> T eFindRelatedOne(Object entity, Object... sqlItems) {return  gctx().eFindRelatedOne(entity, sqlItems);}
	public static <T> List<T> eFindRelatedList(Object entityOrIterable, Object... sqlItems) {return  gctx().eFindRelatedList(entityOrIterable, sqlItems);}
	public static <T> Set<T> eFindRelatedSet(Object entity, Object... sqlItems) {return  gctx().eFindRelatedSet(entity, sqlItems);}
	public static <T> Map<Object, T> eFindRelatedMap(Object entity, Object... sqlItems) {return  gctx().eFindRelatedMap(entity, sqlItems);}
	
	
	// PINT series methods from jDbPro
	public static <T> T pQuery(Object... items) {return gctx().pQuery(items);}
	public static <T> T pQueryForObject(Object... items) {return gctx().pQueryForObject(items);}
	public static long pQueryForLongValue(Object... items) {return gctx().pQueryForLongValue(items);}
	public static int pQueryForIntValue(Object... items) {return gctx().pQueryForIntValue(items);}
	public static String pQueryForString(Object... items) {return gctx().pQueryForString(items);}
	public static List<Map<String, Object>> pQueryForMapList(Object... items) {return gctx().pQueryForMapList(items);}
	public static int pUpdate(Object... items) {return gctx().pUpdate(items);}
	public static <T> T pInsert(Object... items) {return gctx().pInsert(items);}
	public static <T> T pExecute(Object... items) {return gctx().pExecute(items); }  
	public static <T> List<T> pQueryForEntityList(Object... items) {return gctx().pQueryForEntityList(items);} 
	
	public static <T> T iQuery(Object... items) {return  gctx().iQuery(items);}
	public static <T> T iQueryForObject(Object... items) {return gctx().iQueryForObject(items);}
	public static long iQueryForLongValue(Object... items) {return gctx().iQueryForLongValue(items);}
	public static int iQueryForIntValue(Object... items) {return gctx().iQueryForIntValue(items);}
	public static String iQueryForString(Object... items) {return gctx().iQueryForString(items);}
	public static List<Map<String, Object>> iQueryForMapList(Object... items) {return gctx().iQueryForMapList(items);}
	public static int iUpdate(Object... items) {return gctx().iUpdate(items);}
	public static <T> T iInsert(Object... items) {return gctx().iInsert(items);}
	public static <T> T iExecute(Object... items) {return gctx().iExecute(items); }
	public static <T> List<T> iQueryForEntityList(Object... items) {return gctx().iQueryForEntityList(items);}
	public static PreparedSQL iPrepare(Object... items) { return gctx().iPrepare(items); }

	public static <T> T nQuery(Connection conn, ResultSetHandler<T> rsh, String sql, Object... items) {return gctx().nQuery(conn, rsh, sql, items);}
	public static <T> T nQueryForObject(Connection conn, String sql, Object... items) {return gctx().nQueryForObject(conn, sql, items);}
	public static String nQueryForString(Connection conn, String sql, Object... items) {return gctx().nQueryForString(conn, sql, items);}
	public static long nQueryForLongValue(Connection conn, String sql, Object... items) {return gctx().nQueryForLongValue(conn, sql, items);}
	public static int nQueryForIntValue(Connection conn, String sql, Object... items) {return gctx().nQueryForIntValue(conn, sql, items);}
	public static List<Map<String, Object>> nQueryForMapList(Connection conn, String sql, Object... items) {return gctx().nQueryForMapList(conn, sql, items);}
	public static int nUpdate(Connection conn, String sql, Object... items) {return gctx().nUpdate(conn, sql, items);}
	public static <T> T nInsert(Connection conn, ResultSetHandler<T> rsh, String sql, Object... items) {return gctx().nInsert(conn, rsh, sql, items);}
	public static int nExecute(Connection conn, String sql, Object... items) {return gctx().nExecute(conn, sql, items);}
	public static <T> List<T> nExecute(Connection conn, ResultSetHandler<T> rsh, String sql, Object... items) {return gctx().nExecute(conn, rsh, sql, items);}
	public static <T> T nQuery(ResultSetHandler<T> rsh, String sql, Object... items) {return gctx().nQuery(rsh, sql, items);}
	public static <T> T nQueryForObject(String sql, Object... items) {return gctx().nQueryForObject(sql, items);}
	public static String nQueryForString(String sql, Object... items) {return gctx().nQueryForString(sql, items);}
	public static long nQueryForLongValue(String sql, Object... items) {return gctx().nQueryForLongValue(sql, items);}
	public static int nQueryForIntValue(String sql, Object... items) {return gctx().nQueryForIntValue(sql, items);}
	public static List<Map<String, Object>> nQueryForMapList(String sql, Object... items) {return gctx().nQueryForMapList(sql, items);}
	public static int nUpdate(String sql, Object... items) {return gctx().nUpdate(sql, items);}
	public static <T> T nInsert(@SuppressWarnings("rawtypes") ResultSetHandler rsh, String sql, Object... items) {return gctx().nInsert(rsh, sql, items);}
	public static int nExecute(String sql, Object... items) {return gctx().nExecute(sql, items);}
	public static <T> List<T> nExecute(@SuppressWarnings("rawtypes") ResultSetHandler rsh, String sql, Object... items) {return gctx().nExecute(rsh, sql, items);}
		
	public static <T> T tQuery(Object... items) {return gctx().tQuery(items);}
	public static <T> T tQueryForObject(Object... items) {return gctx().tQueryForObject(items);}
	public static long tQueryForLongValue(Object... items) {return gctx().tQueryForLongValue(items);}
	public static int tQueryForIntValue(Object... items) {return gctx().tQueryForIntValue(items);}
	public static String tQueryForString(Object... items) {return gctx().tQueryForString(items);}
	public static List<Map<String, Object>> tQueryForMapList(Object... items) {return gctx().tQueryForMapList(items);}
	public static int tUpdate(Object... items) {return gctx().tUpdate(items);}
	public static <T> T tInsert(Object... items) {return gctx().tInsert(items);}
	public static <T> T tExecute(Object... items) {return gctx().tExecute(items);}
	public static <T> List<T> tQueryForEntityList(Class<T> entityClass, Object... items) {return gctx().tQueryForEntityList(entityClass, items); }

	
  
}