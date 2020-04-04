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
package com.github.drinkjava2.jsqlbox.sqlitem;

import com.github.drinkjava2.jdbpro.CustomizedSqlItem;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.DbContextUtils;
import com.github.drinkjava2.jsqlbox.DbException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This Sample is a CustomizedSqlItem, used to build a " where field1=? and
 * field2=? ..." SQL piece, usages see below (detail demo see CrudTest.java):
 * 
 * <pre>
 * Assert.assertEquals(1, ctx.entityFindBySample(u1).size());
 * Assert.assertEquals(1, ctx.entityFindAll(CrudUser.class, new SampleItem(u2).sql(" where  ").notNullFields()).size());
 * CrudUser sample = new CrudUser("Nam", "addr");
 * Assert.assertEquals(4, ctx.entityFindAll(CrudUser.class, new SampleItem(sample).sql(" where (").allFields()
 * 		.sql(") or name like ?").param(":name%").sql(" order by name")).size());
 * Assert.assertEquals(4,
 * 		ctx.entityFindBySQL(CrudUser.class, new SampleItem(sample).sql("select * from CrudUser where (").nullFields()
 * 				.sql(") or name like ?").param(":name%").sql(" order by name")).size());
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class SampleItem implements CustomizedSqlItem {
	Object entityBean;
	TableModel model = null;
	List<Object> sqlItems = new ArrayList<Object>();

	public SampleItem(Object entityBean) {
		this.entityBean = entityBean;
	}

	public SampleItem(Object entityBean, TableModel model) {
		this.entityBean = entityBean;
		this.model = model;
	}

	/**
	 * For all not null fields, create field1=? and field2=?... sql pieces
	 */
	public SampleItem notNullFields() {
		sqlItems.add("#NOTNULL_Fields");
		return this;
	}

	/**
	 * For all fields has null values, create field1 is null and field2 is null...
	 * sql pieces
	 */
	public SampleItem nullFields() {
		sqlItems.add("#NULL_Fields");
		return this;
	}

	/**
	 * For all fields, create field1=? and field2=? and field3 is null... sql pieces
	 */
	public SampleItem allFields() {
		sqlItems.add("#ALL_Fields");
		return this;
	}

	/** Add a sql piece */
	public SampleItem sql(String... sqlPiece) {
		for (String sql : sqlPiece)
			sqlItems.add(sql);
		return this;
	}

	/** Add a Sql param */
	public SampleItem param(Object... params) {
		for (Object param : params)
			sqlItems.add(new Object[] { param });
		return this;
	}

	private void dealAllFields(PreparedSQL ps, TableModel model, Boolean nullFieldsOnly) {
		boolean isfirst = true;
		Map<String, Method> writeMethods = ClassCacheUtils.getClassWriteMethods(entityBean.getClass());
		for (String fieldName : writeMethods.keySet()) {
			ColumnModel col = model.getColumn(fieldName);
			if (Boolean.TRUE.equals(col.getTransientable()))
				continue;
			Object fieldValue = DbContextUtils.readValueFromBeanFieldOrTail(col, entityBean, false,false);
			if (fieldValue != null && (nullFieldsOnly == null || !nullFieldsOnly)) {
				if (!isfirst)
					ps.addSql(" and ");
				ps.addSql(ps.wrapColumn(col.getColumnName())).append(" =? ");
				ps.addParam(fieldValue);
				isfirst = false;
			} else if (nullFieldsOnly == null || nullFieldsOnly) {
				if (!isfirst)
					ps.addSql(" and ");
				ps.addSql(ps.wrapColumn(col.getColumnName())).append(" is null ");
				isfirst = false;
			}
		}
		if (isfirst) // not found any fields
			ps.addSql(" 1=1 ");
	}

	@Override
	public void doPrepare(PreparedSQL ps) {
		if (model == null)
			model = DbContextUtils.findEntityOrClassTableModel(entityBean);
		DbException.assureNotNull(model.getEntityClass());
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
					// Below code will change ":fieldName%" to param("fieldValue%")
					if (param instanceof String) {
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
							DbException.assureNotEmpty(fieldName);
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
