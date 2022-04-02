/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jdbpro;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.StatementConfiguration;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jdialects.Dialect;

/**
 * DbPro is the enhanced version of Apache Commons DbUtils's QueryRunner, add
 * below improvements:
 * 
 * <pre>
 * 1)Use ConnectionManager to manage connection for better transaction support
 * 2)normal style methods but no longer throw SQLException, methods named as nXxxxx() format
 * 3)In-line style methods, methods named as iXxxxx() format
 * 4)SQL Template style methods, methods named as tXxxxx() format
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DbPro extends ImprovedQueryRunner {// NOSONAR
	public DbPro() {
		super();
	}

	public DbPro(DataSource ds) {
		super(ds);
	}

	public DbPro(DataSource ds, Dialect dialect) {
		super(ds, dialect);
	}
	
	public DbPro(DataSource ds, StatementConfiguration stmtConfig) {
		super(ds, stmtConfig); 
	}

	public DbPro(DataSource ds, Dialect dialect, StatementConfiguration stmtConfig) {
		super(ds, dialect, stmtConfig); 
	}
	

	/**
	 * Quite execute a SQL, do not throw any exception, if any exception happen,
	 * return -1
	 */
	public int quiteExecute(String... sqls) {
		int result = 0;
		for (String sql : sqls)
			try {
				execute(sql);
			} catch (Exception e) {
				result = -1;
			}
		return result;
	}

	public void ________prepareMethods________() {// NOSONAR
	}

	/**
	 * Prepare a PreparedSQL for iXxxx (Single SQL) style, unknown objects (include
	 * null) will automatically looked as SQL pieces, more detail see doPrepare
	 * method
	 */
	public PreparedSQL prepare(Object... items) {
		return doPrepare(true, items);
	}

	/**
	 * Prepare a PreparedSQL for iXxxx (In-line) style or pXxxx style, For in-line
	 * style, unknown items be treated as String, SQL parameters must written in
	 * param() method, for example:
	 * 
	 * ctx.iQuery(new SimpleCacheHandler(), connection, "select u.** from users u
	 * where u.age>?", param(20)," and u.id=?", param("001"), MapListHandler.class);
	 * 
	 * 
	 * pXxxx style only allow first appeared String as SQL, left unknown items will
	 * be treated as SQL parameters, for example:
	 * 
	 * ctx.pQuery(MapListHandler.class, "select * from users where age>? and id=?",
	 * 20 , "001" , connection, new PaginHandler(2,5), sql(" and name=?"), "Tom" );
	 * 
	 * In above examples connection and sqlHandlers are optional items, these
	 * optional items can appear at anywhere
	 * 
	 * @param items
	 *            SQL String / SQL Parameters / Connection / ResultSetHandler class
	 *            or instance / SqlHandler class or instance
	 * @return a PreparedSQL instance
	 */
	private PreparedSQL doPrepare(boolean inlineStyle, Object... items) {// NOSONAR
		PreparedSQL ps = dealSqlItems(null, inlineStyle, items);
		ps.addGlobalAndThreadedHandlers(this); 
		return ps;
	}

	/** Convert parameters to JDBC type, like java.util.Date to java.sql.Date */
	public void preparedParamsToJdbc(PreparedSQL ps) {
		//empty method for child class override
	}

	/**
	 * Deal with multiple SqlItems
	 */
	public PreparedSQL dealSqlItems(PreparedSQL lastPreSql, boolean inlineStyle, Object... items) {// NOSONAR
		if (items == null || items.length == 0)
			throw new DbProException("prepareSQL items can not be empty");
		PreparedSQL predSQL = lastPreSql;
		if (predSQL == null)
			predSQL = new PreparedSQL();
		for (Object item : items) {
			if (item == null) {
				if (inlineStyle)
					throw new DbProException("null value can not append as SQL piece");
				else
					predSQL.addParam(null);
			} else if (!dealOneSqlItem(inlineStyle, predSQL, item)) {
				if (item instanceof SqlItem)
					throw new DbProException("One SqlItem did not find explainer, type=" + ((SqlItem) item).getType());
				if (item.getClass().isArray()) {
					Object[] array = (Object[]) item;
					if (array.length != 0)
						dealSqlItems(predSQL, inlineStyle, (Object[]) item);
				} else if (inlineStyle)
					predSQL.addSql(item); // iXxxx style, unknown object look as SQL piece
				else
					predSQL.addParam(item); // pXxxx style, unknown object look as parameter
			}
		}
		predSQL.setSql(predSQL.getSqlBuilder().toString());
		return predSQL;
	}

	/**
	 * Here deal one SqlItem, if can deal it, return true, otherwise return false,
	 * subclass (like DbContext) can override this method
	 */
	protected boolean dealOneSqlItem(boolean inlineStyle, PreparedSQL predSQL, Object item) {// NOSONAR
		if (item instanceof String) {
			predSQL.addSqlOrParam(inlineStyle, (String) item);
		} else if (item instanceof PreparedSQL) {
			PreparedSQL psItem = (PreparedSQL) item;
			if (psItem.getSql() != null)
				predSQL.addSql(psItem.getSql());
			if (psItem.getParams() != null)
				for (Object obj : psItem.getParams())
					predSQL.addParam(obj);
		} else if (item instanceof SqlTemplateEngine) {
			predSQL.setTemplateEngine((SqlTemplateEngine) item);
		} else if (item instanceof Map<?, ?>) {
			predSQL.addTemplateMap((Map<String, Object>) item);
		} else if (item instanceof SqlOption) {
			if (SqlOption.USE_MASTER.equals(item)) {
				predSQL.setMasterSlaveOption(SqlOption.USE_MASTER);
			} else if (SqlOption.USE_SLAVE.equals(item)) {
				predSQL.setMasterSlaveOption(SqlOption.USE_SLAVE);
			} else if (SqlOption.USE_AUTO.equals(item)) {
				predSQL.setMasterSlaveOption(SqlOption.USE_AUTO);
			} else if (SqlOption.USE_BOTH.equals(item)) {
				predSQL.setMasterSlaveOption(SqlOption.USE_BOTH);
			} else if (SqlOption.EXECUTE.equals(item)) {
				predSQL.setOperationType(SqlOption.EXECUTE);
			} else if (SqlOption.UPDATE.equals(item)) {
				predSQL.setOperationType(SqlOption.UPDATE);
			} else if (SqlOption.QUERY.equals(item)) {
				predSQL.setOperationType(SqlOption.QUERY);
			} else if (SqlOption.INSERT.equals(item)) {
				predSQL.setOperationType(SqlOption.INSERT);
			} else
				return false;
		} else if (item instanceof SqlItem) {
			SqlItem sqItem = (SqlItem) item;
			SqlOption sqlItemType = sqItem.getType();
			if (SqlOption.OTHER.equals(sqlItemType))
				predSQL.addOther(sqItem);
			else if (SqlOption.PARAM.equals(sqlItemType)) {
				if(sqItem.getParameters()==null )
					predSQL.addParam(null);
				else for (Object pm : sqItem.getParameters())
					predSQL.addParam(pm);
			} else if (SqlOption.BIND.equals(sqlItemType)) {
				predSQL.addTemplateParam(sqItem);
			} else if (SqlOption.SQL.equals(sqlItemType)) {
				for (Object pm : sqItem.getParameters())
					predSQL.addSql(pm);
			} else if (SqlOption.QUESTION_PARAM.equals(sqlItemType)) {
				if (sqItem.getParameters() == null) {
					predSQL.addParam(null);
					predSQL.addSql("?");
				} else {
					int i = 0;
					for (Object pm : sqItem.getParameters()) {
						predSQL.addParam(pm);
						if (i > 0)
							predSQL.addSql(",");
						predSQL.addSql("?");
						i++;
					}
				}
			} else if (SqlOption.VALUES_QUESTIONS.equals(sqlItemType)) {
				predSQL.addSql(" values(");
				for (int i = 0; i < predSQL.getParamSize(); i++) {
					if (i > 0)
						predSQL.addSql(",");
					predSQL.addSql("?");
				}
				predSQL.addSql(")");
			} else if (SqlOption.ENABLE_HANDLERS.equals(sqlItemType)) {
				predSQL.enableAllHandlers();
			} else if (SqlOption.DISABLE_HANDLERS.equals(sqlItemType)) {
				predSQL.disableHandlers((Object[]) sqItem.getParameters());
			} else if (SqlOption.SWITCHTO.equals(sqlItemType)) {
				predSQL.setSwitchTo((DbPro) sqItem.getParameters()[0]);
			} else
				return false;
		} else if (item instanceof Text)
			predSQL.addSql(item.toString());
		else if (item instanceof Connection)
			predSQL.setConnection((Connection) item);
		else if (item instanceof DbPro)
			predSQL.setSwitchTo((DbPro) item);
		else if (item instanceof SqlHandler)
			predSQL.addHandler((SqlHandler) item);
		else if (item instanceof ResultSetHandler)
			predSQL.setResultSetHandler((ResultSetHandler) item);
		else if (item instanceof Class) {
			if (Text.class.isAssignableFrom((Class) item)) {
				String text = Text.classToString((Class) item);
				predSQL.addSqlOrParam(inlineStyle, text);
				return true;
			} else
				return false;
		} else if (item instanceof CustomizedSqlItem) {
			((CustomizedSqlItem) item).doPrepare(predSQL);
		} else if(sqlItemHandler!=null)
			return sqlItemHandler.handle(predSQL, item);
		  else
			return false;
		return true;
	}

	public void ________3letter_inlineStyles________() {// NOSONAR
	}

	/**
	 * Executes the in-line style query statement
	 * 
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return An object generated by the handler.
	 */
	public <T> T qry(Object... inlineSQL) {
		PreparedSQL ps = prepare(inlineSQL);
		ps.ifNullSetType(SqlOption.QUERY);
		return (T) runPreparedSQL(ps);
	}

	/**
	 * Execute an In-line style query for an Object, only return the first row and
	 * first column's value if more than one column or more than 1 rows returned
	 * 
	 * @param inlineSQL
	 * @param params
	 * @return An Object or null value determined by SQL content
	 */
	public <T> T qryObject(Object... inlineSQL) {
		PreparedSQL ps = prepare(inlineSQL);
		ps.ifNullSetType(SqlOption.QUERY);
		if (ps.getResultSetHandler() == null)
			ps.setResultSetHandler(new ScalarHandler<T>(1));
		return (T) runPreparedSQL(ps);
	}

	/**
	 * In-line style execute query and force return a long value, runtime exception
	 * may throw if result can not be cast to long.
	 */
	public long qryLongValue(Object... inlineSQL) {
		return ((Number) qryObject(inlineSQL)).longValue();// NOSONAR
	}

	/**
	 * In-line style execute query and force return a int, runtime exception may
	 * throw if result can not be cast to int.
	 */
	public int qryIntValue(Object... inlineSQL) {
		return ((Number) qryObject(inlineSQL)).intValue();// NOSONAR
	}

	 /**
     * In-line style execute query and force return a boolean, runtime exception may
     * throw if result can not be cast to boolean.
     */
    public boolean qryBooleanValue(Object... inlineSQL) {
        return (Boolean) qryObject(inlineSQL);// NOSONAR
    }
    
    
	/**
	 * In-line style execute query and force return a String object.
	 */
	public String qryString(Object... inlineSQL) {
		Object result = qryObject(inlineSQL);
		return result == null ? null : result.toString();
	}

	/**
	 * In-Line style execute query and force return a List<Map<String, Object>> type
	 * result.
	 */
	public List<Map<String, Object>> qryMapList(Object... items) {
		PreparedSQL ps = prepare(items);
		ps.addHandler(new MapListHandler());
		ps.ifNullSetType(SqlOption.QUERY);
		return (List<Map<String, Object>>) runPreparedSQL(ps);
	}
	
	/**
	 * In-Line style execute query and force return a Map<String, Object> type
	 * result, if no record found, return empty HashMap<String, Object> instance;
	 */
	public Map<String, Object> qryMap(Object... items) {
		List<Map<String, Object>> list = qryMapList(items);
		if (list.isEmpty())
			return new HashMap<String, Object>();
		return list.get(0);
	}

	/**
	 * In-Line style execute query and force return a Map<String, Object> type
	 * result, if no record found, return empty HashMap<String, Object> instance;
	 */
	public <T> List<T> qryList(Object... items) {
		return this.qry(new ColumnListHandler<T>(1), items); 
	}
	
	
	/**
	 * Executes the in-line style INSERT, UPDATE, or DELETE statement
	 * 
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return The number of rows updated.
	 */
	public int upd(Object... inlineSQL) {
		PreparedSQL ps = prepare(inlineSQL);
		ps.ifNullSetType(SqlOption.UPDATE);
		return (Integer) runPreparedSQL(ps);
	}

	/**
	 * Executes the in-line style insert statement
	 * 
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return An object generated by the handler.
	 */
	public <T> T ins(Object... inlineSQL) {
		PreparedSQL ps = prepare(inlineSQL);
		ps.ifNullSetType(SqlOption.INSERT);
		return (T) runPreparedSQL(ps);
	}

	/**
	 * Executes the in-line style execute statement
	 * 
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return A list of objects generated by the handler, or number of rows updated
	 *         if no handler
	 */
	public <T> T exe(Object... inlineSQL) {
		PreparedSQL ps = prepare(inlineSQL);
		ps.ifNullSetType(SqlOption.EXECUTE);
		return (T) runPreparedSQL(ps);
	}

	/**
	 * nXxxx style series methods are design to replace QueryRunner's xxxx method,
	 * the difference is nXxxx methods do not throw SqlException
	 */
	public void ________jdbcMethods________() {// NOSONAR

	}

 
	// ============================================================================


}
