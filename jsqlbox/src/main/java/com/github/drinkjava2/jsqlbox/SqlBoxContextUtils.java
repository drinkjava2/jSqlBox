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

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.id.IdentityIdGenerator;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.utils.DialectUtils;

/**
 * @author Yong Zhu
 * @since 1.0.0
 */
public abstract class SqlBoxContextUtils {

	public static TableModel[] metaDataToModels(SqlBoxContext ctx, Dialect dialect) {
		Connection con = null;
		TableModel[] tableModels = null;
		SQLException sqlException = null;
		try {
			con = ctx.prepareConnection();
			tableModels = DialectUtils.db2Models(con, dialect);
		} catch (SQLException e) {
			sqlException = e;
		} finally {
			try {
				ctx.close(con);
			} catch (SQLException e) {
				if (sqlException != null)
					sqlException.setNextException(e);
				else
					sqlException = e;
			}
		}
		if (sqlException != null)
			throw new SqlBoxException(sqlException);
		return tableModels;
	}

	private static ColumnModel findMatchColumnForJavaField(String pojoField, SqlBox box) {
		TableModel beanTableModel = box.getTableModel();
		ColumnModel col = findMatchColumnForJavaField(pojoField, beanTableModel);
		if (col == null)
			col = findMatchColumnForJavaField(pojoField,
					box.getContext().getMetaTableModel(beanTableModel.getTableName()));
		if (col == null)
			throw new SqlBoxException("Can not find database column match entityBean field '" + pojoField + "'");
		return col;
	}

	private static ColumnModel findMatchColumnForJavaField(String pojoField, TableModel tableModel) {
		List<ColumnModel> columns = tableModel.getColumns();
		ColumnModel result = null;
		String underLineFieldName = SqlBoxStrUtils.camelToLowerCaseUnderline(pojoField);
		for (ColumnModel col : columns) {
			if (pojoField.equalsIgnoreCase(col.getPojoField())
					|| underLineFieldName.equalsIgnoreCase(col.getColumnName())) {
				if (result != null)
					throw new SqlBoxException("Field '" + pojoField + "' found duplicated columns definition");
				result = col;
			}
		}
		return result;
	}

	/**
	 * Delete entityBean in database according primary key value
	 */
	public static void delete(Object entityBean, SqlBox box) {
		checkBeanAndBoxExist(entityBean, box);
		TableModel tableModel = box.getTableModel();

		List<Object> parameters = new ArrayList<Object>();
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(tableModel.getTableName()).append(" where ");
		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityBean.getClass());
		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, box);
			if (col.getPkey()) {
				Object value = readValueFromBeanField(entityBean, fieldName);
				sb.append(col.getColumnName()).append("=?, ");
				parameters.add(value);
			}
		}
		sb.setLength(sb.length() - 2);// delete the last "," character
		if (parameters.size() == 0)
			throw new SqlBoxException("No primary key setting for entityBean");
		int rowEffected = box.context.nExecute(sb.toString(), parameters.toArray(new Object[parameters.size()]));
		if (rowEffected <= 0)
			throw new SqlBoxException("No row be deleted for entityBean");
		if (rowEffected > 1)
			throw new SqlBoxException("Multiple rows be deleted for entityBean");
	}

	/**
	 * Insert entityBean into database, and change ID fields to values generated
	 * by IdGenerator (identity or sequence or UUID...)
	 */
	public static void insert(Object entityBean, SqlBox box) {
		checkBeanAndBoxExist(entityBean, box);
		SqlBoxContext ctx = box.getContext();
		TableModel tableModel = box.getTableModel();

		StringBuilder sb = new StringBuilder();
		sb.append("insert into ").append(tableModel.getTableName()).append(" (");

		List<Object> parameters = new ArrayList<Object>();
		String identityColumn = null;
		Type identityType = null;
		Map<String, Method> readMethods = ClassCacheUtils.getClassReadMethods(entityBean.getClass());

		for (String fieldName : readMethods.keySet()) {
			ColumnModel col = findMatchColumnForJavaField(fieldName, box);
			if (!col.getTransientable() && col.getInsertable()) {
				IdGenerator idGen = col.getIdGenerator();
				if (idGen != null) {
					if (idGen.dependOnAutoIdGenerator()) {
						if (identityColumn != null)
							throw new SqlBoxException(
									"More than 1 identity column found for table '" + tableModel.getTableName() + "'");
						identityColumn = fieldName;
					} else {
						sb.append(col.getColumnName()).append(",");
						Object id = idGen.getNextID(ctx, ctx.getDialect(), col.getColumnType());
						parameters.add(id);
						writeValueToBeanField(entityBean, fieldName, id);
					}
				} else {
					Object value = readValueFromBeanField(entityBean, fieldName);
					sb.append(col.getColumnName()).append(", ");
					parameters.add(value);
				}
			}
		}
		sb.setLength(sb.length() - 2);// delete the last ", " character
		sb.append(") values(").append(SqlBoxStrUtils.getQuestionsStr(parameters.size())).append(")");
		ctx.nExecute(sb.toString(), parameters.toArray(new Object[parameters.size()]));
		if (identityColumn != null) {// write identity id to Bean field
			Object identityId = IdentityIdGenerator.INSTANCE.getNextID(ctx, ctx.getDialect(), identityType);
			writeValueToBeanField(entityBean, identityColumn, identityId);
		}
	}

	/** Read value from entityBean field */
	private static Object readValueFromBeanField(Object entityBean, String fieldName) {
		Method readMethod = ClassCacheUtils.getClassFieldReadMethod(entityBean.getClass(), fieldName);
		if (readMethod == null)
			throw new SqlBoxException("Can not find Java bean read method for column '" + fieldName + "'");
		try {
			return readMethod.invoke(entityBean);
		} catch (Exception e) {
			throw new SqlBoxException(e);
		}
	}

	/** write value to entityBean field */
	private static void writeValueToBeanField(Object entityBean, String fieldName, Object value) {
		Method writeMethod = ClassCacheUtils.getClassFieldWriteMethod(entityBean.getClass(), fieldName);
		if (writeMethod == null)
			throw new SqlBoxException("Can not find Java bean read method for column '" + fieldName + "'");
		try {
			writeMethod.invoke(entityBean, value);
		} catch (Exception e) {
			throw new SqlBoxException(e);
		}
	}

	private static void checkBeanAndBoxExist(Object entityBean, SqlBox box) {
		SqlBoxException.assureNotNull(entityBean, "Assert error, entityBean can not be null");
		SqlBoxException.assureNotNull(box, "Assert error, box of entityBean can not be null");
		SqlBoxException.assureNotNull(box.getContext(), "Assert error, box's SqlBoxContext can not be null");
		SqlBoxException.assureNotNull(box.getTableModel(), "Assert error, box's TableModel can not be null");
		SqlBoxException.assureNotEmpty(box.getTableModel().getTableName(),
				"Assert error, box's tableName can not be null");
	}

}