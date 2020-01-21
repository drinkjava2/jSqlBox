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
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.OutParameter;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;

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
public class DbPro extends ImprovedQueryRunner implements NormalJdbcTool {// NOSONAR
	public DbPro() {
		super();
	}

	public DbPro(DataSource ds) {
		super(ds);
	}

	/**
	 * Quite execute a SQL, do not throw any exception, if any exception happen,
	 * return -1
	 */
	public int quiteExecute(String sql, Object... params) {
		try {
			return execute(sql, params);
		} catch (Exception e) {
			return -1;
		}
	}

	public void ________prepareMethods________() {// NOSONAR
	}

	/**
	 * Prepare a PreparedSQL for iXxxx (Single SQL) style, unknown objects (include
	 * null) will automatically looked as SQL pieces, more detail see doPrepare
	 * method
	 */
	public PreparedSQL iPrepare(Object... items) {
		return doPrepare(true, items);
	}

	/**
	 * Prepare a PreparedSQL for pXxxx (Single SQL) style, pXxxx style only allow
	 * single String (The first appeared) as SQL, unknown objects (include null)
	 * will automatically looked as SQL parameters, more detail see doPrepare method
	 */
	public PreparedSQL pPrepare(Object... items) {
		return doPrepare(false, items);
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
					throw new DbProException("In in-line style,  null value can not append as SQL piece");
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
			} else if (SqlOption.USE_TEMPLATE.equals(item)) {
				predSQL.setUseTemplate(true);
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
				for (Object pm : sqItem.getParameters())
					predSQL.addParam(pm);
			} else if (SqlOption.BIND.equals(sqlItemType)) {
				predSQL.addTemplateParam(sqItem);
			} else if (SqlOption.SQL.equals(sqlItemType)) {
				for (Object pm : sqItem.getParameters())
					predSQL.addSql(pm);
			} else if (SqlOption.QUESTION_PARAM.equals(sqlItemType)) {
				int i = 0;
				for (Object pm : sqItem.getParameters()) {
					predSQL.addParam(pm);
					if (i > 0)
						predSQL.addSql(",");
					predSQL.addSql("?");
					i++;
				}
			} else if (SqlOption.NOT_NULL.equals(sqlItemType)) {
				Object[] args = sqItem.getParameters();
				if (args.length < 2)
					throw new DbProException("NOT_NULL type SqlItem need at least 2 args");
				if (args[args.length - 1] != null) {
					for (int i = 0; i < args.length - 1; i++)
						dealOneSqlItem(true, predSQL, args[i]);// in NOT_NULL type, force use i style
					predSQL.addParam(args[args.length - 1]);
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
		} else
			return false;
		return true;
	}

	// ============================================================================

	public void ________iXxxxStyles________() {// NOSONAR
	}

	// ============================================================================

