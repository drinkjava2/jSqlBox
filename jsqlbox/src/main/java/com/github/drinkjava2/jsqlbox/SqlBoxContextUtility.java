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

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.id.IdGenerator;

/**
 * @author Yong Zhu
 * @since 1.0.0
 */
public abstract class SqlBoxContextUtility {

	private static ColumnModel findMatchColumnForJavaField(String pojoField, List<ColumnModel> columns) {
		ColumnModel result = null;
		String underLineFieldName = SqlBoxStringUtils.camelToLowerCaseUnderline(pojoField);
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

	private static IdGenerator buildIdGenerator(ColumnModel columnModel) {
		return null;
	}

	/**
	 * Insert entityBean into database
	 */
	public static void insert(Object entityBean, SqlBox box) {
		SqlBoxException.assureNotNull(entityBean, "Insert error, entityBean can not be null");
		SqlBoxException.assureNotNull(box, "Insert error, SqlBox can not be null");
		SqlBoxContext ctx = box.getContext();
		SqlBoxException.assureNotNull(ctx, "Insert error, SqlBoxContext can not be null");
		TableModel tableModel = box.getTableModel();
		SqlBoxException.assureNotNull(tableModel, "Insert error, TableModel can not be null");
		String tableName = tableModel.getTableName();
		SqlBoxException.assureNotEmpty(tableName, "Insert error, tableName can not be null");

		ArrayList<ColumnModel> columnsToInsert = new ArrayList<ColumnModel>();
		BeanInfo beanInfo = null;
		PropertyDescriptor[] pds = null;
		try {
			beanInfo = Introspector.getBeanInfo(entityBean.getClass());
			pds = beanInfo.getPropertyDescriptors();
		} catch (Exception e) {
			throw new SqlBoxException("Can not get bean property descriptors", e);
		}

		for (PropertyDescriptor pd : pds) {
			String pojoField = pd.getName();
			ColumnModel matchColumn = findMatchColumnForJavaField(pojoField, tableModel.getColumns());
			if (matchColumn != null) {

				Method method = pd.getReadMethod();
				try {
					Object value = method.invoke(entityBean);
					matchColumn.setValue(value);
				} catch (Exception e) {
					throw new SqlBoxException(e);
				}
				columnsToInsert.add(matchColumn);
			}
		}

		StringBuilder sb = new StringBuilder();
		List<Object> parameters = new ArrayList<Object>();

		for (ColumnModel col : columnsToInsert) {
			IdGenerator idGen = buildIdGenerator(col);
			if (idGen != null) {
				parameters.add(idGen.getNextID(ctx));
			} else {
				parameters.add(col.getValue());
			}
		}
		sb.append("insert into ").append(tableName).append(" values(")
				.append(SqlBoxStringUtils.getQuestionsStr(parameters.size())).append(")");

		ctx.nExecute(sb.toString(), parameters.toArray(new Object[parameters.size()]));
		// if success, set id values to bean
	}

}