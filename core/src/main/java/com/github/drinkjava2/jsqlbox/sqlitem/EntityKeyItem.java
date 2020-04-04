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
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.DbContextUtils;
import com.github.drinkjava2.jsqlbox.DbException;

/**
 * This EntityKeyItem is used to build a " alias.id1=? and alias.id2=? ..." SQL
 * piece, alias is based on current PreparedSQL's setting
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class EntityKeyItem implements CustomizedSqlItem {
	private Object entity;

	public EntityKeyItem(Object entity) {
		this.entity = entity;
	}

	@Override
	public void doPrepare(PreparedSQL ps) {
		TableModel model = null;
		String alias = null;
		for (int i = 0; i < ps.getModels().length; i++) {
			TableModel psmodel = (TableModel) ps.getModels()[i];
			if (entity.getClass().equals(psmodel.getEntityClass())) {
				model = psmodel;
				alias = ps.getAliases()[i];
			}
		}
		DbException.assureNotNull(model);// found the model of entity
		DbException.assureNotEmpty(alias); // found the alias
		doAppendEntityKeyParameters(ps, alias, model);
	}

	private void doAppendEntityKeyParameters(PreparedSQL ps, String alias, TableModel model) {
		int i = 0;
		if (model != null)
			for (ColumnModel col : model.getColumns()) {
				if (col.getPkey() && !col.getTransientable()) {
					if (i > 0)
						ps.addSql(" and ");
					ps.addSql(alias).append(".").append(col.getColumnName()).append("=? ");
					Object value = DbContextUtils.readValueFromBeanFieldOrTail(col, entity, false, false);
					ps.addParam(value);
					i++;
				}
			}
	}
}
