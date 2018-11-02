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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;
import com.github.drinkjava2.jdialects.id.AutoIdGenerator;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.id.SequenceIdGenerator;
import com.github.drinkjava2.jdialects.id.TableIdGenerator;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.IndexModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.model.UniqueModel;

/**
 * To transfer platform-independent model to create DDL String array
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class DDLCreateUtils {// NOSONAR
	private static DialectLogger logger = DialectLogger.getLog(DDLCreateUtils.class);

	/**
	 * Transfer tables to DDL by given dialect and without format it, if want get a
	 * formatted DDL, use DDLFormatter.format(DDLs) method to format it
	 */
	public static String[] toCreateDDL(Dialect dialect, TableModel... tables) {
		// Store mixed DDL String, TableGen Object, SequenceGen Object ...
		List<Object> objectResultList = new ArrayList<Object>();

		for (TableModel table : tables)
			transferTableToObjectList(dialect, table, objectResultList);

		boolean hasAutoIdGenerator = false;
		for (TableModel table : tables) {
			for (ColumnModel column : table.getColumns())
				if (GenerationType.AUTO.equals(column.getIdGenerationType())) {
					hasAutoIdGenerator = true;
					break;
				}
			for (IdGenerator idGens : table.getIdGenerators())
				if (hasAutoIdGenerator || idGens.dependOnAutoIdGenerator()) {
					hasAutoIdGenerator = true;
					break;
				}
		}

		List<String> stringResultList = new ArrayList<String>();
		List<TableIdGenerator> tbGeneratorList = new ArrayList<TableIdGenerator>();
		List<SequenceIdGenerator> sequenceList = new ArrayList<SequenceIdGenerator>();
		List<FKeyModel> fKeyConstraintList = new ArrayList<FKeyModel>();

		for (Object strOrObj : objectResultList) {
			if (!StrUtils.isEmpty(strOrObj)) {
				if (strOrObj instanceof String)
					stringResultList.add((String) strOrObj);
				else if (strOrObj instanceof TableIdGenerator)
					tbGeneratorList.add((TableIdGenerator) strOrObj);
				else if (strOrObj instanceof SequenceIdGenerator)
					sequenceList.add((SequenceIdGenerator) strOrObj);
				else if (strOrObj instanceof FKeyModel)
					fKeyConstraintList.add((FKeyModel) strOrObj);
			}
		}

		if (hasAutoIdGenerator) {
			IdGenerator realIdGen = AutoIdGenerator.INSTANCE.getSequenceOrTableIdGenerator(dialect);
			if (realIdGen instanceof TableIdGenerator)
				tbGeneratorList.add((TableIdGenerator) realIdGen);
			else if (realIdGen instanceof SequenceIdGenerator)
				sequenceList.add((SequenceIdGenerator) realIdGen);
			else
				throw new DialectException("Unknow exception happen for realIdGen, please report this bug");
		}

		buildSequenceDDL(dialect, stringResultList, sequenceList);
		buildTableGeneratorDDL(dialect, stringResultList, tbGeneratorList);
		outputFKeyConstraintDDL(dialect, stringResultList, fKeyConstraintList);
		String[] result = stringResultList.toArray(new String[stringResultList.size()]);
		if (Dialect.getGlobalAllowShowSql())
			Dialect.logger.info("Create DDL:\r" + StrUtils.arrayToString(result, "\r"));
		return result;
	}

	/**
	 * Transfer table to a mixed DDL String or TableGen Object list
	 */
	/**
	 * @param dialect
	 * @param t
	 * @param objectResultList
	 */
	private static void transferTableToObjectList(Dialect dialect, TableModel t, List<Object> objectResultList) {
		DDLFeatures features = dialect.ddlFeatures;

		StringBuilder buf = new StringBuilder();
		boolean hasPkey = false;
		String pkeys = "";
		String tableName = t.getTableName();
		List<ColumnModel> columns = t.getColumns();

		// Reserved words check
		dialect.checkNotEmptyReservedWords(tableName, "Table name", tableName);

		List<IndexModel> idexChks = t.getIndexConsts();// check index names
		if (idexChks != null && !idexChks.isEmpty())
			for (IndexModel index : idexChks)
				dialect.checkReservedWords(index.getName());

		List<UniqueModel> ukChks = t.getUniqueConsts();// check unique names
		if (ukChks != null && !ukChks.isEmpty())
			for (UniqueModel unique : ukChks)
				dialect.checkReservedWords(unique.getName());

		List<FKeyModel> fkeyChks = t.getFkeyConstraints();// check Fkey names
		if (fkeyChks != null && !fkeyChks.isEmpty())
			for (FKeyModel fkey : fkeyChks)
				dialect.checkReservedWords(fkey.getFkeyName());

		for (ColumnModel col : columns)// check column names
			if (!col.getTransientable())
				dialect.checkNotEmptyReservedWords(col.getColumnName(), "Column name", tableName);

		// idGenerator
		for (IdGenerator idGen : t.getIdGenerators())
			objectResultList.add(idGen);

		// Foreign key
		for (FKeyModel fkey : t.getFkeyConstraints())
			objectResultList.add(fkey);

		// check and cache prime keys
		for (ColumnModel col : columns) {
			if (col.getTransientable())
				continue;
			if (col.getPkey()) {
				hasPkey = true;
				if (StrUtils.isEmpty(pkeys))
					pkeys = col.getColumnName();
				else
					pkeys += "," + col.getColumnName();
			}
		}

		// create table
		buf.append(hasPkey ? dialect.ddlFeatures.createTableString : dialect.ddlFeatures.createMultisetTableString)
				.append(" ").append(tableName).append(" ( ");

		for (ColumnModel c : columns) {
			if (c.getTransientable())
				continue;
			if (c.getColumnType() == null)
				DialectException
						.throwEX("Type not set on column \"" + c.getColumnName() + "\" at table \"" + tableName + "\"");

			// column definition
			buf.append(c.getColumnName()).append(" ");

			// Identity
			if (GenerationType.IDENTITY.equals(c.getIdGenerationType()) && !features.supportsIdentityColumns)
				DialectException.throwEX("Unsupported identity setting for dialect \"" + dialect + "\" on column \""
						+ c.getColumnName() + "\" at table \"" + tableName + "\"");

			// Column type definition
			if (GenerationType.IDENTITY.equals(c.getIdGenerationType())) {
				if (features.hasDataTypeInIdentityColumn)
					buf.append(dialect.translateToDDLType(c.getColumnType(), c.getLengths()));
				buf.append(' ');
				if (Type.BIGINT.equals(c.getColumnType()))
					buf.append(features.identityColumnStringBigINT);
				else
					buf.append(features.identityColumnString);
			} else {
				buf.append(dialect.translateToDDLType(c.getColumnType(), c.getLengths()));

				// Default
				String defaultValue = c.getDefaultValue();
				if (defaultValue != null) {
					buf.append(" default ").append(defaultValue);
				}

				// Not null
				if (!c.getNullable())
					buf.append(" not null");
				else
					buf.append(features.nullColumnString);
			}

			// Check
			if (!StrUtils.isEmpty(c.getCheck())) {
				if (features.supportsColumnCheck)
					buf.append(" check (").append(c.getCheck()).append(")");
				else
					logger.warn("Ignore unsupported check setting for dialect \"" + dialect + "\" on column \""
							+ c.getColumnName() + "\" at table \"" + tableName + "\" with value: " + c.getCheck());
			}

			// Comments
			if (c.getComment() != null) {
				if (StrUtils.isEmpty(features.columnComment) && !features.supportsCommentOn)
					logger.warn("Ignore unsupported comment setting for dialect \"" + dialect + "\" on column \""
							+ c.getColumnName() + "\" at table \"" + tableName + "\" with value: " + c.getComment());
				else
					buf.append(StrUtils.replace(features.columnComment, "_COMMENT", c.getComment()));
			}

			// tail String
			if (!StrUtils.isEmpty(c.getTail()))
				buf.append(c.getTail());

			buf.append(",");
		}
		// PKEY
		if (!StrUtils.isEmpty(pkeys)) {
			buf.append(" primary key (").append(pkeys).append("),");
		}

		// Table Check
		if (!StrUtils.isEmpty(t.getCheck())) {
			if (features.supportsTableCheck)
				buf.append(" check (").append(t.getCheck()).append("),");
			else
				logger.warn("Ignore unsupported table check setting for dialect \"" + dialect + "\" on table \""
						+ tableName + "\" with value: " + t.getCheck());
		}

		buf.setLength(buf.length() - 1);
		buf.append(")");

		// Engine for MariaDB & MySql only, for example "engine=innoDB"
		String tableTypeString = features.tableTypeString;
		if (!StrUtils.isEmpty(tableTypeString) && !DDLFeatures.NOT_SUPPORT.equals(tableTypeString)) {
			buf.append(tableTypeString);

			// EngineTail, for example:" DEFAULT CHARSET=utf8"
			if (!StrUtils.isEmpty(t.getEngineTail()))
				buf.append(t.getEngineTail());
		}

		objectResultList.add(buf.toString());

		// table comment on
		if (t.getComment() != null) {
			if (features.supportsCommentOn)
				objectResultList.add("comment on table " + t.getTableName() + " is '" + t.getComment() + "'");
			else
				logger.warn("Ignore unsupported table comment setting for dialect \"" + dialect + "\" on table \""
						+ tableName + "\" with value: " + t.getComment());
		}

		// column comment on
		for (ColumnModel c : columns) {
			if (features.supportsCommentOn && c.getComment() != null && StrUtils.isEmpty(features.columnComment))
				objectResultList.add(
						"comment on column " + tableName + '.' + c.getColumnName() + " is '" + c.getComment() + "'");
		}

		// index
		buildIndexDLL(dialect, objectResultList, t);

		// unique
		buildUniqueDLL(dialect, objectResultList, t);
	}

	/**
	 * if name not found, add <br/>
	 * If name same, but other fields different, throw exception </br>
	 * If name same, and other field same, ignore </br>
	 */
	protected static void checkAndInsertToNotRepeatSeq(Set<SequenceIdGenerator> notRepeatedSeq,
			SequenceIdGenerator seq) {
		DialectException.assureNotEmpty(seq.getName(), "SequenceGen name can not be empty");
		DialectException.assureNotEmpty(seq.getSequenceName(),
				"sequenceName can not be empty of \"" + seq.getName() + "\"");
		boolean canAdd = true;
		for (SequenceIdGenerator not : notRepeatedSeq) {
			if (seq.getName().equals(not.getName())) {
				canAdd = false;
				if (!(seq.getSequenceName().equals(not.getSequenceName())
						&& seq.getInitialValue().equals(not.getInitialValue())
						&& seq.getAllocationSize().equals(not.getAllocationSize())))
					throw new DialectException(
							"In one or more tableModel, duplicated SequenceIdGenerator name '" + seq.getName()
									+ "' but different value of sequenceName/initialValue/allocationSize setting");
			} else {
				if (seq.getSequenceName().equals(not.getSequenceName()))
					throw new DialectException(
							"In one or more tableModel, duplicated SequenceName '" + seq.getSequenceName()
									+ "' found for '" + seq.getName() + "' and '" + not.getName() + "'");
			}
		}
		if (canAdd)
			notRepeatedSeq.add(seq);
	}

	private static void buildSequenceDDL(Dialect dialect, List<String> stringList,
			List<SequenceIdGenerator> sequenceList) {
		Set<SequenceIdGenerator> notRepeatedSequences = new HashSet<SequenceIdGenerator>();
		for (SequenceIdGenerator seq : sequenceList)
			checkAndInsertToNotRepeatSeq(notRepeatedSequences, seq);

		DDLFeatures features = dialect.ddlFeatures;
		for (SequenceIdGenerator seq : notRepeatedSequences) {
			if (!features.supportBasicOrPooledSequence()) {
				DialectException.throwEX("Dialect \"" + dialect + "\" does not support sequence setting on sequence \""
						+ seq.getName() + "\"");
			}
			if (features.supportsPooledSequences) {
				// create sequence _SEQ start with 11 increment by 33
				String pooledSequence = StrUtils.replace(features.createPooledSequenceStrings, "_SEQ",
						seq.getSequenceName());
				pooledSequence = StrUtils.replace(pooledSequence, "11", "" + seq.getInitialValue());
				pooledSequence = StrUtils.replace(pooledSequence, "33", "" + seq.getAllocationSize());
				stringList.add(pooledSequence);
			} else {
				if (seq.getInitialValue() >= 2 || seq.getAllocationSize() >= 2)
					DialectException.throwEX("Dialect \"" + dialect
							+ "\" does not support initialValue and allocationSize setting on sequence \""
							+ seq.getName() + "\", try set initialValue and allocationSize to 1 to fix");
				// "create sequence _SEQ"
				String simepleSeq = StrUtils.replace(features.createSequenceStrings, "_SEQ", seq.getSequenceName());
				stringList.add(simepleSeq);
			}
		}

	}

	/**
	 * if name not found, add <br/>
	 * If name same, but other fields different, throw exception </br>
	 * If name same, and other field same, ignore </br>
	 */
	protected static void checkAndInsertToNotRepeatTable(Set<TableIdGenerator> notRepeatedSeq, TableIdGenerator tab) {
		DialectException.assureNotEmpty(tab.getName(), "TableGen name can not be empty");
		DialectException.assureNotEmpty(tab.getTable(),
				"TableGen tableName can not be empty of \"" + tab.getName() + "\"");
		DialectException.assureNotEmpty(tab.getPkColumnName(),
				"TableGen pkColumnName can not be empty of \"" + tab.getName() + "\"");
		DialectException.assureNotEmpty(tab.getPkColumnValue(),
				"TableGen pkColumnValue can not be empty of \"" + tab.getName() + "\"");
		DialectException.assureNotEmpty(tab.getValueColumnName(),
				"TableGen valueColumnName can not be empty of \"" + tab.getName() + "\"");
		boolean canAdd = true;
		for (TableIdGenerator not : notRepeatedSeq) {
			if (tab.getName().equals(not.getName())) {
				canAdd = false;
				if (!(tab.getTable().equals(not.getTable()) && tab.getPkColumnName().equals(not.getPkColumnName())
						&& tab.getPkColumnValue().equals(not.getPkColumnValue())
						&& tab.getValueColumnName().equals(not.getValueColumnName())
						&& tab.getInitialValue().equals(not.getInitialValue())
						&& tab.getAllocationSize().equals(not.getAllocationSize())))
					throw new DialectException("In one or more tableModel, duplicated TableIdGenerator name '"
							+ tab.getName()
							+ "' but different value of table/pKColumnName/pkColumnValue/valueColumnName/initialValue/allocationSize setting");
			}
		}
		if (canAdd)
			notRepeatedSeq.add(tab);
	}

	private static void buildTableGeneratorDDL(Dialect dialect, List<String> stringList,
			List<TableIdGenerator> tbGeneratorList) {
		Set<TableIdGenerator> notRepeatedTab = new HashSet<TableIdGenerator>();

		for (TableIdGenerator tab : tbGeneratorList)
			checkAndInsertToNotRepeatTable(notRepeatedTab, tab);

		Set<String> tableExisted = new HashSet<String>();
		Set<String> columnExisted = new HashSet<String>();
		for (TableIdGenerator tg : tbGeneratorList)
			if (tg.getAllocationSize() != 0) {
				String tableName = tg.getTable().toLowerCase();
				String tableAndPKColumn = tg.getTable().toLowerCase() + "..XXOO.." + tg.getPkColumnName();
				String tableAndValColumn = tg.getTable().toLowerCase() + "..XXOO.." + tg.getValueColumnName();
				if (!tableExisted.contains(tableName)) {
					String s = dialect.ddlFeatures.createTableString + " " + tableName + " (";
					s += tg.getPkColumnName() + " " + dialect.translateToDDLType(Type.VARCHAR, 100) + ",";
					s += tg.getValueColumnName() + " " + dialect.translateToDDLType(Type.BIGINT) + " )";
					stringList.add(s);
					tableExisted.add(tableName);
					columnExisted.add(tableAndPKColumn);
					columnExisted.add(tableAndValColumn);
				} else {
					if (!columnExisted.contains(tableAndPKColumn)) {
						stringList.add("alter table " + tableName + " " + dialect.ddlFeatures.addColumnString + " "
								+ tg.getPkColumnName() + " " + dialect.translateToDDLType(Type.VARCHAR, 100) + " "
								+ dialect.ddlFeatures.addColumnSuffixString);
						columnExisted.add(tableAndPKColumn);
					}
					if (!columnExisted.contains(tableAndValColumn)) {
						stringList.add("alter table " + tableName + " " + dialect.ddlFeatures.addColumnString + " "
								+ tg.getValueColumnName() + " " + dialect.translateToDDLType(Type.VARCHAR, 100) + " "
								+ dialect.ddlFeatures.addColumnSuffixString);
						columnExisted.add(tableAndValColumn);
					}
				}
			}
	}

	private static void outputFKeyConstraintDDL(Dialect dialect, List<String> stringList, List<FKeyModel> trueList) {
		if (DDLFeatures.NOT_SUPPORT.equals(dialect.ddlFeatures.addForeignKeyConstraintString)) {
			logger.warn("Dialect \"" + dialect + "\" does not support foreign key setting, settings be ignored");
			return;
		}
		for (FKeyModel t : trueList) {
			if (!t.getDdl())
				continue; // if ddl is false, skip
			/*
			 * ADD CONSTRAINT _FKEYNAME FOREIGN KEY _FKEYNAME (_FK1, _FK2) REFERENCES
			 * _REFTABLE (_REF1, _REF2)
			 */
			String constName = t.getFkeyName();
			if (StrUtils.isEmpty(constName))
				constName = "fk_" + t.getTableName().toLowerCase() + "_"
						+ StrUtils.replace(StrUtils.listToString(t.getColumnNames()), ",", "_");
			String[] refTableAndColumns = t.getRefTableAndColumns();
			DialectException.assureNotNull(refTableAndColumns);
			String fkeyTemplate;
			if (refTableAndColumns.length == 1)
				fkeyTemplate = dialect.ddlFeatures.addFKeyRefPkeyString;
			else
				fkeyTemplate = dialect.ddlFeatures.addForeignKeyConstraintString;

			fkeyTemplate = StrUtils.replace(fkeyTemplate, "_FK1, _FK2", StrUtils.listToString(t.getColumnNames()));
			fkeyTemplate = StrUtils.replace(fkeyTemplate, "_REF1, _REF2",
					StrUtils.arrayToStringButSkipFirst(t.getRefTableAndColumns()));
			fkeyTemplate = StrUtils.replace(fkeyTemplate, "_REFTABLE", t.getRefTableAndColumns()[0]);
			fkeyTemplate = StrUtils.replace(fkeyTemplate, "_FKEYNAME", constName);
			String tail = StrUtils.isEmpty(t.getFkeyTail()) ? "" : " " + t.getFkeyTail();
			stringList.add("alter table " + t.getTableName() + " " + fkeyTemplate + tail);
		}
	}

	private static void buildIndexDLL(Dialect dialect, List<Object> objectResultList, TableModel t) {
		List<IndexModel> l = t.getIndexConsts();
		if (l == null || l.isEmpty())
			return;
		String template;
		if (Dialect.Teradata14Dialect.equals(dialect))
			template = "create $ifUnique index $indexName ($indexValues) on " + t.getTableName();
		else
			template = "create $ifUnique index $indexName on " + t.getTableName() + " ($indexValues)";
		for (IndexModel index : l) {
			String indexname = index.getName();
			if (StrUtils.isEmpty(indexname))
				indexname = "IX_" + t.getTableName() + "_" + StrUtils.arrayToString(index.getColumnList(), "_");
			String ifUnique = index.getUnique() ? "unique" : "";
			String result = StrUtils.replace(template, "$ifUnique", ifUnique);
			result = StrUtils.replace(result, "$indexName", indexname);
			result = StrUtils.replace(result, "$indexValues", StrUtils.arrayToString(index.getColumnList()));
			objectResultList.add(result);
		}
	}

	private static void buildUniqueDLL(Dialect dialect, List<Object> objectResultList, TableModel t) {
		List<UniqueModel> l = t.getUniqueConsts();
		if (l == null || l.isEmpty())
			return;
		String dialectName = "" + dialect;
		for (UniqueModel unique : l) {
			boolean nullable = false;
			String[] columns = unique.getColumnList();
			for (String colNames : columns) {
				ColumnModel vc = t.getColumnByColName(colNames);
				if (vc != null && vc.getNullable())
					nullable = true;
			}
			String uniqueName = unique.getName();
			if (StrUtils.isEmpty(uniqueName))
				uniqueName = "UK_" + t.getTableName() + "_" + StrUtils.arrayToString(unique.getColumnList(), "_");

			String template = "alter table $TABLE add constraint $UKNAME unique ($COLUMNS)";
			if ((StrUtils.startsWithIgnoreCase(dialectName, "DB2")// DB2 and
																	// DERBY
					|| StrUtils.startsWithIgnoreCase(dialectName, "DERBY")) && nullable)
				template = "create unique index $UKNAME on $TABLE ($COLUMNS)";
			else if (StrUtils.startsWithIgnoreCase(dialectName, "Informix"))
				template = "alter table $TABLE add constraint unique ($COLUMNS) constraint $UKNAME";
			String result = StrUtils.replace(template, "$TABLE", t.getTableName());
			result = StrUtils.replace(result, "$UKNAME", uniqueName);
			result = StrUtils.replace(result, "$COLUMNS", StrUtils.arrayToString(unique.getColumnList()));
			objectResultList.add(result);
		}
	}

}
