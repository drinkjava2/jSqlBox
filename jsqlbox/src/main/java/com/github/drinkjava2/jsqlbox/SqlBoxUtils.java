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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

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
	 * In entity class, a legal fieldID like userName must have a same name no parameter method like userName()
	 */
	public static boolean isLegalFieldID(String fieldID, Class<?> clazz) {
		if ("class".equals(fieldID))
			return false;
		if (SqlBoxUtils.isEmptyStr(fieldID))
			return false;
		if (SqlBoxUtils.isCapitalizedString(fieldID))
			return false;
		if (!SqlBoxUtils.isBaseDataType(clazz))
			return false;
		return true;
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
	 * Get Field value by it's fieldID
	 */
	public static Object getFieldValueByFieldID(Entity entityBean, String fieldID) {
		String getMethod = "get" + SqlBoxUtils.toFirstLetterUpperCase(fieldID);
		try {
			Method m = ReflectionUtils.findMethod(entityBean.getClass(), getMethod, new Class[] {});
			return m.invoke(entityBean, new Object[] {});
		} catch (Exception e) {
			return throwEX(e, "SqlBoxUtils getPropertyValueByFieldID error,   method " + getMethod + " not found in "
					+ entityBean.getClass());
		}
	}

	/**
	 * Set Field value by it's fieldID
	 */
	public static void setFieldValueByFieldID(Entity entityBean, String fieldID, Object value) {
		String setMethod = "set" + SqlBoxUtils.toFirstLetterUpperCase(fieldID);
		try {
			Method m = ReflectionUtils.findMethod(entityBean.getClass(), setMethod, new Class[] { value.getClass() });
			m.invoke(entityBean, new Object[] { value });
		} catch (Exception e) {
			throwEX(e, "SqlBoxUtils getPropertyValueByFieldID error,   method " + setMethod + " not found in "
					+ entityBean.getClass());
		}
	}

	/**
	 * add Field value by it's fieldID, field is a Set type field, value will be added into this field
	 */
	public static void addFieldValueByFieldID(Entity entityBean, String fieldID, Object value) {
		Set<Object> property = null;
		String getMethod = "get" + SqlBoxUtils.toFirstLetterUpperCase(fieldID);
		try {
			Method m = ReflectionUtils.findMethod(entityBean.getClass(), getMethod, new Class[] {});
			property = (Set<Object>) m.invoke(entityBean, new Object[] {});
		} catch (Exception e) {
			throwEX(e, "SqlBoxUtils getPropertyValueByFieldID error,   method \"" + getMethod + "\" not found in "
					+ entityBean.getClass());
		}
		if (property == null)
			property = new LinkedHashSet<>();
		property.add(value);
		String setMethod = "set" + SqlBoxUtils.toFirstLetterUpperCase(fieldID);
		try {
			Method m = ReflectionUtils.findMethod(entityBean.getClass(), setMethod, new Class[] { Set.class });
			m.invoke(entityBean, new Object[] { property });
		} catch (Exception e) {
			throwEX(e, "SqlBoxUtils getPropertyValueByFieldID error,   method \"" + setMethod + "\" not found in "
					+ entityBean.getClass());
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

	/**
	 * Not used but keep here for future use, I think I many forgot this
	 */
	public static class ObjectResultSetExtractor<T> implements ResultSetExtractor<List<T>> {
		@Override
		public List<T> extractData(ResultSet rs) throws SQLException {
			List<T> results = new ArrayList<>();
			while (rs.next()) {
				rs.getMetaData();
			}
			return results;
		}
	}

	/**
	 * Check if a class is a basic Java data type
	 */
	public static boolean isBaseDataType(Class<?> clazz) {// NOSONAR
		if (clazz == null)
			return false;
		return (clazz.equals(String.class) || clazz.equals(Integer.class) || clazz.equals(Byte.class)// NOSONAR
				|| clazz.equals(Long.class) || clazz.equals(Double.class) || clazz.equals(Float.class)
				|| clazz.equals(Character.class) || clazz.equals(Short.class) || clazz.equals(BigDecimal.class)
				|| clazz.equals(BigInteger.class) || clazz.equals(Boolean.class) || clazz.equals(Date.class)
				|| clazz.isPrimitive());
	}

	/**
	 * Find an entity from entityMap by entityID
	 */
	public static Entity findEntityByID(Map<String, Object> id, Map<Object, Entity> entityMap) {
		if (id == null || id.isEmpty() || entityMap.isEmpty())
			return null;
		if (id.size() == 1)
			return entityMap.get(id.values().iterator().next());

		Object[] key = id.keySet().toArray();
		Arrays.sort(key);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < key.length; i++) {
			sb.append(key[i]).append("=").append(id.get(key[i])).append(",");
		}
		return entityMap.get(sb.toString());
	}

	/**
	 * Cache an entity to entityMap, use entityID as key
	 */
	public static void cacheEntityToEntityMap(Entity entity, Map<Object, Entity> entityMap) {
		if (entity == null)
			return;
		Map<String, Object> id = entity.box().getEntityID();
		if (id.size() == 1)
			entityMap.put(id.values().iterator().next(), entity);
		else {

			Object[] key = id.keySet().toArray();
			Arrays.sort(key);

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < key.length; i++) {
				sb.append(key[i]).append("=").append(id.get(key[i])).append(",");
			}
			entityMap.put(sb.toString(), entity);
		}
	}

	// Fetch values from one line of result List
	protected static SqlBox fetchValueFromList(String alias, Map<String, Object> oneLine, Entity entity) {
		SqlBox box = entity.box();
		box.configAlias(alias);
		Map<String, Column> realColumns = box.buildRealColumns();
		for (Column col : realColumns.values()) {
			String aiasColUppserCaseName = entity.aliasByFieldID(col.getFieldID()).toUpperCase();
			if (oneLine.containsKey(aiasColUppserCaseName))
				box.setFieldRealValue(col, oneLine.get(aiasColUppserCaseName));
		}
		return box;
	}

	/**
	 * Usually used for get the last element from a linkedHashset
	 */
	public static <E> E getLastElement(Collection<E> c) {
		E last = null;
		for (E e : c)
			last = e;
		return last;
	}

	public static String formatSQL(String sql) {
		String fSql = "\r\n" + sql;
		fSql = StringUtils.replace(fSql, ",", ",\r\n\t");
		fSql = StringUtils.replace(fSql, " select ", "\r\nselect \r\n\t");
		fSql = StringUtils.replace(fSql, " from ", "\r\nfrom \r\n\t");
		fSql = StringUtils.replace(fSql, " where ", "\r\nwhere \r\n\t");
		fSql = StringUtils.replace(fSql, " delete ", "\r\ndelete \r\n\t");
		fSql = StringUtils.replace(fSql, " update ", "\r\nupdate \r\n\t");
		fSql = StringUtils.replace(fSql, " left ", "\r\nleft ");
		fSql = StringUtils.replace(fSql, " right ", "\r\nright ");
		fSql = StringUtils.replace(fSql, " inner ", "\r\ninner ");
		fSql = StringUtils.replace(fSql, " join ", " join \r\n\t");
		fSql = StringUtils.replace(fSql, " on ", "\r\n   on  ");
		fSql = StringUtils.replace(fSql, " group ", "\r\ngroup \r\n\t");
		fSql = StringUtils.replace(fSql, " order ", "\r\norder \r\n\t");
		return fSql;
	}
}
