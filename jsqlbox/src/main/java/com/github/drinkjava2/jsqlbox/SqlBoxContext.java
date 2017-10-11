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

import javax.sql.DataSource;

import com.github.drinkjava2.jdbpro.DbPro;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.utils.DialectUtils;
import com.github.drinkjava2.jtransactions.ConnectionManager;

/**
 * @author Yong Zhu
 * @since 1.0.0
 */
public class SqlBoxContext extends DbPro {
	private static String sqlBoxSuffixIdentity = "SqlBox";
	public static SqlBoxContext defaultContext = null;
	private Dialect dialect;
	private TableModel[] metaTableModels;

	public SqlBoxContext() {
		super();
	}

	public SqlBoxContext(DataSource ds) {
		super(ds);
		this.dialect = Dialect.guessDialect(ds);
		refreshMetaData();
	}

	public SqlBoxContext(DataSource ds, ConnectionManager cm) {
		super(ds, cm);
		dialect = Dialect.guessDialect(ds);
		refreshMetaData();
	}

	public SqlBoxContext(DataSource ds, Dialect dialect) {
		super(ds);
		this.dialect = dialect;
		refreshMetaData();
	}

	public SqlBoxContext(DataSource ds, Dialect dialect, ConnectionManager cm) {
		super(ds, cm);
		this.dialect = dialect;
		refreshMetaData();
	}

	public void refreshMetaData() {
		metaTableModels = SqlBoxContextUtils.metaDataToModels(this, dialect);
	}

	public static String getSqlBoxSuffixIdentity() {
		return sqlBoxSuffixIdentity;
	}

	public static void setSqlBoxSuffixIdentity(String newSqlBoxSuffixIdentity) {
		sqlBoxSuffixIdentity = newSqlBoxSuffixIdentity;
	}

	public SqlBox findSqlBox(Object entity) {
		SqlBoxException.assureNotNull(entity, "Can not find box instance for null entity");
		SqlBox box = SqlBoxFindUtils.getBindedBox(entity);
		if (box != null)
			return box;
		box = createSqlBox(entity.getClass());
		SqlBoxFindUtils.bindBoxToBean(box, entity, this);
		return box;
	}

	public SqlBox createSqlBox(Class<?> entityOrBoxClass) {
		Class<?> boxClass = null;
		if (entityOrBoxClass == null)
			throw new SqlBoxException("Bean Or SqlBox class can not be null");
		if (SqlBox.class.isAssignableFrom(entityOrBoxClass))
			boxClass = entityOrBoxClass;
		if (boxClass == null)
			boxClass = ClassCacheUtils.checkClassExist(entityOrBoxClass.getName() + sqlBoxSuffixIdentity);
		if (boxClass == null)
			boxClass = ClassCacheUtils.checkClassExist(
					entityOrBoxClass.getName() + "$" + entityOrBoxClass.getSimpleName() + sqlBoxSuffixIdentity);
		if (boxClass != null && !SqlBox.class.isAssignableFrom((Class<?>) boxClass))
			boxClass = null;
		SqlBox box = null;
		if (boxClass == null) {
			box = new SqlBox();
			box.setTableModel(DialectUtils.pojo2Model(entityOrBoxClass));
		} else {
			try {
				box = (SqlBox) boxClass.newInstance();
				TableModel model = box.getTableModel();
				if (model == null) {
					model = DialectUtils.pojo2Model(entityOrBoxClass);
					box.setTableModel(model);
				}
				Method configMethod = null;
				try {// NOSONAR
					configMethod = boxClass.getMethod("config", TableModel.class);
				} catch (Exception e) {// NOSONAR
				}
				if (configMethod != null)
					configMethod.invoke(box, model);
			} catch (Exception e) {
				throw new SqlBoxException("Can not create SqlBox instance: " + entityOrBoxClass, e);
			}
		}
		if (box.getContext() == null)
			box.setContext(this);
		return box;
	}

	public TableModel getMetaTableModel(String tableName) {
		for (TableModel tableModel : metaTableModels)
			if (tableName.equalsIgnoreCase(tableModel.getTableName()))
				return tableModel;
		return null;
	}

	// =============CRUD methods=====
	public void insert(Object entity) {
		SqlBoxContextUtils.insert(entity, this.findSqlBox(entity));
	}

	public void update(Object entity) {
	}

	public void delete(Object entity) {
		SqlBoxContextUtils.delete(entity, this.findSqlBox(entity));
	}

	public <T> T load(Object entity, Object pkey) {
		return null;
	}

	// ========Utils methods=====
	// DDL about
	public String[] pojos2CreateDDLs(Class<?>... pojoClasses) {
		return dialect.toCreateDDL(pojoClasses);
	}

	public String[] pojos2DropAndCreateDDLs(Class<?>... pojoClasses) {
		return dialect.toDropAndCreateDDL(pojoClasses);
	}

	public String[] pojos2DropDDL(Class<?>... pojoClasses) {
		return dialect.toDropDDL(pojoClasses);
	}

	// getter & setter =======

	public Dialect getDialect() {
		return dialect;
	}

	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

	public static SqlBoxContext getDefaultContext() {
		return defaultContext;
	}

	public static void setDefaultContext(SqlBoxContext defaultContext) {
		SqlBoxContext.defaultContext = defaultContext;
	}

	public TableModel[] getMetaTableModels() {
		return metaTableModels;
	}

	public void setMetaTableModels(TableModel[] metaTableModels) {
		this.metaTableModels = metaTableModels;
	}

}