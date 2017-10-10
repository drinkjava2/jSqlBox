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

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.id.IdGenerator;
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

	private static ColumnModel findMatchColumnForJavaField(String pojoField, List<ColumnModel> columns) {
		ColumnModel result = null;
		String underLineFieldName = SqlBoxStrUtils.camelToLowerCaseUnderline(pojoField);
		for (ColumnModel columnModel : columns) {
			String colName = columnModel.getColumnName();
			if (colName.equalsIgnoreCase(pojoField) || colName.equalsIgnoreCase(underLineFieldName)) {
				if (result != null)
					throw new SqlBoxException("Field '" + pojoField + "' found duplicated columns definition");
				result = columnModel;
			}
		}
		if (result != null && result.getTransientable())
			return null;
		return result;
	}

	/**
	 * Insert entityBean into database
	 */
	public static void insert(Object entityBean, SqlBox box) {
		checkBeanAndBoxExist(entityBean, box);
		SqlBoxContext ctx = box.getContext();
		TableModel tableModel = box.getTableModel();

		StringBuilder sb = new StringBuilder();
		sb.append("insert into ").append(tableModel.getTableName()).append("(");

		List<Object> parameters = new ArrayList<Object>();
		ColumnModel identityColumn = null;
		for (ColumnModel col : tableModel.getColumns()) {
			if (!col.getTransientable()) {
				IdGenerator idGen = col.getIdGenerator();
				if (idGen != null) {
					if (idGen.dependOnAutoIdGenerator()) {
						if (identityColumn != null)
							throw new SqlBoxException(
									"More than 1 identity column found for table '" + tableModel.getTableName() + "'");
						identityColumn = col;
					} else {
						sb.append("?,");
						Object id = idGen.getNextID(ctx, ctx.getDialect(), col.getColumnType());
						parameters.add(id);
						writeValueToBeanField(entityBean, col, id);
					}
				} else {
					Object value = readValueFromBeanField(entityBean, col);
					sb.append("?,");
					parameters.add(value);
				}
			}
		}
		sb.setLength(sb.length() - 1);// delete the last "," character
		sb.append(") values(").append(SqlBoxStrUtils.getQuestionsStr(parameters.size())).append(")");
		ctx.nExecute(sb.toString(), parameters.toArray(new Object[parameters.size()]));
		if (identityColumn != null) {// write identity id to Bean field
			Object identityId = identityColumn.getIdGenerator().getNextID(ctx, ctx.getDialect(),
					identityColumn.getColumnType());
			writeValueToBeanField(entityBean, identityColumn, identityId);
		}
	}

	/** Read value from entityBean field */
	private static Object readValueFromBeanField(Object entityBean, ColumnModel column) {
		Method readMethod = column.getPojoReadMethod();
		try {
			return readMethod.invoke(entityBean);
		} catch (Exception e) {
			throw new SqlBoxException(e);
		}
	}

	/** write value to entityBean field */
	private static void writeValueToBeanField(Object entityBean, ColumnModel column, Object value) {
		try {
			column.getPojoWriteMethod().invoke(entityBean, value);
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

	public static void delete(Object entityBean, SqlBox box) {
		checkBeanAndBoxExist(entityBean, box);
	}

}