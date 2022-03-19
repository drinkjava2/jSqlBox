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

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.DbProException;
import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdbpro.TxBody;
import com.github.drinkjava2.jdbpro.handler.PaginHandler;
import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jtransactions.TxResult;

/**
 * DB store some public static methods, usually used for static import to
 * simplify programming
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
public abstract class DB  {// NOSONAR
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
	public static final SqlTemplateEngine TEMPLATE = BasicSqlTemplate.instance();
	public static final SqlItem VQ = new SqlItem(SqlOption.VALUES_QUESTIONS); //same as DB.valuesQuestions();
	//======================

	protected void ________SqlItem_Methods________() {// NOSONAR
	}

	/** Return a SqlItemType.PARAM type SqlItem instance */
	public static SqlItem par(Object... parameters) {
		return new SqlItem(SqlOption.PARAM, parameters);
	}
 
	/**
	 * Cache parameters and return a "?" String
	 */
	public static SqlItem que(Object... parameters) {// NOSONAR
		return new SqlItem(SqlOption.QUESTION_PARAM, parameters);
	}

	/** Return a SqlItemType.PARAM type SqlItem instance */
	public static SqlItem param(Object... parameters) {
		return new SqlItem(SqlOption.PARAM, parameters);
	}
 
	/**
	 * Cache parameters and return a "?" String
	 */
	public static SqlItem ques(Object... parameters) {// NOSONAR
		return new SqlItem(SqlOption.QUESTION_PARAM, parameters);
	}
	
	/**
	 * Cache parameters and return a "?" String
	 */
	public static SqlItem question(Object... parameters) {
		return new SqlItem(SqlOption.QUESTION_PARAM, parameters);
	}

	/**
	 * If last param is not null, then add all items in SQL<br/>
	 * Example: query("select * from a where 1=1",notNull(" and name=?",name));
	 */
	public static Object notNull(Object... items) {
		if (items.length < 2)
			throw new DbProException("notNull method need at least 2 args");
		Object lastObj=items[items.length-1];
		if(items[items.length-1]==null)
				return "";
		items[items.length-1]=par(lastObj);
		return items;
	}

	/**
	 * If no any param is null, then add all items in SQL<br/>
	 * Example: query("select * from a where 1=1",noNull("and name like
	 * ?","%",name,"%"));
	 */
	public static Object noNull(Object... items) {
		if (items.length < 2)
			throw new DbProException("noNull method need at least 2 args");
		for (int i = 0; i <= items.length - 1; i++)
			if (items[i] == null)
				return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= items.length - 1; i++)
			sb.append(items[i]);
		return new Object[] {items[0], par(sb.toString())};
	}

	/**
	 * If last param is not blank, then add all items in SQL<br/>
	 * Example: query("select * from a where 1=1", notBlank(" and name=?",name));
	 */
	public static Object notBlank(Object... items) {
		Object lastObj=items[items.length-1];
		if(StrUtils.isBlankObject(lastObj))
				return "";
		items[items.length-1]=par(lastObj);
		return items;
	}

	/**
	 * If no any param is null, then add all items in SQL<br/>
	 * Example: query("select * from a where 1=1",noNull("and name like
	 * ?","%",name,"%"));
	 */
	public static Object noBlank(Object... items) {
		if (items.length < 2)
			throw new DbProException("noBlank method need at least 2 args");
		for (int i = 0; i <= items.length - 1; i++)
			if (StrUtils.isBlankObject(items[i]))
				return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= items.length - 1; i++)
			sb.append(items[i]);
		return new Object[] {items[0], par(sb.toString())};
	}
	
	/** if condition true, return items  array, else return "" */
	public static Object when(boolean condition, Object... items) {
		return condition ? items : "";
	}

	/**
	 * Create "values(?,?,?...,?)" String according how many SQL parameters be
	 * cached
	 */
	public static SqlItem valuesQuestions() {
		return new SqlItem(SqlOption.VALUES_QUESTIONS);
	}
	
	public static SqlItem other(Object... otherInfos) {
		return new SqlItem(SqlOption.OTHER, otherInfos);
	}
	
	public static List<Object[]> getOthers() {
		return gctx().getOthers();
	}

	/**
	 * Switch to another DbPro
	 */
	public static SqlItem switchTo(DbPro dpPro) {
		return new SqlItem(SqlOption.SWITCHTO, dpPro);
	}

	/**
	 * For tXxxx style templateEngine use, return a SqlItemType.PUT type SqlItem
	 * instance,
	 * 
	 * Usage: put("key1",value1,"key2",value2...);
	 */
	public static SqlItem bind(Object... parameters) {
		return new SqlItem(SqlOption.BIND, parameters);
	}

	/**
	 * Create a SqlOption.IOC_OBJECT type SqlItem instance, args will create
	 * instance by IocTool
	 */
	public static SqlItem disableHandlers(Class<?>... args) {
		return new SqlItem(SqlOption.DISABLE_HANDLERS, (Object[]) args);
	}
	
	//======================
	
	
	
	
	
	
	
	
	
	/** Shortcut method equal to DbContext.getGlobalDbContext() */
	public static DbContext gctx() {
		return DbContext.getGlobalDbContext();
	}
	
	/** Shortcut call to dialect.pagin method */
    public static String pagin(int pageNumber, int pageSize, String sql) {
        return gctx().pagin(pageNumber, pageSize, sql);
    }

    /** Shortcut call to dialect.trans method */
    public static String trans(String sql) {
        return gctx().trans(sql);
    }

    /** Shortcut call to dialect.paginAndTrans method */
    public static String paginAndTrans(int pageNumber, int pageSize, String sql) {
        return gctx().paginAndTrans(pageNumber, pageSize, sql);
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

	/** Disable PaginHandler */
	public static SqlItem noPagin() {
		return new SqlItem(SqlOption.DISABLE_HANDLERS, PaginHandler.class);
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

	protected void ________Entity_Methods________() {// NOSONAR
	}
	//@formatter:off 
	//Entity series methods from DbContext
 
	public static <T> List<T> entityFind(Class<T> entityClass, Object... items) {return gctx().entityFind(entityClass, items);}
	public static <T> List<T> entityFindBySample(Object sampleBean, Object... items) {return gctx().entityFindBySample(sampleBean, items);}
	public static <T> List<T> entityFindBySql(Object... items) {return gctx().entityFindBySql(items);}
	public static <T> T entityFindOneBySQL(Object... items) {return gctx().entityFindOneBySQL(items);} 
	public static <T> T entityLoad(T entity, Object... items) {return gctx().entityLoad(entity, items);} 
	public static <T> T entityLoadById(Class<T> entityClass, Object entityId, Object... items) {return gctx().entityLoadById(entityClass, entityId, items);}
    public static <T> T entityLoadByIdTry(Class<T> entityClass, Object entityId, Object... items) {return gctx().entityLoadByIdTry(entityClass, entityId, items);}
    public static <T> T entityLoadBySql(Object... items) {return gctx().entityLoadBySql(items);}
	public static <T> T entityInsert(T entity, Object... items) {return gctx().entityInsert(entity, items);} 
    public static <T> T entityUpdate(Object entity, Object... items) {return gctx().entityUpdate(entity, items);}
	public static boolean entityExist(Object entity, Object... items) {return gctx().entityExist(entity, items);}
	public static boolean entityExistById(Class<?> entityClass, Object id, Object... items) {return gctx().entityExistById(entityClass, id, items);}
	public static int entityCount(Class<?> entityClass, Object... items) {return gctx().entityCount(entityClass, items);}
	public static int entityDeleteByIdTry(Class<?> entityClass, Object id, Object... items) {return gctx().entityDeleteByIdTry(entityClass, id, items);}
	public static int entityDeleteTry(Object entity, Object... items) {return gctx().entityDeleteTry(entity, items);}
	public static int entityLoadTry(Object entity, Object... items) {return gctx().entityLoadTry(entity, items);}
	public static int entityUpdateTry(Object entity, Object... items) {return gctx().entityUpdateTry(entity, items);}
	public static void entityDelete(Object entity, Object... items) { gctx().entityDelete(entity, items);}
	public static void entityDeleteById(Class<?> entityClass, Object id, Object... items) {gctx().entityDeleteById(entityClass, id, items);}
	public static <T> T entityFindRelatedOne(Object entity, Object... sqlItems) {return  gctx().entityFindRelatedOne(entity, sqlItems);}
	public static <T> List<T> entityFindRelatedList(Object entityOrIterable, Object... sqlItems) {return  gctx().entityFindRelatedList(entityOrIterable, sqlItems);}
	public static <T> Set<T> entityFindRelatedSet(Object entity, Object... sqlItems) {return  gctx().entityFindRelatedSet(entity, sqlItems);}
	public static <T> Map<Object, T> entityFindRelatedMap(Object entity, Object... sqlItems) {return  gctx().entityFindRelatedMap(entity, sqlItems);}
	public static EntityNet autoNet(Class<?>... entityClass) {return  gctx().autoNet(entityClass);}	 
	
	
	// simplilfied SQL methods 
	protected void ________SQL_Methods________() {}// NOSONAR
	
	public static <T> T qry(Object... items) {return  gctx().qry(items);}
	public static <T> T qryObject(Object... items) {return gctx().qryObject(items);}
	public static long qryLongValue(Object... items) {return gctx().qryLongValue(items);}
	public static int qryIntValue(Object... items) {return gctx().qryIntValue(items);}
	public static String qryString(Object... items) {return gctx().qryString(items);}
	public static List<Map<String, Object>> qryMapList(Object... items) {return gctx().qryMapList(items);}
	public static Map<String, Object> qryMap(Object... items) {return gctx().qryMap(items);}
	public static <T> List<T> qryList(Object... items) {return gctx().qryList(items);}
	public static int upd(Object... items) {return gctx().upd(items);}
	public static <T> T ins(Object... items) {return gctx().ins(items);}
	public static <T> T exe(Object... items) {return gctx().exe(items); }
	public static <T> List<T> qryEntityList(Object... items) {return gctx().qryEntityList(items);}
	public static PreparedSQL prepare(Object... items) { return gctx().prepare(items); }

  
}