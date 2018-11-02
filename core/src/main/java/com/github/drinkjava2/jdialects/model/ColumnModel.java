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
package com.github.drinkjava2.jdialects.model;

import java.util.Iterator;
import java.util.List;

import com.github.drinkjava2.jdialects.DebugUtils;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.id.UUIDAnyGenerator;

/**
 * A ColumnModel definition represents a platform dependent column in a Database
 * Table, from 1.0.5 this class name changed from "Column" to "ColumnModel" to
 * avoid naming conflict to JPA's "@Column" annotation
 * 
 * </pre>
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class ColumnModel {
	private String columnName;// no need explain

	private TableModel tableModel; // belong to which tableModel

	private Type columnType;// See com.github.drinkjava2.jdialects.Type

	private Boolean pkey = false; // if is primary key

	private Boolean nullable = true; // if nullable

	/** DDL check string */
	private String check;

	/** DDL default value */
	private String defaultValue;

	/** Optional, put an extra tail String at end of column definition DDL */
	private String tail;

	/** Comment of this column */
	private String comment;

	/** length, precision, scale all share use lengths array */
	private Integer[] lengths = new Integer[] {};

	// =======================================================================
	private GenerationType idGenerationType;

	private String idGeneratorName;
	// =======================================================================

	// =====Below fields are designed only for ORM tools ==========
	/** Map to a Java entity field, for ORM tool use only */
	private String entityField;

	/** The column length, for ORM tool use only */
	private Integer length = 255;

	/** The numeric precision, for ORM tool use only */
	private Integer precision = 0;

	/** The numeric scale, for ORM tool use only */
	private Integer scale = 0;

	/** If insert-able or not, for ORM tool use only */
	private Boolean insertable = true;

	/** If update-able or not, for ORM tool use only */
	private Boolean updatable = true;

	/** If this is a Transient type, for ORM tool use only */
	private Boolean transientable = false;

	/** ShardTable strategy and parameters, for ORM tool use only */
	private String[] shardTable = null;

	/** ShardDatabase strategy and parameters, for ORM tool use only */
	private String[] shardDatabase = null;

	public ColumnModel(String columnName) {
		if (StrUtils.isEmpty(columnName))
			DialectException.throwEX("columnName is not allowed empty");
		this.columnName = columnName;
	}

	/** Add a not null DDL piece if support */
	public ColumnModel notNull() {
		this.nullable = false;
		return this;
	}

	/** Add a column check DDL piece if support */
	public ColumnModel check(String check) {
		this.check = check;
		return this;
	}

	public ColumnModel newCopy() {
		ColumnModel col = new ColumnModel(columnName);
		col.columnType = columnType;
		col.pkey = pkey.booleanValue();
		col.nullable = nullable;
		col.check = check;
		col.defaultValue = defaultValue;
		col.tail = tail;
		col.comment = comment;
		col.lengths = lengths;
		col.entityField = entityField;
		col.length = length;
		col.precision = precision;
		col.scale = scale;
		col.insertable = insertable;
		col.updatable = updatable;
		col.transientable = transientable;
		col.idGeneratorName = idGeneratorName;
		col.idGenerationType = idGenerationType;
		col.shardTable = shardTable;
		col.shardDatabase = shardDatabase;
		return col;
	}

	/**
	 * A shortcut method to add a index for single column, for multiple columns
	 * index please use tableModel.index() method
	 */
	public ColumnModel singleIndex(String indexName) {
		makeSureTableModelExist();
		DialectException.assureNotEmpty(indexName, "indexName can not be empty");
		this.tableModel.index(indexName).columns(this.getColumnName());
		return this;
	}

	/**
	 * A shortcut method to add a index for single column, for multiple columns
	 * index please use tableModel.index() method
	 */
	public ColumnModel singleIndex() {
		makeSureTableModelExist();
		this.tableModel.index().columns(this.getColumnName());
		return this;
	}

	/**
	 * A shortcut method to add a unique constraint for single column, for multiple
	 * columns index please use tableModel.unique() method
	 */
	public ColumnModel singleUnique(String uniqueName) {
		makeSureTableModelExist();
		DialectException.assureNotEmpty(uniqueName, "indexName can not be empty");
		this.tableModel.unique(uniqueName).columns(this.getColumnName());
		return this;
	}

	/**
	 * A shortcut method to add a unique constraint for single column, for multiple
	 * columns index please use tableModel.unique() method
	 */
	public ColumnModel singleUnique() {
		makeSureTableModelExist();
		this.tableModel.unique().columns(this.getColumnName());
		return this;
	}

	private void makeSureTableModelExist() {
		DialectException.assureNotNull(this.tableModel,
				"ColumnModel should belong to a TableModel, please call tableModel.column() method first.");
	}

	/** Default value for column's definition DDL */
	public ColumnModel defaultValue(String value) {
		this.defaultValue = value;
		return this;
	}

	/** Add comments at end of column definition DDL */
	public ColumnModel comment(String comment) {
		this.comment = comment;
		return this;
	}

	/** Mark primary key, if more than one will build compound Primary key */
	public ColumnModel pkey() {
		this.pkey = true;
		return this;
	}

	/** Mark is a shartTable column, for ORM tool use */
	public ColumnModel shardTable(String... shardTable) {
		this.shardTable = shardTable;
		return this;
	}

	/** Mark is a shartDatabase column, for ORM tool use */
	public ColumnModel shardDatabase(String... shardDatabase) {
		this.shardDatabase = shardDatabase;
		return this;
	}

	/**
	 * equal to pkey method. Mark primary key, if more than one will build compound
	 * Primary key
	 */
	public ColumnModel id() {
		this.pkey = true;
		return this;
	}

	/**
	 * A shortcut method to add Foreign constraint for single column, for multiple
	 * columns please use tableModel.fkey() method instead
	 */
	public FKeyModel singleFKey(String... refTableAndColumns) {
		makeSureTableModelExist();
		if (refTableAndColumns == null || refTableAndColumns.length > 2)
			throw new DialectException(
					"singleFKey() first parameter should be table name, second parameter(optional) should be column name");
		return this.tableModel.fkey().columns(this.columnName).refs(refTableAndColumns);
	}

	// ===========id generator methods=======================
	public IdGenerator getIdGenerator() {
		makeSureTableModelExist();
		return this.tableModel.getIdGenerator(idGenerationType, idGeneratorName);
	}

	/** Mark a field will use database's native identity type. */
	public ColumnModel identityId() {
		makeSureTableModelExist();
		this.idGenerationType = GenerationType.IDENTITY;
		this.idGeneratorName = null;
		return this;
	}

	public ColumnModel uuid25() {
		makeSureTableModelExist();
		this.idGenerationType = GenerationType.UUID25;
		this.idGeneratorName = null;
		return this;
	}

	public ColumnModel uuid32() {
		makeSureTableModelExist();
		this.idGenerationType = GenerationType.UUID32;
		this.idGeneratorName = null;
		return this;
	}

	public ColumnModel uuid36() {
		makeSureTableModelExist();
		this.idGenerationType = GenerationType.UUID36;
		this.idGeneratorName = null;
		return this;
	}

	public ColumnModel snowflake() {
		makeSureTableModelExist();
		this.idGenerationType = GenerationType.SNOWFLAKE;
		this.idGeneratorName = null;
		return this;
	}

	public ColumnModel uuidAny(String name, Integer length) {
		makeSureTableModelExist();
		this.idGenerationType = GenerationType.UUID_ANY;
		this.idGeneratorName = name;
		if (this.tableModel.getIdGenerator(GenerationType.UUID_ANY, idGeneratorName) == null)
			this.tableModel.getIdGenerators().add(new UUIDAnyGenerator(name, length));
		return this;
	}

	public ColumnModel timeStampId() {
		makeSureTableModelExist();
		this.idGenerationType = GenerationType.TIMESTAMP;
		this.idGeneratorName = null;
		return this;
	}

	/** Bind column to a global Auto Id generator, can be Sequence or a Table */
	public ColumnModel autoId() {
		makeSureTableModelExist();
		this.idGenerationType = GenerationType.AUTO;
		this.idGeneratorName = null;
		return this;
	}

	public ColumnModel sortedUUID(String name, Integer sortedLength, Integer uuidLength) {
		makeSureTableModelExist();
		this.tableModel.sortedUUIDGenerator(name, sortedLength, uuidLength);
		this.idGenerationType = GenerationType.SORTED_UUID;
		this.idGeneratorName = null;
		return this;
	}

	/** The value of this column will be generated by a sequence */
	public ColumnModel sequenceGenerator(String name, String sequenceName, Integer initialValue,
			Integer allocationSize) {
		makeSureTableModelExist();
		this.tableModel.sequenceGenerator(name, sequenceName, initialValue, allocationSize);
		this.idGenerationType = GenerationType.SEQUENCE;
		this.idGeneratorName = name;
		return this;
	}

	/**
	 * The value of this column will be generated by a sequence or table generator
	 */
	public ColumnModel idGenerator(String idGeneratorName) {
		makeSureTableModelExist();
		this.idGenerationType = null;
		this.idGeneratorName = idGeneratorName;
		return this;
	}

	public ColumnModel tableGenerator(String name, String tableName, String pkColumnName, String valueColumnName,
			String pkColumnValue, Integer initialValue, Integer allocationSize) {
		makeSureTableModelExist();
		this.tableModel.tableGenerator(name, tableName, pkColumnName, valueColumnName, pkColumnValue, initialValue,
				allocationSize);
		this.idGenerationType = GenerationType.TABLE;
		this.idGeneratorName = name;
		return this;
	}

	// ===================================================

	/**
	 * Put an extra tail String manually at the end of column definition DDL
	 */
	public ColumnModel tail(String tail) {
		this.tail = tail;
		return this;
	}

	/**
	 * Mark this column map to a Java entity field, if exist other columns map to
	 * this field, delete other columns. This method only designed for ORM tool
	 */
	public ColumnModel entityField(String entityFieldName) {
		DialectException.assureNotEmpty(entityFieldName, "entityFieldName can not be empty");
		this.entityField = entityFieldName;
		if (this.tableModel != null) {
			List<ColumnModel> oldColumns = this.tableModel.getColumns();
			Iterator<ColumnModel> columnIter = oldColumns.iterator();
			while (columnIter.hasNext()) {
				ColumnModel column = columnIter.next();
				if (entityFieldName.equals(column.getEntityField())
						&& !this.getColumnName().equals(column.getColumnName()))
					columnIter.remove();
			}
		}
		return this;
	}

	/** Mark a field insertable=true, only for JPA or ORM tool use */
	public ColumnModel insertable(Boolean insertable) {
		this.insertable = insertable;
		return this;
	}

	/** Mark a field updatable=true, only for JPA or ORM tool use */
	public ColumnModel updatable(Boolean updatable) {
		this.updatable = updatable;
		return this;
	}

	public String getDebugInfo() {
		return DebugUtils.getColumnModelDebugInfo(this);
	}

	public void checkReadOnly() {
		if (tableModel != null && tableModel.getReadOnly())
			throw new DialectException("TableModel '" + tableModel.getTableName() + "' is readOnly, can not be modified.");
	}
	
	//@formatter:off shut off eclipse's formatter
	public ColumnModel LONG() {this.columnType=Type.BIGINT;return this;} 
	public ColumnModel BOOLEAN() {this.columnType=Type.BOOLEAN;return this;} 
	public ColumnModel DOUBLE() {this.columnType=Type.DOUBLE;return this;} 
	public ColumnModel FLOAT(Integer... lengths) {this.columnType=Type.FLOAT;this.lengths=lengths;return this;} 
	public ColumnModel INTEGER() {this.columnType=Type.INTEGER;return this;} 
	public ColumnModel SHORT() {this.columnType=Type.SMALLINT;return this;} 
	public ColumnModel BIGDECIMAL(Integer precision, Integer scale) {this.columnType=Type.NUMERIC; this.lengths= new Integer[]{precision,scale}; return this;} 
	public ColumnModel STRING(Integer length) {this.columnType=Type.VARCHAR;this.lengths=new Integer[]{length}; return this;} 
	
	public ColumnModel DATE() {this.columnType=Type.DATE;return this;} 
	public ColumnModel TIME() {this.columnType=Type.TIME;return this;} 
	public ColumnModel TIMESTAMP() {this.columnType=Type.TIMESTAMP;return this;} 
	public ColumnModel BIGINT() {this.columnType=Type.BIGINT;return this;} 
	public ColumnModel BINARY(Integer... lengths) {this.columnType=Type.BINARY;this.lengths=lengths; return this;} 
	public ColumnModel BIT() {this.columnType=Type.BIT;return this;} 
	public ColumnModel BLOB(Integer... lengths) {this.columnType=Type.BLOB;this.lengths=lengths;return this;} 
	public ColumnModel CHAR(Integer... lengths) {this.columnType=Type.CHAR;this.lengths=lengths;return this;} 
	public ColumnModel CLOB(Integer... lengths) {this.columnType=Type.CLOB;this.lengths=lengths;return this;} 
	public ColumnModel DECIMAL(Integer... lengths) {this.columnType=Type.DECIMAL;this.lengths=lengths;return this;} 
	public ColumnModel JAVA_OBJECT() {this.columnType=Type.JAVA_OBJECT;return this;} 
	public ColumnModel LONGNVARCHAR(Integer length) {this.columnType=Type.LONGNVARCHAR;this.lengths=new Integer[]{length};return this;} 
	public ColumnModel LONGVARBINARY(Integer... lengths) {this.columnType=Type.LONGVARBINARY;this.lengths=lengths;return this;} 
	public ColumnModel LONGVARCHAR(Integer... lengths) {this.columnType=Type.LONGVARCHAR;this.lengths=lengths;return this;} 
	public ColumnModel NCHAR(Integer length) {this.columnType=Type.NCHAR;this.lengths=new Integer[]{length};return this;} 
	public ColumnModel NCLOB() {this.columnType=Type.NCLOB;return this;} 
	public ColumnModel NUMERIC(Integer... lengths) {this.columnType=Type.NUMERIC;this.lengths=lengths;return this;} 
	public ColumnModel NVARCHAR(Integer length) {this.columnType=Type.NVARCHAR;   this.lengths=new Integer[]{length};return this;} 
	public ColumnModel OTHER(Integer... lengths) {this.columnType=Type.OTHER;this.lengths=lengths;return this;} 
	public ColumnModel REAL() {this.columnType=Type.REAL;return this;} 
	public ColumnModel SMALLINT() {this.columnType=Type.SMALLINT;return this;} 
	public ColumnModel TINYINT() {this.columnType=Type.TINYINT;return this;} 
	public ColumnModel VARBINARY(Integer... lengths) {this.columnType=Type.VARBINARY;this.lengths=lengths;return this;} 
	public ColumnModel VARCHAR(Integer length) {this.columnType=Type.VARCHAR;this.lengths=new Integer[]{length};return this;}
	//@formatter:on

	// getter & setters==============
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public TableModel getTableModel() {
		return tableModel;
	}

	public void setTableModel(TableModel tableModel) {
		this.tableModel = tableModel;
	}

	public Type getColumnType() {
		return columnType;
	}

	public void setColumnType(Type columnType) {
		this.columnType = columnType;
	}

	public Boolean getPkey() {
		return pkey;
	}

	public void setPkey(Boolean pkey) {
		this.pkey = pkey;
	}

	public Boolean getNullable() {
		return nullable;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	public String getCheck() {
		return check;
	}

	public void setCheck(String check) {
		this.check = check;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public GenerationType getIdGenerationType() {
		return idGenerationType;
	}

	public void setIdGenerationType(GenerationType idGenerationType) {
		this.idGenerationType = idGenerationType;
	}

	public String getIdGeneratorName() {
		return idGeneratorName;
	}

	public void setIdGeneratorName(String idGeneratorName) {
		this.idGeneratorName = idGeneratorName;
	}

	public String getTail() {
		return tail;
	}

	public void setTail(String tail) {
		this.tail = tail;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Integer[] getLengths() {
		return lengths;
	}

	public void setLengths(Integer[] lengths) {
		this.lengths = lengths;
	}

	public String getEntityField() {
		return entityField;
	}

	public void setEntityField(String entityField) {
		this.entityField = entityField;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getPrecision() {
		return precision;
	}

	public void setPrecision(Integer precision) {
		this.precision = precision;
	}

	public Integer getScale() {
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}

	public Boolean getInsertable() {
		return insertable;
	}

	public void setInsertable(Boolean insertable) {
		this.insertable = insertable;
	}

	public Boolean getUpdatable() {
		return updatable;
	}

	public void setUpdatable(Boolean updatable) {
		this.updatable = updatable;
	}

	public Boolean getTransientable() {
		return transientable;
	}

	public void setTransientable(Boolean transientable) {
		this.transientable = transientable;
	}

	public String[] getShardTable() {
		return shardTable;
	}

	public void setShardTable(String[] shardTable) {
		this.shardTable = shardTable;
	}

	public String[] getShardDatabase() {
		return shardDatabase;
	}

	public void setShardDatabase(String[] shardDatabase) {
		this.shardDatabase = shardDatabase;
	}

 

}
