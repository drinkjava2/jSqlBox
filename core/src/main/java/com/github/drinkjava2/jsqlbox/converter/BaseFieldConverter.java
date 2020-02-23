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
package com.github.drinkjava2.jsqlbox.converter;

import static com.github.drinkjava2.jdbpro.JDBPRO.param;

import com.github.drinkjava2.jdbpro.LinkArrayList;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.DbContextUtils;

/**
 * TailType has a tails() method return a map instance stored tail values
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */
public class BaseFieldConverter implements FieldConverter {

	@Override
	public void handleSQL(SqlOption sqlOption, DbContext ctx, ColumnModel col, Object entity,
			LinkArrayList<Object> sqlBody, LinkArrayList<Object> sqlWhere) {
		if (SqlOption.UPDATE.equals(sqlOption)) {
			Object value = DbContextUtils.readValueFromBeanFieldOrTail(col, entity, false, false);
			if (!sqlBody.isEmpty())
				sqlBody.append(", ");
			sqlBody.append(col.getColumnName()).append("=?").append(param(value));
		} else if (SqlOption.DELETE.equals(sqlOption)) {// NOSONAR
		} else if (SqlOption.INSERT.equals(sqlOption)) {
			Object value = DbContextUtils.readValueFromBeanFieldOrTail(col, entity, false, false);
			sqlBody.append(col.getColumnName());
			sqlBody.append(param(value)).append(",");
		}
	}

	@Override
	public Object entityFieldToDbValue(ColumnModel col, Object entity) {
		return DbContextUtils.doReadFromFieldOrTail(col, entity);
	}

	@Override
	public void writeDbValueToEntityField(Object entityBean, ColumnModel col, Object value) {
		DbContextUtils.doWriteToFieldOrTail(col, entityBean, value);
	}

}