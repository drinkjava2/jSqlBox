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
	private static String sqlBoxSuffixIdentity = "BX";
	public static SqlBoxContext defaultContext = null;
	private Dialect dialect;

	public SqlBoxContext() {
		super();
	}

	public SqlBoxContext(DataSource ds) {
		super(ds);
		this.dialect = Dialect.guessDialect(ds);
	}

	public SqlBoxContext(DataSource ds, ConnectionManager cm) {
		super(ds, cm);
		dialect = Dialect.guessDialect(ds);
	}

	public SqlBoxContext(DataSource ds, Dialect dialect) {
		super(ds);
		this.dialect = dialect;
	}

	public SqlBoxContext(DataSource ds, Dialect dialect, ConnectionManager cm) {
		super(ds, cm);
		this.dialect = dialect;
	}

	public static String getSqlBoxSuffixIdentity() {
		return sqlBoxSuffixIdentity;
	}

	public static void setSqlBoxSuffixIdentity(String newSqlBoxSuffixIdentity) {
		sqlBoxSuffixIdentity = newSqlBoxSuffixIdentity;
	}

	public SqlBox findSqlBox(Object entity) {
		SqlBoxException.assureNotNull(entity, "Can not find box instance for null entity");
		SqlBox box = SqlBoxUtility.getBindedBox(entity);
		if (box != null)
			return box;
		box = createSqlBox(entity.getClass());
		SqlBoxUtility.bindBoxToBean(box, entity, this);
		return box;
	}

	public SqlBox createSqlBox(Class<?> entityOrBoxClass) {
		Class<?> boxClass = null;
		if (entityOrBoxClass == null)
			throw new SqlBoxException("Bean Or SqlBox class can not be null");
		if (SqlBox.class.isAssignableFrom(entityOrBoxClass))
			boxClass = entityOrBoxClass;
		if (boxClass == null)
			boxClass = SqlBoxUtility.checkSqlBoxClassExist(entityOrBoxClass.getName() + sqlBoxSuffixIdentity);
		if (boxClass == null)
			boxClass = SqlBoxUtility.checkSqlBoxClassExist(
					entityOrBoxClass.getName() + "$" + entityOrBoxClass.getSimpleName() + sqlBoxSuffixIdentity);
		SqlBox box = null;
		if (boxClass == null) {
			box = new SqlBox();
			box.setTableModel(DialectUtils.pojo2Model(entityOrBoxClass).newCopy());
		} else {
			try {
				box = (SqlBox) boxClass.newInstance();
				TableModel model = box.getTableModel();
				if (model == null) {
					model = DialectUtils.pojo2Model(entityOrBoxClass).newCopy();
					box.setTableModel(model);
				}
				Method configMethod = null;
				try {//NOSONAR
					configMethod = boxClass.getMethod("config", TableModel.class);
				} catch (Exception e) {//NOSONAR
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

	// =============CRUD methods=====
	public void insert(Object entity) {
		SqlBoxContextUtility.insert(entity, this.findSqlBox(entity));
	}

	public void update(Object entity) {
	}

	public void delete(Object entity) {
	}

	public <T> T load(Object entity, Object pkey) {
		return null;
	}

	// ========Utils methods=====
	// DDL about
	public String[] pojos2CreateDDLs(Class<?> pojoClasses) {
		return dialect.toCreateDDL(pojoClasses);
	}

	public String[] pojos2DropAndCreateDDLs(Class<?> pojoClasses) {
		return dialect.toDropAndCreateDDL(pojoClasses);
	}

	public String[] pojo2DropDDL(Class<?> pojoClasses) {
		return dialect.toDropDDL(pojoClasses);
	}

	public String[] pojos2CreateDDLs(Object entity) {
		return dialect.toCreateDDL(this.findSqlBox(entity).getTableModel());
	}

	public String[] pojos2DropAndCreateDDLs(Object entity) {
		return dialect.toDropAndCreateDDL(this.findSqlBox(entity).getTableModel());
	}

	public String[] pojo2DropDDL(Object entity) {
		return dialect.toDropDDL(this.findSqlBox(entity).getTableModel());
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

}