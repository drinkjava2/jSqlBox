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
package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdbpro.CustomizedSqlItem;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContextUtils;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * This SqlBoxException used to wrap exception to a Runtime type Exception
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class Sample implements CustomizedSqlItem {
	Object entityBean;
	TableModel model = null;
	List<Object> sqlItems = new ArrayList<Object>();

	public Sample(Object entityBean) {
		this.entityBean = entityBean;
	}

	public Sample(Object entityBean, TableModel model) {
		this.entityBean = entityBean;
		this.model = model;
	}

	/**
	 * For all not null fields, create field1=? and field2=?... sql pieces
	 */
	public Sample notNullFields() {
		sqlItems.add("#NOTNULL_Fields");
		return this;
	}

	/**
	 * For all fields has null values, create field1 is null and field2 is null...
	 * sql pieces
	 */
	public Sample nullFields() {
		sqlItems.add("#NULL_Fields");
		return this;
	}

	/**
	 * For all fields, create field1=? and field2=? and field3 is null... sql pieces
	 */
	public Sample allFields() {
		sqlItems.add("#ALL_Fields");
		return this;
	}

	/** Add a sql piece */
	public Sample sql(String... sqlPiece) {
		for (String sql : sqlPiece)
			sqlItems.add(sql);
		return this;
	}

	/** Add a Sql param */
	public Sample param(Object... params) {
		for (Object param : params)
			sqlItems.add(new Object[] { param });
		return this;
	}

	private void dealAllFields(PreparedSQL ps, TableModel model, Boolean nullFieldsOnly) {
		boolean isfirst = true;
		Map<String, Method> writeMethods = ClassCacheUtils.getClassWriteMethods(entityBean.getClass());
		for (String fieldName : writeMethods.keySet()) {
			ColumnModel col = SqlBoxContextUtils.findMatchColumnForJavaField(fieldName, model);
			if (col.getTransientable())
				continue;
			Object fieldValue = ClassCacheUtils.readValueFromBeanField(entityBean, fieldName);
			if (fieldValue != null && (nullFieldsOnly == null || !nullFieldsOnly)) {
				if (!isfirst)
					ps.addSql(" and ");
				ps.addSql(col.getColumnName()).append(" =? ");
				ps.addParam(fieldValue);
				isfirst = false;
			} else if (nullFieldsOnly == null || nullFieldsOnly) {
				if (!isfirst)
					ps.addSql(" and ");
				ps.addSql(col.getColumnName()).append(" is null ");
				isfirst = false;
			}
		}
		if (isfirst) // not found any fields
			ps.addSql(" 1=1 ");
	}

	@Override
	public void doPrepare(PreparedSQL ps) {
		if (model == null)
			model = SqlBoxContextUtils.findEntityOrClassTableModel(entityBean);
		SqlBoxException.assureNotNull(model.getEntityClass());
		if (!sqlItems.isEmpty())
			for (Object piece : sqlItems) { // NOSONAR
				if ("#NOTNULL_Fields".equals(piece)) {
					dealAllFields(ps, model, false);
					continue;
				} else if ("#NULL_Fields".equals(piece)) {
					dealAllFields(ps, model, true);
					continue;
				} else if ("#ALL_Fields".equals(piece)) {
					dealAllFields(ps, model, null);
					continue;
				}
				if (piece instanceof String) {
					ps.addSql(piece);
				} else {
					Object param = ((Object[]) piece)[0];
					Object realParam = param;
					if (param instanceof String) { // change ":fieldName" to param("fieldValue")
						String s = (String) param;
						int colon = s.indexOf(':');
						if (colon >= 0) {
							StringBuilder fieldNameSb = new StringBuilder();
							for (int i = colon + 1; i < s.length(); i++) {
								if (StrUtils.isNormalLetters(s.charAt(i)))
									fieldNameSb.append(s.charAt(i));
								else
									break;
							}
							String fieldName = fieldNameSb.toString();
							SqlBoxException.assureNotEmpty(fieldName);
							String fieldValue = String
									.valueOf(ClassCacheUtils.readValueFromBeanField(entityBean, fieldName));
							realParam = StrUtils.replaceFirst(s, ":" + fieldName, fieldValue);
						}
					}
					ps.addParam(realParam);
				}
			}
	}

}