	/**
	 * Executes the in-line style query statement
	 * 
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return An object generated by the handler.
	 */
	public <T> T iQuery(Object... inlineSQL) {
		PreparedSQL ps = iPrepare(inlineSQL);
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
	public <T> T iQueryForObject(Object... inlineSQL) {
		PreparedSQL ps = iPrepare(inlineSQL);
		ps.ifNullSetType(SqlOption.QUERY);
		if (ps.getResultSetHandler() == null)
			ps.setResultSetHandler(new ScalarHandler<T>(1));
		return (T) runPreparedSQL(ps);
	}

	/**
	 * In-line style execute query and force return a long value, runtime exception
	 * may throw if result can not be cast to long.
	 */
	public long iQueryForLongValue(Object... inlineSQL) {
		return ((Number) iQueryForObject(inlineSQL)).longValue();// NOSONAR
	}

	/**
	 * In-line style execute query and force return a int, runtime exception may
	 * throw if result can not be cast to int.
	 */
	public int iQueryForIntValue(Object... inlineSQL) {
		return ((Number) iQueryForObject(inlineSQL)).intValue();// NOSONAR
	}

	/**
	 * In-line style execute query and force return a String object.
	 */
	public String iQueryForString(Object... inlineSQL) {
		return String.valueOf(iQueryForObject(inlineSQL));
	}

	/**
	 * In-Line style execute query and force return a List<Map<String, Object>> type
	 * result.
	 */
	public List<Map<String, Object>> iQueryForMapList(Object... items) {
		PreparedSQL ps = iPrepare(items);
		ps.addHandler(new MapListHandler());
		ps.ifNullSetType(SqlOption.QUERY);
		return (List<Map<String, Object>>) runPreparedSQL(ps);
	}

	/**
	 * Executes the in-line style INSERT, UPDATE, or DELETE statement
	 * 
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return The number of rows updated.
	 */
	public int iUpdate(Object... inlineSQL) {
		PreparedSQL ps = iPrepare(inlineSQL);
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
	public <T> T iInsert(Object... inlineSQL) {
		PreparedSQL ps = iPrepare(inlineSQL);
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
	public <T> T iExecute(Object... inlineSQL) {
		PreparedSQL ps = iPrepare(inlineSQL);
		ps.ifNullSetType(SqlOption.EXECUTE);
		return (T) runPreparedSQL(ps);
	}

	public void ________pXxxxStyles________() {// NOSONAR
	}

	/**
	 * Executes the pXxxx style query statement
	 * 
	 * @param items
	 *            The items
	 * @return An object generated by the handler.
	 */
	public <T> T pQuery(Object... items) {
		PreparedSQL ps = pPrepare(items);
		ps.ifNullSetType(SqlOption.QUERY);
		return (T) runPreparedSQL(ps);
	}

	/**
	 * Execute an pXxxx style query for an Object, only return the first row and
	 * first column's value if more than one column or more than 1 rows returned
	 * 
	 * @param items
	 *            The items
	 * @return An Object or null value determined by SQL content
	 */
	public <T> T pQueryForObject(Object... items) {
		PreparedSQL ps = pPrepare(items);
		ps.ifNullSetType(SqlOption.QUERY);
		if (ps.getResultSetHandler() == null)
			ps.setResultSetHandler(new ScalarHandler<T>(1));
		return (T) runPreparedSQL(ps);
	}

	/**
	 * pXxxx style execute query and force return a long value, runtime exception
	 * may throw if result can not be cast to long.
	 */
	public long pQueryForLongValue(Object... items) {
		return ((Number) pQueryForObject(items)).longValue();// NOSONAR
	}

	/**
	 * pXxxx style execute query and force return a int, runtime exception may throw
	 * if result can not be cast to int.
	 */
	public int pQueryForIntValue(Object... items) {
		return ((Number) pQueryForObject(items)).intValue();// NOSONAR
	}

	/**
	 * pXxxx style execute query and force return a String object.
	 */
	public String pQueryForString(Object... items) {
		Object o = pQueryForObject(items);
		return String.valueOf(o);
	}

	/**
	 * pXxxx style execute query and force return a List<Map<String, Object>> type
	 * result.
	 */
	public List<Map<String, Object>> pQueryForMapList(Object... items) {
		PreparedSQL ps = pPrepare(items);
		ps.addHandler(new MapListHandler());
		ps.ifNullSetType(SqlOption.QUERY);
		return (List<Map<String, Object>>) runPreparedSQL(ps);
	}

	/**
	 * Executes the pXxxx style INSERT, UPDATE, or DELETE statement
	 * 
	 * @param items
	 *            the items
	 * @return The number of rows updated.
	 */
	public int pUpdate(Object... items) {
		PreparedSQL ps = pPrepare(items);
		ps.ifNullSetType(SqlOption.UPDATE);
		return (Integer) runPreparedSQL(ps);
	}

	/**
	 * Executes the pXxxx style insert statement
	 * 
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return An object generated by the handler.
	 */
	public <T> T pInsert(Object... items) {
		PreparedSQL ps = pPrepare(items);
		ps.ifNullSetType(SqlOption.INSERT);
		return (T) runPreparedSQL(ps);
	}

	/**
	 * Executes the pXxxx style execute statement
	 * 
	 * @param items
	 *            the items
	 * @return A list of objects generated by the handler, or number of rows updated
	 *         if no handler
	 */
	public <T> T pExecute(Object... items) {
		PreparedSQL ps = pPrepare(items);
		ps.ifNullSetType(SqlOption.EXECUTE);
		return (T) runPreparedSQL(ps);
	}

	public void ________tXxxxStyles________() {// NOSONAR
	}

	/**
	 * Executes the pXxxx style query statement
	 * 
	 * @param items
	 *            The items
	 * @return An object generated by the handler.
	 */
	public <T> T tQuery(Object... items) {
		PreparedSQL ps = iPrepare(items);
		ps.ifNullSetUseTemplate(true);
		ps.ifNullSetType(SqlOption.QUERY);
		return (T) runPreparedSQL(ps);
	}

	/**
	 * Execute an pXxxx style query for an Object, only return the first row and
	 * first column's value if more than one column or more than 1 rows returned
	 * 
	 * @param items
	 *            The items
	 * @return An Object or null value determined by SQL content
	 */
	public <T> T tQueryForObject(Object... items) {
		PreparedSQL ps = iPrepare(items);
		ps.ifNullSetUseTemplate(true);
		ps.ifNullSetType(SqlOption.QUERY);
		if (ps.getResultSetHandler() == null)
			ps.setResultSetHandler(new ScalarHandler<T>(1));
		return (T) runPreparedSQL(ps);
	}

	/**
	 * pXxxx style execute query and force return a long value, runtime exception
	 * may throw if result can not be cast to long.
	 */
	public long tQueryForLongValue(Object... items) {
		return ((Number) tQueryForObject(items)).longValue();// NOSONAR
	}

	/**
	 * pXxxx style execute query and force return a int value, runtime exception may
	 * throw if result can not be cast to int.
	 */
	public int tQueryForIntValue(Object... items) {
		return ((Number) tQueryForObject(items)).intValue();// NOSONAR
	}

	/**
	 * pXxxx style execute query and force return a String object.
	 */
	public String tQueryForString(Object... items) {
		return String.valueOf(tQueryForObject(items));
	}

	/**
	 * pXxxx style execute query and force return a List<Map<String, Object>> type
	 * result.
	 */
	public List<Map<String, Object>> tQueryForMapList(Object... items) {
		PreparedSQL ps = iPrepare(items);
		ps.ifNullSetUseTemplate(true);
		ps.addHandler(new MapListHandler());
		ps.ifNullSetType(SqlOption.QUERY);
		return (List<Map<String, Object>>) runPreparedSQL(ps);
	}

	/**
	 * Executes the pXxxx style INSERT, UPDATE, or DELETE statement
	 * 
	 * @param items
	 *            the items
	 * @return The number of rows updated.
	 */
	public int tUpdate(Object... items) {
		PreparedSQL ps = iPrepare(items);
		ps.ifNullSetUseTemplate(true);
		ps.ifNullSetType(SqlOption.UPDATE);
		return (Integer) runPreparedSQL(ps);
	}

	/**
	 * Executes the pXxxx style insert statement
	 * 
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return An object generated by the handler.
	 */
	public <T> T tInsert(Object... items) {
		PreparedSQL ps = iPrepare(items);
		ps.ifNullSetUseTemplate(true);
		ps.ifNullSetType(SqlOption.INSERT);
		return (T) runPreparedSQL(ps);
	}

	/**
	 * Executes the pXxxx style execute statement
	 * 
	 * @param items
	 *            the items
	 * @return A list of objects generated by the handler, or number of rows updated
	 *         if no handler
	 */
	public <T> T tExecute(Object... items) {
		PreparedSQL ps = iPrepare(items);
		ps.ifNullSetUseTemplate(true);
		ps.ifNullSetType(SqlOption.EXECUTE);
		return (T) runPreparedSQL(ps);
	}

	/**
	 * nXxxx style series methods are design to replace QueryRunner's xxxx method,
	 * the difference is nXxxx methods do not throw SqlException
	 */
	public void ________nXxxxStyles________() {// NOSONAR

	}

	/**
	 * Executes the given SELECT SQL query and returns a result object.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param sql
	 *            the SQL
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code>.
	 * @param params
	 *            the parameters if have
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T nQuery(Connection conn, ResultSetHandler<T> rsh, String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.QUERY, conn, rsh, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (T) runPreparedSQL(ps);
	}

	/**
	 * Query for an Object, only return the first row and first column's value if
	 * more than one column or more than 1 rows returned, a null object may return
	 * if no result found , DbProRuntimeException may be threw if some SQL operation
	 * Exception happen.
	 * 
	 * @param sql
	 * @param params
	 * @return An Object or null, Object type determined by SQL content
	 */
	public <T> T nQueryForObject(Connection conn, String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.QUERY, conn, SingleTonHandlers.scalarHandler, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (T) runPreparedSQL(ps);
	}

	/**
	 * Execute query and force return a String object, no need catch SQLException.
	 * 
	 */
	public String nQueryForString(Connection conn, String sql, Object... params) {
		return nQueryForObject(conn, sql, params);
	}

	/**
	 * Execute query and force return a long value, no need catch SQLException,
	 * runtime exception may throw if result can not be cast to long.
	 */
	public long nQueryForLongValue(Connection conn, String sql, Object... params) {
		return ((Number) nQueryForObject(conn, sql, params)).longValue();// NOSONAR
	}

	/**
	 * Execute query and force return a int, no need catch SQLException, runtime
	 * exception may throw if result can not be cast to int.
	 */
	public int nQueryForIntValue(Connection conn, String sql, Object... params) {
		return ((Number) nQueryForObject(conn, sql, params)).intValue();// NOSONAR
	}

	/**
	 * Execute query and force return a List<Map<String, Object>> type result, no
	 * need catch SQLException.
	 */
	public List<Map<String, Object>> nQueryForMapList(Connection conn, String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.QUERY, conn, SingleTonHandlers.mapListHandler, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (List<Map<String, Object>>) runPreparedSQL(ps);
	}

	/**
	 * Executes the given INSERT, UPDATE, or DELETE SQL statement.
	 * 
	 * @param sql
	 *            the SQL
	 * @param params
	 *            the parameters if have
	 * @return The number of rows updated.
	 */
	public int nUpdate(Connection conn, String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.UPDATE, conn, null, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (Integer) runPreparedSQL(ps);
	}

	/**
	 * Executes the given INSERT SQL statement. Note: This method does not close
	 * connection.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The resultSetHandler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param sql
	 *            the SQL
	 * @param params
	 *            the parameters if have
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T nInsert(Connection conn, ResultSetHandler<T> rsh, String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.INSERT, conn, rsh, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (T) runPreparedSQL(ps);
	}

	/**
	 * Execute an statement, including a stored procedure call, which does not
	 * return any result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters.
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets.
	 * 
	 * @param sql
	 *            the SQL
	 * @return The number of rows updated.
	 */
	public int nExecute(Connection conn, String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.EXECUTE, conn, null, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (Integer) runPreparedSQL(ps);
	}

	/**
	 * Execute an statement, including a stored procedure call, which returns one or
	 * more result sets. Any parameters which are instances of {@link OutParameter}
	 * will be registered as OUT parameters. Note: This method does not close
	 * connection.
	 * 
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters.
	 *
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The result set handler
	 * @param sql
	 *            the SQL
	 * @return A list of objects generated by the handler
	 * 
	 */
	public <T> List<T> nExecute(Connection conn, ResultSetHandler<T> rsh, String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.EXECUTE, conn, rsh, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (List<T>) runPreparedSQL(ps);
	}

	/**
	 * Executes the given SELECT SQL query and returns a result object.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param sql
	 *            the SQL
	 * @param rsh
	 *            The resultSetHandler used to create the result object from the
	 *            <code>ResultSet</code>.
	 * @param params
	 *            the parameters if have
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T nQuery(ResultSetHandler<T> rsh, String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.QUERY, null, rsh, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (T) runPreparedSQL(ps);
	}

	/**
	 * Query for an Object, only return the first row and first column's value if
	 * more than one column or more than 1 rows returned, a null object may return
	 * if no result found , DbProRuntimeException may be threw if some SQL operation
	 * Exception happen.
	 * 
	 * @param sql
	 * @param params
	 * @return An Object or null, Object type determined by SQL content
	 */
	@Override
	public <T> T nQueryForObject(String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.QUERY, null, SingleTonHandlers.scalarHandler, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (T) runPreparedSQL(ps);
	}

	// ============================================================================

	/**
	 * Execute query and force return a String object, no need catch SQLException
	 */
	public String nQueryForString(String sql, Object... params) {
		return String.valueOf((Object)nQueryForObject(sql, params));
	}

	/**
	 * Execute query and force return a long value, no need catch SQLException,
	 * runtime exception may throw if result can not be cast to long
	 */
	public long nQueryForLongValue(String sql, Object... params) {
		return ((Number) nQueryForObject(sql, params)).longValue();// NOSONAR
	}

	/**
	 * Execute query and force return a int value, no need catch SQLException,
	 * runtime exception may throw if result can not be cast to int
	 */
	public int nQueryForIntValue(String sql, Object... params) {
		return ((Number) nQueryForObject(sql, params)).intValue();// NOSONAR
	}

	/**
	 * Execute query and force return a List<Map<String, Object>> type result, no
	 * need catch SQLException
	 */
	public List<Map<String, Object>> nQueryForMapList(String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.QUERY, null, SingleTonHandlers.mapListHandler, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (List<Map<String, Object>>) runPreparedSQL(ps);
	}

	/**
	 * Executes the given INSERT, UPDATE, or DELETE SQL statement.
	 * 
	 * @param sql
	 *            the SQL
	 * @param params
	 *            the parameters if have
	 * @return The number of rows updated.
	 */
	@Override
	public int nUpdate(String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.UPDATE, null, null, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (Integer) runPreparedSQL(ps);
	}

	/**
	 * Executes the given INSERT SQL statement.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The resultSetHandler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param sql
	 *            the SQL
	 * @param params
	 *            the parameters if have
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T nInsert(ResultSetHandler rsh, String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.INSERT, null, rsh, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (T) runPreparedSQL(ps);
	}

	/**
	 * Execute an statement, including a stored procedure call, which does not
	 * return any result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters.
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets.
	 * 
	 * @param sql
	 *            the SQL
	 * @return The number of rows updated.
	 */
	@Override
	public int nExecute(String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.EXECUTE, null, null, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (Integer) runPreparedSQL(ps);
	}

	/**
	 * Execute an statement, including a stored procedure call, which returns one or
	 * more result sets. Any parameters which are instances of {@link OutParameter}
	 * will be registered as OUT parameters.
	 * 
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters.
	 *
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The result set handler
	 * @param sql
	 *            the SQL
	 * @return A list of objects generated by the handler
	 * 
	 */
	public <T> List<T> nExecute(ResultSetHandler rsh, String sql, Object... params) {
		PreparedSQL ps = new PreparedSQL(SqlOption.EXECUTE, null, rsh, sql, params);
		ps.addGlobalAndThreadedHandlers(this);
		return (List<T>) runPreparedSQL(ps);
	}

}
