/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
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

	/**
	 * The alias name for this table, this field is designed to ORM tool use
	 */
	private String alias;

	public TableModel() {
		super();
	}

	public TableModel(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return a copy of this TableModel
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
		addGenerator(new TableIdGenerator(name, tableName, pkColumnName, valueColumnName, pkColumnValue, initialValue,
				allocationSize));
	}

	/** Add a UUIDAnyGenerator */
	public void uuidAny(String name, Integer length) {
		addGenerator(new UUIDAnyGenerator(name, length));
	}

	/**
	 * Add a "create table..." DDL to generate ID, similar like JPA's TableGen
	 */
	public void addGenerator(IdGenerator generator) {
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
		this.addGenerator(new SequenceIdGenerator(name, sequenceName, initialValue, allocationSize));
	}

	/**
	 * Add a Sequence Generator, note: not all database support sequence
	 */
	public void sortedUUIDGenerator(String name, Integer sortedLength, Integer uuidLength) {
		DialectException.assureNotNull(name);
		if (this.getIdGenerator(GenerationType.SORTED_UUID, name) != null)
			throw new DialectException("Duplicated sortedUUIDGenerator name '" + name + "'");
		idGenerators.add(new SortedUUIDGenerator(name, sortedLength, uuidLength));
	}

	/**
	 * Add the table check, note: not all database support table check
	 */
	public TableModel check(String check) {
		this.check = check;
		return this;
	}

	/**
	 * Add the table comment, note: not all database support table comment
	 */
	public TableModel comment(String comment) {
		this.comment = comment;
		return this;
	}

	/**
	 * Add a ColumnModel
	 */
	public TableModel addColumn(ColumnModel column) {
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
		ColumnModel col = getColumn(columnName);
		if (col != null)
			return col;
		return addColumn(columnName);
	}

	/**
	 * Add a column with given columnName to tableModel
	 * 
	 * @param columnName
	 * @return the Column object
	 */
	public ColumnModel addColumn(String columnName) {
		DialectException.assureNotEmpty(columnName, "columnName can not be empty");
		for (ColumnModel columnModel : columns)
			if (columnName.equalsIgnoreCase(columnModel.getColumnName()))
				throw new DialectException("ColumnModel name '" + columnName + "' already existed");
		ColumnModel column = new ColumnModel(columnName);
		addColumn(column);
		return column;
	}

	/**
	 * Return ColumnModel object by columnName, if not found, return null;
	 */
	public ColumnModel getColumn(String columnName) {
		for (ColumnModel columnModel : columns)
			if (columnModel.getColumnName() != null && columnModel.getColumnName().equalsIgnoreCase(columnName))
				return columnModel;
		return null;
	}

	/**
	 * Return ColumnModel object by columnName, if not found, return null;
	 */
	public ColumnModel getColumnByColOrEntityFieldName(String colOrFieldName) {
		for (ColumnModel columnModel : columns) {
			if (columnModel.getColumnName() != null && columnModel.getColumnName().equalsIgnoreCase(colOrFieldName))
				return columnModel;
			if (columnModel.getEntityField() != null && columnModel.getEntityField().equalsIgnoreCase(colOrFieldName))
				return columnModel;
		}
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
		FKeyModel fkey = new FKeyModel();
		fkey.setTableName(this.tableName);
		getFkeyConstraints().add(fkey);
		return fkey;
	}

	/**
	 * Start add a foreign key definition in DDL, detail usage see demo
	 */
	public FKeyModel fkey(String fkeyName) {
		FKeyModel fkey = new FKeyModel();
		fkey.setTableName(this.tableName);
		fkey.setFkeyName(fkeyName);
		getFkeyConstraints().add(fkey);
		return fkey;
	}

	/**
	 * Get a FKeyModel by given fkeyName
	 */
	public FKeyModel getFkey(String fkeyName) {
		for (FKeyModel fkey : fkeyConstraints)
			if (!StrUtils.isEmpty(fkeyName) && fkeyName.equalsIgnoreCase(fkey.getFkeyName()))
				return fkey;
		return null;
	}

	/**
	 * Start add a Index in DDL, detail usage see demo
	 */
	public IndexModel index() {
		IndexModel index = new IndexModel();
		getIndexConsts().add(index);
		return index;
	}

	/**
	 * Start add a Index in DDL, detail usage see demo
	 */
	public IndexModel index(String indexName) {
		IndexModel index = new IndexModel();
		index.setName(indexName);
		getIndexConsts().add(index);
		return index;
	}

	/**
	 * Start add a unique constraint in DDL, detail usage see demo
	 */
	public UniqueModel unique() {
		UniqueModel unique = new UniqueModel();
		getUniqueConsts().add(unique);
		return unique;
	}

	/**
	 * Start add a unique constraint in DDL, detail usage see demo
	 */
	public UniqueModel unique(String uniqueName) {
		UniqueModel unique = new UniqueModel();
		unique.setName(uniqueName);
		getUniqueConsts().add(unique);
		return unique;
	}

	/**
	 * If support engine like MySQL or MariaDB, add engineTail at the end of "create
	 * table..." DDL, usually used to set encode String like " DEFAULT CHARSET=utf8"
	 * for MySQL
	 */
	public TableModel engineTail(String engineTail) {
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
	// getter & setter=========================

	protected void getAndSetters____________________________() {// NOSONAR
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getCheck() {
		return check;
	}

	public void setCheck(String check) {
		this.check = check;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<ColumnModel> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnModel> columns) {
		this.columns = columns;
	}

	public List<FKeyModel> getFkeyConstraints() {
		if (fkeyConstraints == null)
			fkeyConstraints = new ArrayList<FKeyModel>();
		return fkeyConstraints;
	}

	public void setFkeyConstraints(List<FKeyModel> fkeyConstraints) {
		this.fkeyConstraints = fkeyConstraints;
	}

	public String getEngineTail() {
		return engineTail;
	}

	public void setEngineTail(String engineTail) {
		this.engineTail = engineTail;
	}

	public List<IndexModel> getIndexConsts() {
		if (indexConsts == null)
			indexConsts = new ArrayList<IndexModel>();
		return indexConsts;
	}

	public void setIndexConsts(List<IndexModel> indexConsts) {
		this.indexConsts = indexConsts;
	}

	public List<UniqueModel> getUniqueConsts() {
		if (uniqueConsts == null)
			uniqueConsts = new ArrayList<UniqueModel>();
		return uniqueConsts;
	}

	public void setUniqueConsts(List<UniqueModel> uniqueConsts) {
		this.uniqueConsts = uniqueConsts;
	}

	public List<IdGenerator> getIdGenerators() {
		if (idGenerators == null)
			idGenerators = new ArrayList<IdGenerator>();
		return idGenerators;
	}

	public void setIdGenerators(List<IdGenerator> idGenerators) {
		this.idGenerators = idGenerators;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
}
