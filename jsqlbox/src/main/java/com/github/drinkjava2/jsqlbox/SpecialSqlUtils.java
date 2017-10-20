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
import java.util.Map;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * SpecialSqlUtils is utility class store static methods to explain special SQL
 * which has special method like pagin(), net() .....
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class SpecialSqlUtils {

	protected static ThreadLocal<Object[]> netObjectConfigCache = new ThreadLocal<Object[]>();
	protected static ThreadLocal<SqlBox[]> netSqlBoxCache = new ThreadLocal<SqlBox[]>();
	protected static ThreadLocal<int[]> paginationCache = new ThreadLocal<int[]>();

	/**
	 * Store SqlBox array binded to ListMaps
	 */
	protected static ThreadLocal<Map<Object, SqlBox[]>> netBoxConfigBindedToObject = new ThreadLocal<Map<Object, SqlBox[]>>() {
		@Override
		protected Map<Object, SqlBox[]> initialValue() {
			return new HashMap<Object, SqlBox[]>();
		}
	};

	/**
	 * Explain special SQL to standard SQL, special SQL utilize ThreadLocal
	 * variants to implement some special method like pagin(), net()...
	 */
	public static String explainSpecialSql(SqlBoxContext ctx, String... SQLs) {
		StringBuilder sb = new StringBuilder();
		for (String str : SQLs)
			sb.append(str);
		String resultSql = sb.toString();
		int[] pagins = paginationCache.get();
		resultSql = SpecialSqlUtils.explainNetConfig(ctx, resultSql);
		if (pagins != null)
			resultSql = ctx.getDialect().paginate(pagins[0], pagins[1], resultSql);
		return resultSql;
	}

	protected static void cleanLastNetBoxCache() {
		SpecialSqlUtils.netSqlBoxCache.set(null);
	}

	/** Clean ThreadLocal variants which should only used for 1 SQL each time */
	protected static void cleanThreadLocalShouldOnlyUseOneTime() {
		SpecialSqlUtils.netSqlBoxCache.set(null);
		SpecialSqlUtils.netObjectConfigCache.set(null);
		paginationCache.set(null);
	}

	protected static <T> void bindSqlBoxConfigToObject(T t) {
		SqlBox[] boxes = SpecialSqlUtils.netSqlBoxCache.get();
		if (boxes != null && boxes.length > 0)
			SpecialSqlUtils.netBoxConfigBindedToObject.get().put(t, boxes);
	}

	/**
	 * Transfer Object[] to SqlBox[], object can be:
	 * 
	 * <pre>
	 * 1. if is SqlBox instance, directly add into array,
	 * 2. if is a Class, will call SqlBoxUtils.createSqlBox() to create a SqlBox instance,
	 * 3. else will call SqlBoxUtils.findAndBindSqlBox() to create a SqlBox instance
	 * </pre>
	 */
	public static SqlBox[] netConfigsToSqlBoxes(SqlBoxContext ctx, Object[] netConfigs) {
		if (netConfigs == null || netConfigs.length == 0)
			return null;
		SqlBox[] result = new SqlBox[netConfigs.length];
		for (int i = 0; i < result.length; i++) {
			Object obj = netConfigs[i];
			if (obj == null)
				throw new SqlBoxException("Can not convert null to SqlBox instance");
			if (obj instanceof SqlBox)
				result[i] = (SqlBox) obj;
			else if (obj instanceof Class)
				result[i] = SqlBoxUtils.createSqlBox(ctx, (Class<?>) obj);
			else {
				result[i] = SqlBoxUtils.findAndBindSqlBox(ctx, obj);
			}
		}
		return result;
	}

	/**
	 * Replace u.** to u.userName as u_userName, u.address as u_address...
	 * 
	 * <pre>
	 * netConfig:
	 * null: use MetaTableModels, select all columns and db table name should  same as given table name
	 * not null: change the Object[] to a Sqlbox[], according the SqlBox[]'s setting to select columns which not transient
	 * 
	 * </pre>
	 */
	private static String replaceStarStarToColumn(SqlBoxContext ctx, String sql, String alias, String tableName,
			SqlBox[] netConfig) {
		StringBuilder replace = new StringBuilder();
		if (netConfig != null && netConfig.length > 0) {
			for (SqlBox box : netConfig) {
				TableModel tb = box.getTableModel();
				if (tableName.equalsIgnoreCase(tb.getTableName())) {
					for (ColumnModel col : tb.getColumns()) {
						if (!col.getTransientable())
							replace.append(alias).append(".").append(col.getColumnName()).append(" as ").append(alias)
									.append("_").append(col.getColumnName()).append(", ");
					}
					break;
				}
			}
		}
		if (replace.length() == 0)
			throw new SqlBoxException("Can not find columns in table +'" + tableName + "'");
		replace.setLength(replace.length() - 2);
		return StrUtils.replace(sql, alias + ".**", replace.toString());
	}

	/**
	 * Explain SQL include "**", for example: <br/>
	 * "select u.** from user u" to <br/>
	 * "select u.userName as u_userName, u.address as u_address from user u"
	 */
	public static String explainNetConfig(SqlBoxContext ctx, String thesql) {
		String sql = thesql;
		int pos = sql.indexOf(".**");
		if (pos < 0)
			return sql;
		SqlBox[] configBoxes = netConfigsToSqlBoxes(ctx, netObjectConfigCache.get());
		// Cache the SqlBox[] for EntityNet use
		if (configBoxes != null && configBoxes.length > 0)
			SpecialSqlUtils.netSqlBoxCache.set(configBoxes);
		else
			configBoxes = ctx.getDbMetaBoxes();

		while (pos > -1) {
			StringBuilder alias_ = new StringBuilder();
			for (int i = pos - 1; i >= 0; i--) {
				if (SqlBoxStrUtils.isNormalLetters(sql.charAt(i)))
					alias_.insert(0, sql.charAt(i));
				else
					break;
			}
			if (alias_.length() == 0)
				throw new SqlBoxException(".** can not put at front");
			String alias = alias_.toString();

			int posAlias = (sql + " ").indexOf(" " + alias + " ");
			if (posAlias == -1)
				posAlias = (sql).indexOf(" " + alias + ",");
			if (posAlias == -1)
				posAlias = (sql).indexOf(" " + alias + ")");
			if (posAlias == -1)
				posAlias = (sql).indexOf(" " + alias + "\t");
			if (posAlias == -1)
				throw new SqlBoxException("Alias '" + alias + "' not found");

			StringBuilder tbStr_ = new StringBuilder();
			for (int i = posAlias - 1; i >= 0; i--) {
				char c = sql.charAt(i);
				if (SqlBoxStrUtils.isNormalLetters(c))
					tbStr_.insert(0, c);
				else if (tbStr_.length() > 0)
					break;
			}
			if (tbStr_.length() == 0)
				throw new SqlBoxException("Alias '" + alias + "' not found tablename in SQL");
			String tbStr = tbStr_.toString();

			// now alias="u", tbStr="users"
			sql = replaceStarStarToColumn(ctx, sql, alias, tbStr, configBoxes);
			pos = sql.indexOf(".**");
		}
		return sql;
	}

}
