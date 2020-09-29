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

import java.util.List;
import java.util.Map;
import java.util.Set;

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
	public static SqlItem tail(Object... tableName) {//NOSONAR
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
	public static <T> List<T> eFindAll(Class<T> entityClass, Object... items) {return gctx().entityFind(entityClass, items);}
	public static <T> List<T> eFindBySample(Object sampleBean, Object... items) {return gctx().entityFindBySample(sampleBean, items);}
	public static <T> List<T> eFindBySQL(Object... items) {return gctx().beanFindBySql(items);}
	public static <T> T eFindOneBySQL(Object... items) {return gctx().entityFindOneBySQL(items);} 
	 
 	//eLoadXxx method if load fail, will throw DbException
	public static <T> T eLoad(T entity, Object... items) {return gctx().entityLoad(entity, items);} 
	public static <T> T eLoadById(Class<T> entityClass, Object entityId, Object... items) {return gctx().entityLoadById(entityClass, entityId, items);}
    public static <T> T eLoadByIdTry(Class<T> entityClass, Object entityId, Object... items) {return gctx().entityLoadByIdTry(entityClass, entityId, items);}
    public static <T> T eLoadBySQL(Object... items) {return gctx().entityLoadBySql(items);}

	public static <T> T eInsert(T entity, Object... items) {return gctx().entityInsert(entity, items);} 
    public static <T> T eUpdate(Object entity, Object... items) {return gctx().entityUpdate(entity, items);}
	public static boolean eExist(Object entity, Object... items) {return gctx().entityExist(entity, items);}
	public static boolean eExistById(Class<?> entityClass, Object id, Object... items) {return gctx().entityExistById(entityClass, id, items);}
	public static int eCountAll(Class<?> entityClass, Object... items) {return gctx().entityCount(entityClass, items);}
	public static int eDeleteByIdTry(Class<?> entityClass, Object id, Object... items) {return gctx().entityDeleteByIdTry(entityClass, id, items);}
	public static int eDeleteTry(Object entity, Object... items) {return gctx().entityDeleteTry(entity, items);}
	public static int eLoadTry(Object entity, Object... items) {return gctx().entityLoadTry(entity, items);}
	public static int eUpdateTry(Object entity, Object... items) {return gctx().entityUpdateTry(entity, items);}
	public static void eDelete(Object entity, Object... items) { gctx().entityDelete(entity, items);}
	public static void eDeleteById(Class<?> entityClass, Object id, Object... items) {gctx().entityDeleteById(entityClass, id, items);}
	public static EntityNet eAutoNet(Class<?>... entityClass) {return  gctx().autoNet(entityClass);}
	public static <T> T eFindRelatedOne(Object entity, Object... sqlItems) {return  gctx().entityFindRelatedOne(entity, sqlItems);}
	public static <T> List<T> eFindRelatedList(Object entityOrIterable, Object... sqlItems) {return  gctx().entityFindRelatedList(entityOrIterable, sqlItems);}
	public static <T> Set<T> eFindRelatedSet(Object entity, Object... sqlItems) {return  gctx().entityFindRelatedSet(entity, sqlItems);}
	public static <T> Map<Object, T> eFindRelatedMap(Object entity, Object... sqlItems) {return  gctx().entityFindRelatedMap(entity, sqlItems);}
	
	
 
	
	
	// simplilfied SQL methods 
	public static <T> T qry(Object... items) {return  gctx().qry(items);}
	public static <T> T iQueryForObject(Object... items) {return gctx().qryObject(items);}
	public static long qryLongValue(Object... items) {return gctx().qryLongValue(items);}
	public static int qryIntValue(Object... items) {return gctx().qryIntValue(items);}
	public static String qryString(Object... items) {return gctx().qryString(items);}
	public static List<Map<String, Object>> qryMapList(Object... items) {return gctx().qryMapList(items);}
	public static Map<String, Object> qryMap(Object... items) {return gctx().qryMap(items);}
	public static int upd(Object... items) {return gctx().upd(items);}
	public static <T> T ins(Object... items) {return gctx().ins(items);}
	public static <T> T exe(Object... items) {return gctx().exe(items); }
	public static <T> List<T> qryEntityList(Object... items) {return gctx().qryEntityList(items);}
	public static PreparedSQL prepare(Object... items) { return gctx().prepare(items); }

  
}