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

import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jdbpro.handler.AroundSqlHandler;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxException;
import com.github.drinkjava2.jsqlbox.SqlBoxStrUtils;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNetUtils;

/**
 * EntitySqlMapListHandler is used to explain alias.** to real columns in SQL,
 * example:
 * 
 * select u.** from users u ==> select u.name, u.address, u.age from users u
 * 
 * And bind the netConfigObjects to result Map List
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class EntitySqlMapListHandler extends MapListHandler implements AroundSqlHandler {
	protected Object[] netConfigObjects;
	protected TableModel[] generatedTableModels;

	public EntitySqlMapListHandler(Object... netConfigObjects) {
		this.netConfigObjects = netConfigObjects;
	}

	@Override
	public String handleSql(QueryRunner query, String sql, Object... params) {
		return explainNetQuery((SqlBoxContext) query, sql);
	}

	@Override
	public Object handleResult(QueryRunner query, Object result) {
		if (result != null && result instanceof List<?>) {
			if (generatedTableModels == null)
				throw new SqlBoxException("Can not bind null generatedTableModels to list result");
			EntityNetUtils.bindTableModel(result, generatedTableModels);
		}
		return result;
	}

	/**
	 * Replace .** to all fields, replace .## to all PKey and Fkey fields only,
	 * for example:
	 * 
	 * <pre>
	 * u.**  ==> u.id as u_id, u.userName as u_userName, u.address as u_address...
	 * u.##  ==> u.id as u_id
	 * </pre>
	 */
	private static String replaceStarStarToColumn(String sql, String alias, String tableName, TableModel[] models) {
		String result = sql;
		if (sql.contains(alias + ".**")) {
			StringBuilder sb = new StringBuilder();
			if (models != null && models.length > 0) {
				for (TableModel tb : models) {
					if (tableName.equalsIgnoreCase(tb.getTableName())) {
						if (StrUtils.isEmpty(tb.getAlias()))
							tb.setAlias(alias);
						else {
							if (!alias.equalsIgnoreCase(tb.getAlias()))
								throw new SqlBoxException("Alias '" + alias + "' not same as tableModel's alias");
						}
						for (ColumnModel col : tb.getColumns()) {
							if (!col.getTransientable())
								sb.append(alias).append(".").append(col.getColumnName()).append(" as ").append(alias)
										.append("_").append(col.getColumnName()).append(", ");
						}
						break;
					}
				}
			}
			if (sb.length() == 0)
				throw new SqlBoxException("In SQL '" + sql + "', Can not find columns in table '" + tableName + "'");
			sb.setLength(sb.length() - 2);
			result = StrUtils.replaceFirst(sql, alias + ".**", sb.toString());
			return result;
		}

		if (sql.contains(alias + ".##")) {// Pkey and Fkey only
			StringBuilder sb = new StringBuilder();
			if (models != null && models.length > 0) {
				for (TableModel tb : models) {
					if (tableName.equalsIgnoreCase(tb.getTableName())) {
						if (StrUtils.isEmpty(tb.getAlias()))
							tb.setAlias(alias);
						else {
							if (!alias.equalsIgnoreCase(tb.getAlias()))
								throw new SqlBoxException("Alias '" + alias + "' not same as tableModel's alias");
						}
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
										.append("_").append(col.getColumnName()).append(", ");
						}
						break;
					}
				}
			}
			if (sb.length() == 0)
				throw new SqlBoxException(
						"In SQL '" + sql + "', Can not find key columns in table '" + tableName + "'");
			sb.setLength(sb.length() - 2);
			result = StrUtils.replaceFirst(result, alias + ".##", sb.toString());
		}
		return result;
	}

	/**
	 * Replace .** to all fields, replace .## to all PKey and FKey fields only,
	 * for example:
	 * 
	 * <pre>
	 * u.**  ==> u.id as u_id, u.userName as u_userName, u.address as u_address...
	 * u.##  ==> u.id as u_id
	 * </pre>
	 */
	public String explainNetQuery(SqlBoxContext ctx, String sqlString) {// NOSONAR
		SqlBoxException.assureNotEmpty(sqlString, "Sql can not be empty");
		String sql = SqlBoxStrUtils.formatSQL(sqlString);
		TableModel[] configModels = EntityNetUtils.objectConfigsToModels(ctx, netConfigObjects);
		int pos = sql.indexOf(".**");
		if (pos < 0)
			pos = sql.indexOf(".##");
		while (pos >= 0) {
			StringBuilder aliasSB = new StringBuilder();
			for (int i = pos - 1; i >= 0; i--) {
				if (SqlBoxStrUtils.isNormalLetters(sql.charAt(i)))
					aliasSB.insert(0, sql.charAt(i));
				else
					break;
			}
			if (aliasSB.length() == 0)
				throw new SqlBoxException(".** can not put at front");
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
				throw new SqlBoxException("Alias '" + alias + "' not found");

			StringBuilder tableNameSb = new StringBuilder();
			for (int i = posAlias - 1; i >= 0; i--) {
				char c = sql.charAt(i);
				if (SqlBoxStrUtils.isNormalLetters(c))
					tableNameSb.insert(0, c);
				else if (tableNameSb.length() > 0)
					break;
			}
			if (tableNameSb.length() == 0)
				throw new SqlBoxException("Alias '" + alias + "' not found tablename in SQL");
			String tbStr = tableNameSb.toString();

			sql = replaceStarStarToColumn(sql, alias, tbStr, configModels);
			pos = sql.indexOf(".**");
			if (pos < 0)
				pos = sql.indexOf(".##");
		}
		generatedTableModels = configModels;
		return sql;
	}

}
