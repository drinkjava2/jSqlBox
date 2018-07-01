/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import java.sql.Connection;

import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * This utility tool should have below methods:
 * 
 * entity2Model() method: Convert 1 entity class or annotated entity class to
 * TableModel Objects
 * 
 * entity2Models() method: Convert entity classes or annotated entity classes to
 * TableModel Objects
 * 
 * db2Models() method: Convert JDBC database to TableModels
 * 
 * Models2Excel() method: Convert TableModel Objects to Excel CSV format text
 *
 * excel2Models() method: Convert Excel CSV format text to TableModel Objects
 * 
 * Models2EntitySrc method: Convert TableModel Object to Annotated entity class
 * Java Source code
 * 
 * Model2ModelSrc method: Convert TableModel Objects to Java configuration, for
 * example: TableModel t= new TableModel("tb"), t.column("col").LONG()...
 * 
 * @author Yong Zhu
 * @since 1.0.5
 */
public abstract class TableModelUtils {
	/**
	 * Convert entity class or JPA annotated entity class to "TableModel" Object, if
	 * class have a "config(TableModel tableModel)" method, will also call it. This
	 * method support annotations on entity class, detail see README.md.
	 * 
	 * @param entityClass
	 * @return TableModel
	 */
	public static TableModel entity2Model(Class<?> entityClass) {
		return TableModelUtilsOfEntity.oneEntity2Model(entityClass);
	}

	/**
	 * Convert entity or JPA annotated entity classes to "TableModel" Object, if
	 * these classes have a "config(TableModel tableModel)" method, will also call
	 * it. This method support Annotations on entity class, detail see README.md.
	 * 
	 * @param entityClasses
	 * @return TableModel
	 */
	public static TableModel[] entity2Models(Class<?>... entityClasses) {
		return TableModelUtilsOfEntity.entity2Model(entityClasses);
	}

	/**
	 * Convert JDBC connected database structure to TableModels, note: <br/>
	 * 1)This method does not close connection, do not forgot close it later <br/>
	 * 2)This method does not support sequence, foreign keys, primary keys..., only
	 * read the database structure, but in future version may support
	 */
	public static TableModel[] db2Models(Connection con, Dialect dialect) {
		return TableModelUtilsOfDb.db2Model(con, dialect);
	}

}
