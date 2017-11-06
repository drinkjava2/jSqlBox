/*
 * Copyright (C) 2016 Yong Zhu.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdbpro.improve.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.improve.SqlExplainSupport;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * EntityNetSqlExplainer is the SqlExplainer to explain net() method to help
 * build a EntityNet
 * 
 * "SqlExplainSupport" interface is defined in jDbPro project, an explainer
 * works like a intercepter, it has few callback methods to deal with SQL and
 * query result
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class EntityNetSqlExplainer implements SqlExplainSupport {
	private Object[] netConfigObjects;
	private TableModel[] generatedTableModels;
	public static final ThreadLocal<Map<Object, Object>> netConfigBindToListCache = new ThreadLocal<Map<Object, Object>>() {
		@Override
		protected Map<Object, Object> initialValue() {
			return new HashMap<Object, Object>();
		}
	};

	public EntityNetSqlExplainer(Object... netConfigObjects) {
		this.netConfigObjects = netConfigObjects;
	}

	@Override
	public String explainSql(ImprovedQueryRunner query, String sql, int paramType, Object paramOrParams) {
		return explainNetQuery((SqlBoxContext) query, sql);
	}

	@Override
	public Object explainResult(Object result) {
		if (result != null && result instanceof List<?>) {
			if (generatedTableModels == null)
				throw new SqlBoxException("Can not bind null generatedTableModels to list result");
			EntityNetSqlExplainer.bindTableModel(result, generatedTableModels);
		}
		return result;
	}

	/**
	 * Transfer Object[] to TableModel[], object can be SqlBox instance, entityClass
	 * or entity Bean
	 * 
	 * <pre>
	 * 1. TableModel instance, will use it
	 * 2. SqlBox instance, will use its tableModel
	 * 3. Class, will call ctx.createSqlBox() to create a SqlBox instance and use its tableModel
	 * 4. Object, will call SqlBoxUtils.findAndBindSqlBox() to create a SqlBox instance
	 * </pre>
	 */
	public static TableModel[] objectConfigsToModels(SqlBoxContext ctx, Object[] netConfigs) {
		if (netConfigs == null || netConfigs.length == 0)
			return new TableModel[0];
		TableModel[] result = new TableModel[netConfigs.length];
		for (int i = 0; i < netConfigs.length; i++) {
			Object obj = netConfigs[i];
			if (obj == null)
				throw new SqlBoxException("Can not convert null to SqlBox instance");
			if (obj instanceof TableModel)
				result[i] = (TableModel) obj;
			else if (obj instanceof SqlBox)
				result[i] = ((SqlBox) obj).getTableModel();
			else if (obj instanceof Class)
				result[i] = ctx.box((Class<?>) obj).getTableModel();
			else {
				result[i] = SqlBoxUtils.findAndBindSqlBox(ctx, obj).getTableModel();
			}
		}
		return result;
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
	 * Replace .** to all fields, replace .## to all PKey and FKey fields only, for
	 * example:
	 * 
	 * <pre>
	 * u.**  ==> u.id as u_id, u.userName as u_userName, u.address as u_address...
	 * u.##  ==> u.id as u_id
	 * </pre>
	 */
	public String explainNetQuery(SqlBoxContext ctx, String thesql) {
		String sql = thesql;
		TableModel[] configModels = objectConfigsToModels(ctx, netConfigObjects);
		// if no netConfigObjects found, use database's meta data
		if (configModels == null || configModels.length == 0)
			configModels = ctx.getDbMetaTableModels();
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

			int posAlias = (sql + " ").indexOf(" as " + alias + " ");
			if (posAlias == -1)
				posAlias = (sql).indexOf(" as " + alias + ",");
			if (posAlias == -1)
				posAlias = (sql).indexOf(" as " + alias + ")");
			if (posAlias == -1)
				posAlias = (sql).indexOf(" as " + alias + "\t");
			if (posAlias == -1)
				posAlias = (sql + " ").indexOf(" " + alias + " ");
			if (posAlias == -1)
				posAlias = (sql).indexOf(" " + alias + ",");
			if (posAlias == -1)
				posAlias = (sql).indexOf(" " + alias + ")");
			if (posAlias == -1)
				posAlias = (sql).indexOf(" " + alias + "\t");
			if (posAlias == -1)
				throw new SqlBoxException("Alias '" + alias + "' not found");

			StringBuilder tableSB = new StringBuilder();
			for (int i = posAlias - 1; i >= 0; i--) {
				char c = sql.charAt(i);
				if (SqlBoxStrUtils.isNormalLetters(c))
					tableSB.insert(0, c);
				else if (tableSB.length() > 0)
					break;
			}
			if (tableSB.length() == 0)
				throw new SqlBoxException("Alias '" + alias + "' not found tablename in SQL");
			String tbStr = tableSB.toString();

			// now alias="u", tbStr="users"
			sql = replaceStarStarToColumn(sql, alias, tbStr, configModels);
			pos = sql.indexOf(".**");
			if (pos < 0)
				pos = sql.indexOf(".##");
		}
		generatedTableModels = configModels;
		return sql;
	}

	public static void removeBindedTableModel(List<?> listMap) {
		netConfigBindToListCache.get().remove(listMap);
	}

	public static TableModel[] getBindedTableModel(List<?> listMap) {
		return (TableModel[]) netConfigBindToListCache.get().get(listMap);
	}

	public static void bindTableModel(Object listMap, TableModel[] tableModels) {
		netConfigBindToListCache.get().put(listMap, tableModels);
	}

}
