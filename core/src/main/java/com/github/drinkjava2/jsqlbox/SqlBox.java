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

import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * SqlBox is a configuration object, usually be binded to an entity to save some
 * extra info for this entity. The most important part is "TableModel", it's a
 * virtual model not related to any real database table, see jDialects project.
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class SqlBox {
	/** A TableModel instance */
	TableModel tableModel;

	/**
	 * Optional, a SqlBoxContext instance, usually used for ActiveRecordSupport
	 * entity
	 */
	SqlBoxContext context;

	// Shortcut method
	public String table() {
		if (tableModel == null)
			return null;
		return tableModel.getTableName();
	}

	/**
	 * @return tableModel's entityClass
	 */
	public Class<?> getEntityClass() {
		if (tableModel == null)
			return null;
		else
			return tableModel.getEntityClass();
	}

	/**
	 * Set tableModel's entityClass
	 */
	public void setEntityClass(Class<?> entityClass) {
		if (tableModel == null)
			throw new SqlBoxException("Can not set entityClass on null tableModel");
		tableModel.setEntityClass(entityClass);
	}

	// getter & setter ========
	public SqlBoxContext getContext() {
		return context;
	}

	public void setContext(SqlBoxContext context) {
		this.context = context;
	}

	public TableModel getTableModel() {
		return tableModel;
	}

	public void setTableModel(TableModel tableModel) {
		this.tableModel = tableModel;
	}

}
