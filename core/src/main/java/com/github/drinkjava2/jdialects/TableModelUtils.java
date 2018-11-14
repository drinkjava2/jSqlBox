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
package com.github.drinkjava2.jdialects;

import java.sql.Connection;

import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * This utility tool to translate Entity class / Database metaData / Excel(will
 * add in future) file to TableModel
 * 
 * @author Yong Zhu
 * @since 1.0.5
 */
public abstract class TableModelUtils {// NOSONAR
	/** Convert entity class to a editable TableModel instance */
	public static TableModel entity2Model(Class<?> entityClass) {
		return TableModelUtilsOfEntity.entity2EditableModel(entityClass);
	}

	/** Convert entity classes to editable TableModel instances */
	public static TableModel[] entity2Models(Class<?>... entityClasses) {
		return TableModelUtilsOfEntity.entity2EditableModels(entityClasses);
	}

	/** Convert entity class to a read-only TableModel instance */
	public static TableModel entity2ReadOnlyModel(Class<?> entityClass) {
		return TableModelUtilsOfEntity.entity2ReadOnlyModel(entityClass);
	}

	/** Convert entity classes to read-only TableModel instances */
	public static TableModel[] entity2ReadOnlyModels(Class<?>... entityClasses) {
		return TableModelUtilsOfEntity.entity2ReadOnlyModel(entityClasses);
	}

	/**
	 * Convert database metaData to TableModels, note: <br/>
	 * 1)This method does not close connection, do not forgot close it later <br/>
	 * 2)This method does not support sequence, foreign keys, primary keys..., only
	 * read the basic database columns structure, but in future version may support
	 */
	public static TableModel[] db2Models(Connection con, Dialect dialect) {
		return TableModelUtilsOfDb.db2Model(con, dialect);
	}

	/**
	 * This method bind a tableModel to a entity class, this is a global setting
	 */
	public static void bindTableModel(Class<?> entityClass, TableModel tableModel) {
		TableModelUtilsOfEntity.globalTableModelCache.put(entityClass, tableModel);
	}
}
