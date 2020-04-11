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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * This utility tool to translate Entity class / Database metaData / Excel(will
 * add in future) file to TableModel
 *
 * @author Yong Zhu
 * @since 1.0.5
 */
public abstract class TableModelUtils {// NOSONAR
	public static final String OPT_LINK_STYLE = "linkStyle";
	public static final String OPT_FIELD_FLAGS = "fieldFlags";
	public static final String OPT_PACKAGE_NAME = "packageName";
	public static final String OPT_IMPORTS = "imports";
	public static final String OPT_CLASS_DEFINITION = "classDefinition";
	public static final String OPT_PUBLIC_FIELD = "enablePublicField";
	public static final String OPT_EXCLUDE_TABLES = "excludeTables";

	/**
	 * Convert tableName to entity class, note: before use this method
	 * entity2Models() method should be called first to cache talbeModels in memory
	 */
	public static Class<?> tableNameToEntityClass(String tableName) {
		return TableModelUtilsOfEntity.tableNameToEntityClass(tableName);
	}

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
	 * 2)This method does not read sequence, index, unique constraints
	 */
	public static TableModel[] db2Models(Connection con, Dialect dialect) {
		return TableModelUtilsOfDb.db2Models(con, dialect);
	}

	/**
	 * Read database structure and write them to Java entity class source code
	 *
	 * @param ds
	 *            The DataSource instance
	 * @param dialect
	 *            The dialect of database
	 * @param outputfolder
	 *            the out put folder
	 * @param setting
	 *            see TableModelUtilsOfJavaSrc.modelToJavaSourceCode() method
	 */
	public static void db2JavaSrcFiles(DataSource ds, Dialect dialect, String outputfolder,
			Map<String, Object> setting) {
		Connection conn = null;
		try {
			conn = ds.getConnection();
			TableModel[] models = db2Models(conn, dialect);
			File dir = new File(outputfolder);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			@SuppressWarnings("unchecked")
			Collection<String> excludeTables = (Collection<String>) setting.get(OPT_EXCLUDE_TABLES);
			if (excludeTables != null) {
				Set<String> t = new HashSet<>();
				for (String s : excludeTables) {
					t.add(s.trim().toLowerCase());
				}
				excludeTables = t;
			}
			for (TableModel model : models) {
				String tableName = model.getTableName();
				if (excludeTables != null && excludeTables.contains(tableName.toLowerCase())) {
					continue;
				}
				File writename = new File(dir, TableModelUtilsOfJavaSrc.getClassNameFromTableModel(model) + ".java");

				writename.createNewFile();// NOSONAR
				BufferedWriter out = new BufferedWriter(new FileWriter(writename));
				String javaSrc = model2JavaSrc(model, setting);
				out.write(javaSrc);
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();//NOSONAR
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					e1.printStackTrace();//NOSONAR
				}
		}
	}

	/**
	 * Convert a TablemModel instance to Java entity class source code
	 *
	 * @param model
	 *            The TableModel instance
	 * @param setting
	 *            see TableModelUtilsOfJavaSrc.modelToJavaSourceCode() method
	 * @return class source code
	 */
	public static String model2JavaSrc(TableModel model, Map<String, Object> setting) {
		return TableModelUtilsOfJavaSrc.modelToJavaSourceCode(model, setting);
	}

	/**
	 * This method bind a tableModel to a entity class, this is a global setting
	 */
	public static void bindGlobalModel(Class<?> entityClass, TableModel tableModel) {
		TableModelUtilsOfEntity.globalTableModelCache.put(entityClass, tableModel);
	}

}
