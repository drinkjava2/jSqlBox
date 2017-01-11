/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.drinkjava2.jsqlbox;

import static com.github.drinkjava2.jsqlbox.SqlBoxException.throwEX;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import com.github.drinkjava2.springsrc.ReflectionUtils;

/**
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class SqlBoxUtils {

	// To check if a class exist, if exist, cache it to avoid check again
	private static ConcurrentHashMap<String, Integer> classExistCache = new ConcurrentHashMap<>();

	private static ThreadLocal<HashMap<String, Method>> methodExistCache = new ThreadLocal<HashMap<String, Method>>() {
		@Override
		protected HashMap<String, Method> initialValue() {
			return new HashMap<>();
		}
	};

	private SqlBoxUtils() {
	}

	/**
	 * Return true if a String is null or ""
	 */
	public static boolean isEmptyStr(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * Check class if exist
	 */
	public static Class<?> checkSqlBoxClassExist(String className) {
		Integer i = classExistCache.get(className);
		if (i == null)
			try {
				Class<?> clazz = Class.forName(className);
				if (SqlBox.class.isAssignableFrom((Class<?>) clazz)) {
					classExistCache.put(className, 1);
					return clazz;
				}
				classExistCache.put(className, 0);
				return null;
			} catch (Exception e) {
				SqlBoxException.eatException(e);
				classExistCache.put(className, 0);
				return null;
			}
		if (1 == i) {
			try {
				return Class.forName(className);
			} catch (Exception e) {
				SqlBoxException.eatException(e);
			}
		}
		return null;
	}

	/**
	 * If first letter is Capitalized, return true
	 */
	public static boolean isCapitalizedString(String str) {
		char c = str.substring(0, 1).toCharArray()[0];
		return c >= 'A' && c <= 'Z';
	}

	/**
	 * Change first letter to lower case
	 */
	public static String toFirstLetterLowerCase(String s) {
		if (Character.isLowerCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	/**
	 * Change first letter to upper case
	 */
	public static String toFirstLetterUpperCase(String s) {
		if (Character.isUpperCase(s.charAt(0)))
			return s;
		else
			return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
	}

	/**
	 * Invoke to get field value by its fieldID
	 */
	public static String getStaticStringField(Class<?> beanClass, String fieldID) {
		try {
			Field field = beanClass.getField(fieldID);
			return (String) field.get(null);
		} catch (Exception e) {
			SqlBoxException.eatException(e);
		}
		return null;
	}

	/**
	 * Camel string change to lower case underline string, "AbcDef" to "abc_def"
	 */
	public static String camelToLowerCaseUnderline(String name) {
		StringBuilder sb = new StringBuilder();
		if (name != null && name.length() > 0) {
			sb.append(name.substring(0, 1).toLowerCase());
			for (int i = 1; i < name.length(); i++) {
				String s = name.substring(i, i + 1);
				char c = s.substring(0, 1).toCharArray()[0];
				if (c >= 'A' && c <= 'Z')
					sb.append("_");
				sb.append(s.toLowerCase());
			}
		}
		return sb.toString();
	}

	/**
	 * Make the given field accessible, only called when actually necessary, to avoid unnecessary conflicts with a JVM
	 * SecurityManager (if active).
	 */
	public static void makeAccessible(Field field) {
		if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
				|| Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
			field.setAccessible(true);
		}
	}

	/**
	 * Get JDK random type4 UUID
	 */
	public static String getHex32UUID() {
		return UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}

	/**
	 * Get Field value by it's column definition
	 */
	public static Object getFieldRealValue(Object entityBean, Column col) {
		try {
			Method m = ReflectionUtils.findMethod(entityBean.getClass(), col.getReadMethodName(), new Class[] {});
			return m.invoke(entityBean, new Object[] {});
		} catch (Exception e) {
			return throwEX(e, "SqlBoxUtils getFieldRealValue error, method " + col.getReadMethodName()
					+ " invoke error for entity: " + entityBean);
		}
	}

	/**
	 * Extract EntityID Values from realColumns
	 */
	public static Map<String, Object> extractEntityIDValues(Object entityID, Map<String, Column> realColumns) {
		if (entityID instanceof Map)
			return (Map<String, Object>) entityID;
		Map<String, Object> idvalues = new HashMap<>();
		if (entityID instanceof List) {
			idvalues = new HashMap<>();
			for (Column col : (List<Column>) entityID)
				idvalues.put(col.getFieldID(), col.getPropertyValue());
		} else {
			List<Column> idCols = extractIdColumnsOnly(realColumns);
			if (idCols == null || idCols.size() != 1)
				throwEX("SqlBoxUtils extractEntityIDValues error, id column is not 1, entityID:" + entityID);
			else
				idvalues.put(idCols.get(0).getFieldID(), entityID);
		}
		return idvalues;
	}

	private static List<Column> extractIdColumnsOnly(Map<String, Column> realColumns) {
		List<Column> idColumns = new ArrayList<>();
		for (Entry<String, Column> entry : realColumns.entrySet()) {
			Column col = entry.getValue();
			if (col.getEntityID()) {
				idColumns.add(col);
			}
		}
		if (idColumns.isEmpty())
			throwEX("SqlBoxUtils extractIdColumnsOnly error, no entityID set for class ");
		return idColumns;
	}

	public static Method getDeclaredMethodQuickly(Class<?> targetClass, String methodName, Class<?> parameterclazz) {
		String key = new StringBuilder(targetClass.toString()).append("_").append(methodName).append("_")
				.append(parameterclazz).toString();
		HashMap<String, Method> map = methodExistCache.get();
		if (map.containsKey(key))
			return map.get(key);
		Method method = ReflectionUtils.findMethod(targetClass, methodName, new Class[] { parameterclazz });
		map.put(key, method);
		return method;
	}

	/**
	 * For inside debug use only
	 */
	public static String getSqlRowSetMetadataDebugInfo(SqlRowSetMetaData rsm) {
		StringBuilder sb = new StringBuilder();
		int coll = rsm.getColumnCount();
		for (int i = 0; i < coll; i++) {
			sb.append("==============================").append("\r\n");
			sb.append("getColumnName=" + rsm.getColumnName(i + 1)).append("\r\n");
			sb.append("getColumnClassName=" + rsm.getColumnClassName(i + 1)).append("\r\n");
			sb.append("getColumnType=" + rsm.getColumnType(i + 1)).append("\r\n");
			sb.append("getColumnTypeName=" + rsm.getColumnTypeName(i + 1)).append("\r\n");
			sb.append("getColumnDisplaySize=" + rsm.getColumnDisplaySize(i + 1)).append("\r\n");
			sb.append("getTableName=" + rsm.getTableName(i + 1)).append("\r\n");
			sb.append("getCatalogName=" + rsm.getCatalogName(i + 1)).append("\r\n");
			sb.append("getColumnLabel=" + rsm.getColumnLabel(i + 1)).append("\r\n");
			sb.append("getSchemaName=" + rsm.getSchemaName(i + 1)).append("\r\n");
		}
		return sb.toString();
	}

	/**
	 * For inside debug use only
	 */
	public static String getResultSetMeataDataDebugInfo(ResultSetMetaData rsmd) {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append("===ResultSetMetaData debug info=== ");
			sb.append("getColumnCount:" + rsmd.getColumnCount()).append("\r\n");
			for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {
				sb.append("ColumnName:" + rsmd.getColumnName(i)).append("\t");
				sb.append("ColumnClassName:" + rsmd.getColumnClassName(i)).append("\t");
				sb.append("ColumnDisplaySize:" + rsmd.getColumnDisplaySize(i)).append("\t");
				sb.append("ColumnLabel:" + rsmd.getColumnLabel(i)).append("\t");
				sb.append("Scale:" + rsmd.getScale(i)).append("\t");
				sb.append("ColumnType:" + rsmd.getColumnType(i)).append("\t");
				sb.append("ColumnTypeName:" + rsmd.getColumnTypeName(i)).append("\t");
				sb.append("Precision:" + rsmd.getPrecision(i)).append("\t");
				sb.append("SchemaNam:" + rsmd.getSchemaName(i)).append("\t");
				sb.append("CatalogName:" + rsmd.getCatalogName(i)).append("\t");
				sb.append("TableName:" + rsmd.getTableName(i)).append("\t");
				sb.append("isAutoIncrement:" + rsmd.isAutoIncrement(i)).append("\t");
				sb.append("isCurrency:" + rsmd.isCurrency(i)).append("\t");
				sb.append("isNullable:" + rsmd.isNullable(i)).append("\t");
				sb.append("isReadOnly:" + rsmd.isReadOnly(i)).append("\t");
				sb.append("isSearchable:" + rsmd.isSearchable(i)).append("\r\n");
			}
		} catch (SQLException e) {
			SqlBoxException.throwEX(e, "getResultSetMeataDataDebugInfo error.");
		}
		return sb.toString();
	}

}
