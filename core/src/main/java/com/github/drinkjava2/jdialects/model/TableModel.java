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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.github.drinkjava2.jdialects.DebugUtils;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;
import com.github.drinkjava2.jdialects.id.AutoIdGenerator;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.id.IdentityIdGenerator;
import com.github.drinkjava2.jdialects.id.SequenceIdGenerator;
import com.github.drinkjava2.jdialects.id.SnowflakeGenerator;
import com.github.drinkjava2.jdialects.id.SortedUUIDGenerator;
import com.github.drinkjava2.jdialects.id.TableIdGenerator;
import com.github.drinkjava2.jdialects.id.TimeStampIdGenerator;
import com.github.drinkjava2.jdialects.id.UUID25Generator;
import com.github.drinkjava2.jdialects.id.UUID32Generator;
import com.github.drinkjava2.jdialects.id.UUID36Generator;
import com.github.drinkjava2.jdialects.id.UUIDAnyGenerator;

/**
 * A TableModel definition represents a platform dependent Database Table, from
 * 1.0.5 this class name changed from "Table" to "TableModel" to avoid naming
 * conflict to JPA's "@Table" annotation
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class TableModel {
	/** The table tableName in database */
	private String tableName;

	/** check constraint for table */
	private String check;

	/** comment for table */
	private String comment;

	/**
	 * Optional, If support engine like MySQL or MariaDB, add engineTail at the end
	 * of "create table..." DDL, usually used to set encode String like " DEFAULT
	 * CHARSET=utf8" for MySQL
	 */
	private String engineTail;

	/** Columns in this table */
	private List<ColumnModel> columns = new ArrayList<ColumnModel>();

	/** IdGenerators */
	private List<IdGenerator> idGenerators;

	/** Foreign Keys */
	private List<FKeyModel> fkeyConstraints;

	/** Indexes */
	private List<IndexModel> indexConsts;

	/** Unique constraints */
	private List<UniqueModel> uniqueConsts;

	/**
	 * Map to which entityClass, this field is designed to ORM tool to use
	 */
	private Class<?> entityClass;

	private Boolean readOnly = false;

	public TableModel() {
		super();
	}

	public TableModel(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return an editable copy of current TableModel
	 */
	public TableModel newCopy() {// NOSONAR
		TableModel tb = new TableModel();
		tb.tableName = this.tableName;
		tb.check = this.check;
		tb.comment = this.comment;
		tb.engineTail = this.engineTail;
		tb.entityClass = this.entityClass;
		if (!columns.isEmpty())
			for (ColumnModel item : columns) {
				ColumnModel newItem = item.newCopy();
				newItem.setTableModel(tb);
				tb.columns.add(newItem);
			}

		if (idGenerators != null && !idGenerators.isEmpty())
			for (IdGenerator item : idGenerators)
				tb.getIdGenerators().add(item.newCopy());

		if (fkeyConstraints != null && !fkeyConstraints.isEmpty())
			for (FKeyModel item : fkeyConstraints)
				tb.getFkeyConstraints().add(item.newCopy());

		if (indexConsts != null && !indexConsts.isEmpty())
			for (IndexModel item : indexConsts)
				tb.getIndexConsts().add(item.newCopy());

		if (uniqueConsts != null && !uniqueConsts.isEmpty())
			for (UniqueModel item : uniqueConsts)
				tb.getUniqueConsts().add(item.newCopy());
		return tb;
	}

	/** Add a TableGenerator */
	public void tableGenerator(String name, String tableName, String pkColumnName, String valueColumnName,
			String pkColumnValue, Integer initialValue, Integer allocationSize) {
		checkReadOnly();
		addGenerator(new TableIdGenerator(name, tableName, pkColumnName, valueColumnName, pkColumnValue, initialValue,
				allocationSize));
	}

	/** Add a UUIDAnyGenerator */
	public void uuidAny(String name, Integer length) {
		checkReadOnly();
		addGenerator(new UUIDAnyGenerator(name, length));
	}

	/**
	 * Add a "create table..." DDL to generate ID, similar like JPA's TableGen
	 */
	public void addGenerator(IdGenerator generator) {
		checkReadOnly();
		DialectException.assureNotNull(generator);
		DialectException.assureNotNull(generator.getGenerationType());
		DialectException.assureNotEmpty(generator.getIdGenName(), "IdGenerator name can not be empty");
		getIdGenerators().add(generator);
	}

	/**
	 * Add a sequence definition DDL, note: some dialects do not support sequence
	 * 
	 * @param name
	 *            The name of sequence Java object itself
	 * @param sequenceName
	 *            the name of the sequence will created in database
	 * @param initialValue
	 *            The initial value
	 * @param allocationSize
	 *            The allocationSize
	 */
	public void sequenceGenerator(String name, String sequenceName, Integer initialValue, Integer allocationSize) {
		checkReadOnly();
		this.addGenerator(new SequenceIdGenerator(name, sequenceName, initialValue, allocationSize));
	}

	/**
	 * Add a Sequence Generator, note: not all database support sequence
	 */
	public void sortedUUIDGenerator(String name, Integer sortedLength, Integer uuidLength) {
		checkReadOnly();
		DialectException.assureNotNull(name);
		if (this.getIdGenerator(GenerationType.SORTED_UUID, name) != null)
			throw new DialectException("Duplicated sortedUUIDGenerator name '" + name + "'");
		idGenerators.add(new SortedUUIDGenerator(name, sortedLength, uuidLength));
	}

	/**
	 * Add the table check, note: not all database support table check
	 */
	public TableModel check(String check) {
		checkReadOnly();
		this.check = check;
		return this;
	}

	/**
	 * Add the table comment, note: not all database support table comment
	 */
	public TableModel comment(String comment) {
		checkReadOnly();
		this.comment = comment;
		return this;
	}

	/**
	 * Add a ColumnModel
	 */
	public TableModel addColumn(ColumnModel column) {
		checkReadOnly();
		DialectException.assureNotNull(column);
		DialectException.assureNotEmpty(column.getColumnName(), "Column's columnName can not be empty");
		column.setTableModel(this);
		columns.add(column);
		return this;
	}

	/**
	 * Remove a ColumnModel by given columnName
	 */
	public TableModel removeColumn(String columnName) {
		checkReadOnly();
		List<ColumnModel> oldColumns = this.getColumns();
		Iterator<ColumnModel> columnIter = oldColumns.iterator();
		while (columnIter.hasNext())
			if (columnIter.next().getColumnName().equalsIgnoreCase(columnName))
				columnIter.remove();
		return this;
	}

	/**
	 * Remove a FKey by given fkeyName
	 */
	public TableModel removeFKey(String fkeyName) {
		checkReadOnly();
		List<FKeyModel> fkeys = getFkeyConstraints();
		Iterator<FKeyModel> fkeyIter = fkeys.iterator();
		while (fkeyIter.hasNext())
			if (fkeyIter.next().getFkeyName().equalsIgnoreCase(fkeyName))
				fkeyIter.remove();
		return this;
	}

	/**
	 * find column in tableModel by given columnName, if not found, add a new column
	 * with columnName
	 */
	public ColumnModel column(String columnName) {// NOSONAR
		for (ColumnModel columnModel : columns) {
			if (columnModel.getColumnName() != null && columnModel.getColumnName().equalsIgnoreCase(columnName))
				return columnModel;
			if (columnModel.getEntityField() != null && columnModel.getEntityField().equalsIgnoreCase(columnName))
				return columnModel;
		}
		return addColumn(columnName);
	}

	/**
	 * Add a column with given columnName to tableModel
	 * 
	 * @param columnName
	 * @return the Column object
	 */
	public ColumnModel addColumn(String columnName) {
		checkReadOnly();
		DialectException.assureNotEmpty(columnName, "columnName can not be empty");
		for (ColumnModel columnModel : columns)
			if (columnName.equalsIgnoreCase(columnModel.getColumnName()))
				throw new DialectException("ColumnModel name '" + columnName + "' already existed");
		ColumnModel column = new ColumnModel(columnName);
		addColumn(column);
		return column;
	}

	/**
	 * Get ColumnModel by column Name or field name ignore case, if not found,
	 * return null
	 */
	public ColumnModel getColumn(String colOrFieldName) {
		for (ColumnModel columnModel : columns) {
			if (columnModel.getColumnName() != null && columnModel.getColumnName().equalsIgnoreCase(colOrFieldName))
				return columnModel;
			if (columnModel.getEntityField() != null && columnModel.getEntityField().equalsIgnoreCase(colOrFieldName))
				return columnModel;
		}
		return null;
	}

	/**
	 * Get ColumnModel by columnName ignore case, if not found, return null
	 */
	public ColumnModel getColumnByColName(String colName) {
		for (ColumnModel columnModel : columns) {
			if (columnModel.getColumnName() != null && columnModel.getColumnName().equalsIgnoreCase(colName))
				return columnModel;
		}
		return null;
	}

	/**
	 * Get ColumnModel by entity field name ignore case, if not found, return null
	 */
	public ColumnModel getColumnByFieldName(String fieldName) {
		for (ColumnModel columnModel : columns)
			if (columnModel.getEntityField() != null && columnModel.getEntityField().equalsIgnoreCase(fieldName))
				return columnModel;
		return null;
	}

	/**
	 * @return First found ShardTable Column , if not found , return null
	 */
	public ColumnModel getShardTableColumn() {
		for (ColumnModel columnModel : columns)
			if (columnModel.getShardTable() != null)
				return columnModel;// return first found only
		return null;
	}

	/**
	 * @return First found ShardDatabase Column , if not found , return null
	 */
	public ColumnModel getShardDatabaseColumn() {
		for (ColumnModel columnModel : columns)
			if (columnModel.getShardDatabase() != null)
				return columnModel;// return first found only
		return null;
	}

	/**
	 * Start add a foreign key definition in DDL, detail usage see demo
	 */
	public FKeyModel fkey() {
		checkReadOnly();
		FKeyModel fkey = new FKeyModel();
		fkey.setTableName(this.tableName);
		fkey.setTableModel(this);
		getFkeyConstraints().add(fkey);
		return fkey;
	}

	/**
	 * Start add a foreign key definition in DDL, detail usage see demo
	 */
	public FKeyModel fkey(String fkeyName) {
		checkReadOnly();
		FKeyModel fkey = new FKeyModel();
		fkey.setTableName(this.tableName);
		fkey.setFkeyName(fkeyName);
		fkey.setTableModel(this);
		getFkeyConstraints().add(fkey);
		return fkey;
	}

	/**
	 * Get a FKeyModel by given fkeyName
	 */
	public FKeyModel getFkey(String fkeyName) {
		if (fkeyConstraints == null)
			return null;
		for (FKeyModel fkey : fkeyConstraints)
			if (!StrUtils.isEmpty(fkeyName) && fkeyName.equalsIgnoreCase(fkey.getFkeyName()))
				return fkey;
		return null;
	}

	/**
	 * Start add a Index in DDL, detail usage see demo
	 */
	public IndexModel index() {
		checkReadOnly();
		IndexModel index = new IndexModel();
		index.setTableModel(this);
		getIndexConsts().add(index);
		return index;
	}

	/**
	 * Start add a Index in DDL, detail usage see demo
	 */
	public IndexModel index(String indexName) {
		checkReadOnly();
		IndexModel index = new IndexModel();
		index.setName(indexName);
		index.setTableModel(this);
		getIndexConsts().add(index);
		return index;
	}

	/**
	 * Start add a unique constraint in DDL, detail usage see demo
	 */
	public UniqueModel unique() {
		checkReadOnly();
		UniqueModel unique = new UniqueModel();
		unique.setTableModel(this);
		getUniqueConsts().add(unique);
		return unique;
	}

	/**
	 * Start add a unique constraint in DDL, detail usage see demo
	 */
	public UniqueModel unique(String uniqueName) {
		checkReadOnly();
		UniqueModel unique = new UniqueModel();
		unique.setName(uniqueName);
		unique.setTableModel(this);
		getUniqueConsts().add(unique);
		return unique;
	}

	/**
	 * If support engine like MySQL or MariaDB, add engineTail at the end of "create
	 * table..." DDL, usually used to set encode String like " DEFAULT CHARSET=utf8"
	 * for MySQL
	 */
	public TableModel engineTail(String engineTail) {
		checkReadOnly();
		this.engineTail = engineTail;
		return this;
	}

	/**
	 * Search and return the IdGenerator in this TableModel by its generationType
	 * and name
	 */
	public IdGenerator getIdGenerator(GenerationType generationType, String name) {
		return getIdGenerator(generationType, name, getIdGenerators());
	}

	/**
	 * Get one of these IdGenerator instance by generationType:
	 * IDENTITY,AUTO,UUID25,UUID32,UUID36,TIMESTAMP
	 */
	public IdGenerator getIdGenerator(GenerationType generationType) {
		return getIdGeneratorByType(generationType);
	}

	/**
	 * Search and return the IdGenerator in this TableModel by its name
	 */
	public IdGenerator getIdGenerator(String name) {
		return getIdGenerator(null, name, getIdGenerators());
	}

	/**
	 * Get one of these IdGenerator instance by generationType:
	 * IDENTITY,AUTO,UUID25,UUID32,UUID36,TIMESTAMP, if not found , return null;
	 */
	public static IdGenerator getIdGeneratorByType(GenerationType generationType) {
		if (generationType == null)
			return null;
		switch (generationType) {
		case IDENTITY:
			return IdentityIdGenerator.INSTANCE;
		case AUTO:
			return AutoIdGenerator.INSTANCE;
		case UUID25:
			return UUID25Generator.INSTANCE;
		case UUID32:
			return UUID32Generator.INSTANCE;
		case UUID36:
			return UUID36Generator.INSTANCE;
		case TIMESTAMP:
			return TimeStampIdGenerator.INSTANCE;
		case SNOWFLAKE:
			return SnowflakeGenerator.INSTANCE;
		default:
			return null;
		}
	}

	/**
	 * Get a IdGenerator by type, if not found, search by name
	 */
	public static IdGenerator getIdGenerator(GenerationType generationType, String name,
			List<IdGenerator> idGeneratorList) {
		// fixed idGenerators
		IdGenerator idGen = getIdGeneratorByType(generationType);
		if (idGen != null)
			return idGen;
		if (StrUtils.isEmpty(name))
			return null;
		for (IdGenerator idGenerator : idGeneratorList) {
			if (generationType != null && name.equalsIgnoreCase(idGenerator.getIdGenName()))
				return idGenerator;
			if ((generationType == null || GenerationType.OTHER.equals(generationType))
					&& name.equalsIgnoreCase(idGenerator.getIdGenName()))
				return idGenerator;
		}
		return null;
	}

	public int getPKeyCount() {
		int pkeyCount = 0;
		for (ColumnModel col : columns)
			if (col.getPkey() && !col.getTransientable())
				pkeyCount++;
		return pkeyCount;
	}

	public ColumnModel getFirstPKeyColumn() {
		for (ColumnModel col : columns)
			if (col.getPkey() && !col.getTransientable())
				return col;
		return null;
	}

	/** Get pkey columns sorted by column name */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<ColumnModel> getPKeyColsSortByColumnName() {
		List<ColumnModel> pkeyCols = new ArrayList<ColumnModel>();
		for (ColumnModel col : columns)
			if (col.getPkey() && !col.getTransientable())
				pkeyCols.add(col);
		Collections.sort(pkeyCols, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((ColumnModel) o1).getColumnName().compareTo(((ColumnModel) o1).getColumnName());
			}
		});
		return pkeyCols;
	}

	public String getDebugInfo() {
		return DebugUtils.getTableModelDebugInfo(this);
	}

	private void checkReadOnly() {
		if (readOnly)
			throw new DialectException("TableModel '" + tableName + "' is readOnly, can not be modified.");
	}
	// getter & setter=========================

	protected void getAndSetters____________________________() {// NOSONAR
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		checkReadOnly();
		this.tableName = tableName;
	}

	public String getCheck() {
		return check;
	}

	public void setCheck(String check) {
		checkReadOnly();
		this.check = check;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		checkReadOnly();
		this.comment = comment;
	}

	public List<ColumnModel> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnModel> columns) {
		checkReadOnly();
		this.columns = columns;
	}

	public List<FKeyModel> getFkeyConstraints() {
		if (fkeyConstraints == null)
			fkeyConstraints = new ArrayList<FKeyModel>();
		return fkeyConstraints;
	}

	public void setFkeyConstraints(List<FKeyModel> fkeyConstraints) {
		checkReadOnly();
		this.fkeyConstraints = fkeyConstraints;
	}

	public String getEngineTail() {
		return engineTail;
	}

	public void setEngineTail(String engineTail) {
		checkReadOnly();
		this.engineTail = engineTail;
	}

	public List<IndexModel> getIndexConsts() {
		if (indexConsts == null)
			indexConsts = new ArrayList<IndexModel>();
		return indexConsts;
	}

	public void setIndexConsts(List<IndexModel> indexConsts) {
		checkReadOnly();
		this.indexConsts = indexConsts;
	}

	public List<UniqueModel> getUniqueConsts() {
		if (uniqueConsts == null)
			uniqueConsts = new ArrayList<UniqueModel>();
		return uniqueConsts;
	}

	public void setUniqueConsts(List<UniqueModel> uniqueConsts) {
		checkReadOnly();
		this.uniqueConsts = uniqueConsts;
	}

	public List<IdGenerator> getIdGenerators() {
		if (idGenerators == null)
			idGenerators = new ArrayList<IdGenerator>();
		return idGenerators;
	}

	public void setIdGenerators(List<IdGenerator> idGenerators) {
		checkReadOnly();
		this.idGenerators = idGenerators;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<?> entityClass) {
		checkReadOnly();
		this.entityClass = entityClass;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

}
