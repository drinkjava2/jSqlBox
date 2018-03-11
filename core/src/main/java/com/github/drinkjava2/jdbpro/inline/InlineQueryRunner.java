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
package com.github.drinkjava2.jdbpro.inline;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.OutParameter;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdbpro.DbProRuntimeException;
import com.github.drinkjava2.jdbpro.improve.ImprovedQueryRunner;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * InlineQueryRunner added In-line style SQL support for QueryRunner
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class InlineQueryRunner extends ImprovedQueryRunner {

	public InlineQueryRunner() {
		super();
	}

	public InlineQueryRunner(DataSource ds) {
		super(ds);
	}

	public InlineQueryRunner(DataSource ds, ConnectionManager cm) {
		super(ds, cm);
	}

	/**
	 * A ThreadLocal variant for temporally store parameters in current Thread
	 */
	protected static ThreadLocal<ArrayList<Object>> inlineThreadlocalParamCache = new ThreadLocal<ArrayList<Object>>() {
		@Override
		protected ArrayList<Object> initialValue() {
			return new ArrayList<Object>();
		}
	};

	/** Clear all in-line parameters stored in ThreadLocal */
	protected static void clearParams() {
		inlineThreadlocalParamCache.get().clear();
	}

	/**
	 * Clear all ThreadLocal parameters first, then cache parameters in ThreadLocal
	 * and return an empty String, a non parameter param0() call equal to
	 * clearAllInlineThreadlocalParams()
	 */
	public static String param0(Object... parameters) {
		clearParams();
		return param(parameters);
	}

	/**
	 * Cache parameters in ThreadLocal and return an empty String
	 */
	public static String param(Object... parameters) {
		for (Object o : parameters)
			inlineThreadlocalParamCache.get().add(o);
		return "";
	}

	/**
	 * Clear all ThreadLocal parameters first, then cache parameters in ThreadLocal,
	 * then return a "?" String
	 */
	public static String question0(Object... parameters) {
		clearParams();
		return question(parameters);
	}

	/**
	 * Cache parameters in ThreadLocal and return a "?" String
	 */
	public static String question(Object... parameters) {
		for (Object o : parameters)
			inlineThreadlocalParamCache.get().add(o);
		return "?";
	}

	/**
	 * Create "values(?,?,?...,?)" String according how many SQL parameters be
	 * cached in ThreadLocal
	 */
	public static String valuesQuesions() {
		int howManyQuestionMarks = inlineThreadlocalParamCache.get().size();
		StringBuilder sb = new StringBuilder("values(");
		for (int i = 0; i < howManyQuestionMarks; i++) {
			if (i != howManyQuestionMarks - 1)
				sb.append("?,");
			else
				sb.append("?");
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Build a PreparedSQL instance by given in-line style SQL and parameters
	 * stored in ThreadLocal
	 * 
	 * @param inlineSQL
	 * @return PreparedSQL instance
	 */
	public static PreparedSQL inlineToSqlAndParams(String... inlineSQL) {
		try {
			String sql = null;
			if (inlineSQL != null) {
				StringBuilder sb = new StringBuilder("");
				for (String str : inlineSQL)
					sb.append(str);
				sql = sb.toString();
			}
			ArrayList<Object> params = inlineThreadlocalParamCache.get();
			PreparedSQL sp = new PreparedSQL();
			sp.setSql(sql);
			sp.setParams(params.toArray(new Object[params.size()]));
			return sp;
		} finally {
			clearParams();
		}
	}

	/**
	 * Usually used to translate a Bean to "field1,field2,...fieldx" format or
	 * "field1=?,field2=?.... fieldx=?" format inLineSQL String piece, and save bean
	 * property parameters in ThreadLocal
	 * 
	 * @param bean
	 *            The Bean will be transfer to SQL piece
	 * @param conditionStr
	 *            The condition String
	 * @param separatorStr
	 *            The separator String
	 * @return a SQL piece and store all bean properties as parameters in
	 *         ThreadLocaled
	 */
	public static String inline(Object bean, String conditionStr, String separatorStr) {
		DbProRuntimeException.assertNotNull(bean, "DbProBeanUtils bean can not be null");
		Class<?> beanClass = bean.getClass();
		BeanInfo beanInfo = null;
		PropertyDescriptor[] pds = null;
		try {
			beanInfo = Introspector.getBeanInfo(beanClass);
			pds = beanInfo.getPropertyDescriptors();
		} catch (Exception e) {
			throw new DbProRuntimeException("DbProBeanUtils  fail to get bean Properties.", e);
		}
		if (pds == null || pds.length < 1)
			return "";

		StringBuilder sb = new StringBuilder();
		Object[] params = new Object[pds.length - 1];
		int i = 0;
		for (PropertyDescriptor pd : pds) {
			String fieldName = pd.getName();
			if (!"class".equals(fieldName)) {
				Method md = pd.getReadMethod();
				try {
					Object value = md.invoke(bean);
					sb.append(fieldName).append(conditionStr).append(separatorStr);
					params[i++] = value;
				} catch (Exception e) {
					throw new DbProRuntimeException("DbProBeanUtils fail to get bean Properties.", e);
				}
			}
		}
		sb.setLength(sb.length() - separatorStr.length());
		for (Object param : params) {
			DbPro.param(param);
		}
		return sb.toString();
	}

	/**
	 * Same function like inline() method but clean ThreadLocal cache first
	 */
	public static String inline0(Object bean, String conditionStr, String separatorStr) {
		DbPro.param0();
		return inline(bean, conditionStr, separatorStr);
	}

	// ======================================================
	// =============== SQL methods below ====================
	// ======================================================

	/**
	 * Executes the given SELECT SQL query and returns a result object. Note: this
	 * method does not close connection
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code>.
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T iQuery(Connection conn, ResultSetHandler rsh, String... inlineSQL) {
		try {
			PreparedSQL sp = inlineToSqlAndParams(inlineSQL);
			return (T) this.query(conn, sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the given SELECT SQL query and returns a result object.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code>.
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T iQuery(ResultSetHandler rsh, String... inlineSQL) {
		try {
			PreparedSQL sp = inlineToSqlAndParams(inlineSQL);
			return (T) this.query(sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an In-line style query for an Object, only return the first row and
	 * first column's value if more than one column or more than 1 rows returned, a
	 * null object may return if no result found , DbProRuntimeException may be
	 * threw if some SQL operation Exception happen. Note: this method does not
	 * close connection
	 * 
	 * @param sql
	 * @param params
	 * @return An Object or null, Object type determined by SQL content
	 */
	public <T> T iQueryForObject(Connection conn, String... inlineSQL) {
		return iQuery(conn, new ScalarHandler<T>(1), inlineSQL);
	}

	/**
	 * Execute an In-line style query for an Object, only return the first row and
	 * first column's value if more than one column or more than 1 rows returned, a
	 * null object may return if no result found , DbProRuntimeException may be
	 * threw if some SQL operation Exception happen.
	 * 
	 * @param sql
	 * @param params
	 * @return An Object or null, Object type determined by SQL content
	 */
	public <T> T iQueryForObject(String... inlineSQL) {
		return iQuery(new ScalarHandler<T>(1), inlineSQL);
	}

	/**
	 * In-line style execute query and force return a long value, runtime exception
	 * may throw if result can not be cast to long. Note: this method does not close
	 * connection
	 */
	public long iQueryForLongValue(Connection conn, String... inlineSQL) {
		return ((Number) iQueryForObject(conn, inlineSQL)).longValue();// NOSONAR
	}

	/**
	 * In-line style execute query and force return a long value, runtime exception
	 * may throw if result can not be cast to long
	 */
	public long iQueryForLongValue(String... inlineSQL) {
		return ((Number) iQueryForObject(inlineSQL)).longValue();// NOSONAR
	}

	/**
	 * In-line style execute query and force return a String object. Note: This
	 * method does not close connection.
	 */
	public String iQueryForString(Connection conn, String... inlineSQL) {
		return String.valueOf(iQueryForObject(conn, inlineSQL));
	}

	/** In-line style execute query and force return a String object */
	public String iQueryForString(String... inlineSQL) {
		return String.valueOf(iQueryForObject(inlineSQL));
	}

	/**
	 * In-Line style execute query and force return a List<Map<String, Object>> type
	 * result. Note: this method does not close connection
	 */
	public List<Map<String, Object>> iQueryForMapList(Connection conn, String... inlineSQL) {
		return iQuery(conn, new MapListHandler(), inlineSQL);
	}

	/**
	 * In-Line style execute query and force return a List<Map<String, Object>> type
	 * result
	 */
	public List<Map<String, Object>> iQueryForMapList(String... inlineSQL) {
		return iQuery(new MapListHandler(), inlineSQL);
	}

	/**
	 * Executes the given INSERT, UPDATE, or DELETE SQL statement. Note: this method
	 * does not close connection
	 * 
	 * @param inlineSQL
	 *            the in-line style SQL *
	 * @return The number of rows updated.
	 */
	public int iUpdate(Connection conn, String... inlineSQL) {
		try {
			PreparedSQL sp = inlineToSqlAndParams(inlineSQL);
			return update(conn, sp.getSql(), sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the given INSERT, UPDATE, or DELETE SQL statement. Transaction mode
	 * is determined by connectionManager property.
	 * 
	 * @param inlineSQL
	 *            the in-line style SQL *
	 * @return The number of rows updated.
	 */
	public int iUpdate(String... inlineSQL) {
		try {
			PreparedSQL sp = inlineToSqlAndParams(inlineSQL);
			return this.update(sp.getSql(), sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the given INSERT SQL statement. Note: this method does not close
	 * connection
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T iInsert(Connection conn, ResultSetHandler rsh, String... inlineSQL) {
		try {
			PreparedSQL sp = inlineToSqlAndParams(inlineSQL);
			return (T) insert(conn, sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Executes the given INSERT SQL statement. Transaction mode is determined by
	 * connectionManager property.
	 * 
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The handler used to create the result object from the
	 *            <code>ResultSet</code> of auto-generated keys.
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return An object generated by the handler.
	 * 
	 */
	public <T> T iInsert(ResultSetHandler rsh, String... inlineSQL) {
		try {
			PreparedSQL sp = inlineToSqlAndParams(inlineSQL);
			return (T) insert(sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an statement, including a stored procedure call, which returns one or
	 * more result sets. Any parameters which are instances of {@link OutParameter}
	 * will be registered as OUT parameters. Note: this method does not close
	 * connection
	 * <p>
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters.
	 *
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The result set handler
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return A list of objects generated by the handler
	 */
	public <T> List<T> iExecute(Connection conn, ResultSetHandler rsh, String... inlineSQL) {
		try {
			PreparedSQL sp = inlineToSqlAndParams(inlineSQL);
			return (List<T>) this.execute(conn, sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an statement, including a stored procedure call, which returns one or
	 * more result sets. Any parameters which are instances of {@link OutParameter}
	 * will be registered as OUT parameters.Transaction mode is determined by
	 * connectionManager property.
	 * <p>
	 * Use this method when: a) running SQL statements that return multiple result
	 * sets; b) invoking a stored procedure that return result sets and OUT
	 * parameters.
	 *
	 * @param <T>
	 *            The type of object that the handler returns
	 * @param rsh
	 *            The result set handler
	 * @param inlineSQL
	 *            the in-line style SQL
	 * @return A list of objects generated by the handler
	 */
	public <T> List<T> iExecute(ResultSetHandler rsh, String... inlineSQL) {
		try {
			PreparedSQL sp = inlineToSqlAndParams(inlineSQL);
			return (List<T>) this.execute(sp.getSql(), rsh, sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an statement, including a stored procedure call, which does not
	 * return any result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters. Note: this method
	 * does not close connection
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets.
	 *
	 * @param inlineSQL
	 *            the in-line style SQL.
	 * @return The number of rows updated.
	 */
	public int iExecute(Connection conn, String... inlineSQL) {
		try {
			PreparedSQL sp = inlineToSqlAndParams(inlineSQL);
			return this.execute(conn, sp.getSql(), sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

	/**
	 * Execute an statement, including a stored procedure call, which does not
	 * return any result sets. Any parameters which are instances of
	 * {@link OutParameter} will be registered as OUT parameters. Transaction mode
	 * is determined by connectionManager property.
	 * <p>
	 * Use this method when invoking a stored procedure with OUT parameters that
	 * does not return any result sets.
	 *
	 * @param inlineSQL
	 *            the in-line style SQL.
	 * @return The number of rows updated.
	 */
	public int iExecute(String... inlineSQL) {
		try {
			PreparedSQL sp = inlineToSqlAndParams(inlineSQL);
			return this.execute(sp.getSql(), sp.getParams());
		} catch (SQLException e) {
			throw new DbProRuntimeException(e);
		}
	}

}
