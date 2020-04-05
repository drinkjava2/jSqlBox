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
package com.github.drinkjava2.jsqlbox.handler;

import com.github.drinkjava2.jdbpro.DefaultOrderSqlHandler;
import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.DbException;

/**
 * SSHandler is used to explain alias.** to real columns in SQL, transient
 * columns not included, the example:
 * 
 * select u.** from users u ==> select u.name as u_name, u.address as u_address
 * from users u
 * 
 * SS means star-star
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class SSHandler extends DefaultOrderSqlHandler {

	@Override
	public void beforeExecute(ImprovedQueryRunner runner, PreparedSQL ps) {
		Object[] tableModels = ps.getModels();
		if (tableModels == null || tableModels.length == 0)
			throw new DbException("TableModel items needed for SSHandler");
		String[] aliases = ps.getAliases();
		if (aliases == null || aliases.length != tableModels.length)
			throw new DbException("Alias qty not same as TableModel qty.");
		String sql = explainNetQuery(ps);
		ps.setSql(sql);
	}

	/**
	 * Replace .** to all fields, replace .## to all PKey and FKey fields only, for
	 * example:
	 * 
	 * <pre>
	 * u.**  ==> u.id as u_id, u.userName as u_userName, u.address as u_address...
	 * u.##  ==> u.id as u_id
	 * </pre>
	 */
	private String explainNetQuery(PreparedSQL ps) {// NOSONAR
		String sql = StrUtils.formatSQL(ps.getSql());
		DbException.assureNotEmpty(ps.getSql(), "Sql can not be empty");
		int pos = sql.indexOf(".**");
		if (pos < 0)
			pos = sql.indexOf(".##");
		while (pos >= 0) {
			StringBuilder aliasSB = new StringBuilder();
			for (int i = pos - 1; i >= 0; i--) {
				if (StrUtils.isNormalLetters(sql.charAt(i)))
					aliasSB.insert(0, sql.charAt(i));
				else
					break;
			}
			if (aliasSB.length() == 0)
				throw new DbException(".** can not put at front");
			String alias = aliasSB.toString();
			sql += " ";// NOSONAR

			// alias found, not find the table name
			int posAlias = StrUtils.indexOfIgnoreCase(sql, " as " + alias + " ");
			if (posAlias == -1)
				posAlias = StrUtils.indexOfIgnoreCase(sql, " as " + alias + ",");
			if (posAlias == -1)
				posAlias = StrUtils.indexOfIgnoreCase(sql, " as " + alias + ")");
			if (posAlias == -1)
				posAlias = StrUtils.indexOfIgnoreCase(sql, " " + alias + " ");
			if (posAlias == -1)
				posAlias = StrUtils.indexOfIgnoreCase(sql, " " + alias + ",");
			if (posAlias == -1)
				posAlias = StrUtils.indexOfIgnoreCase(sql, " " + alias + ")");
			if (posAlias == -1)
				throw new DbException("Alias '" + alias + "' not found");

			StringBuilder tableNameSb = new StringBuilder();
			for (int i = posAlias - 1; i >= 0; i--) {
				char c = sql.charAt(i);
				if (StrUtils.isNormalLetters(c))
					tableNameSb.insert(0, c);
				else if (tableNameSb.length() > 0)
					break;
			}
			if (tableNameSb.length() == 0)
				throw new DbException("Alias '" + alias + "' not found tablename in SQL");
			String tbStr = tableNameSb.toString();
 
			sql = replaceStarStarToColumn(sql, alias, tbStr, ps); 
			pos = sql.indexOf(".**");
			if (pos < 0)
				pos = sql.indexOf(".##");
		} 
		return sql;
	}

	/**
	 * Replace .** to all fields, replace .## to all PKey and Fkey fields only, for
	 * example:
	 * 
	 * <pre>
	 * u.**  ==> u.id as u_id, u.userName as u_userName, u.address as u_address...
	 * u.##  ==> u.id as u_id
	 * </pre>
	 */
	private static String replaceStarStarToColumn(String sql, String alias, String tableName, PreparedSQL ps) {
		String result = sql; 
		if (sql.contains(alias + ".**")) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < ps.getModels().length; i++) {
				TableModel tb = (TableModel) ps.getModels()[i];
				if (tableName.equalsIgnoreCase(tb.getTableName())) {
					if (!alias.equalsIgnoreCase(ps.getAliases()[i]))
						throw new DbException(
								"Alias '" + alias + "' not same as tableModel's alias '" + ps.getAliases()[i] + "'");
					for (ColumnModel col : tb.getColumns()) {
						if (!col.getTransientable())
							sb.append(alias).append(".").append(col.getColumnName()).append(" as ").append(alias)
									.append("_").append(col.getClearQuoteColumnName()).append(", ");
					}
					break;
				}
			}
			if (sb.length() == 0)
				throw new DbException("In SQL '" + sql + "', Can not find columns in table '" + tableName + "'");
			sb.setLength(sb.length() - 2);
			result = StrUtils.replaceFirst(sql, alias + ".**", sb.toString());
			return result;
		}

		if (sql.contains(alias + ".##")) {// Pkey and Fkey only
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < ps.getModels().length; i++) {
				TableModel tb = (TableModel) ps.getModels()[i];
				if (tableName.equalsIgnoreCase(tb.getTableName())) {
					if (!alias.equalsIgnoreCase(ps.getAliases()[i]))
						throw new DbException("Alias '" + alias + "' not same as tableModel's alias");
					for (ColumnModel col : tb.getColumns()) {
						boolean found = false;
						if (!col.getTransientable()) {
							if (col.getPkey())
								found = true;
							else {
								for (FKeyModel tableModel : tb.getFkeyConstraints()) {
									if (tableModel.getColumnNames().contains(col.getColumnName())) {
										found = true;
										break;
									}
								}
							}
						}
						if (found)
							sb.append(alias).append(".").append(col.getColumnName()).append(" as ").append(alias)
									.append("_").append(col.getClearQuoteColumnName()).append(", ");
					}
					break;
				}
			}
			if (sb.length() == 0)
				throw new DbException("In SQL '" + sql + "', Can not find key columns in table '" + tableName + "'");
			sb.setLength(sb.length() - 2);
			result = StrUtils.replaceFirst(result, alias + ".##", sb.toString());
		}
		return result;
	}

}
