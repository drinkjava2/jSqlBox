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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * DialectDebugUtils only for debug purpose, to print detail info of dialects,
 * may delete it in future version
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public abstract class DebugUtils {//// NOSONAR

	public static String getColumnModelDebugInfo(ColumnModel c) {
		StringBuilder sb = new StringBuilder();
		sb.append("columnName=" + c.getColumnName()).append(", ");
		sb.append("transient=" + c.getTransientable()).append(", ");
		sb.append("columnType=" + c.getColumnType()).append(", ");
		sb.append("pkey=" + c.getPkey()).append(", ");
		if (c.getShardTable() != null)
			sb.append("shardTable=" + Arrays.deepToString(c.getShardTable())).append(", ");
		else
			sb.append("shardTable=null").append(", ");

		if (c.getShardDatabase() != null)
			sb.append("shardDatabase=" + Arrays.deepToString(c.getShardDatabase())).append(", ");
		else
			sb.append("shardDatabase=null").append(", ");

		sb.append("idGenerationType=" + c.getIdGenerationType()).append(", ");
		sb.append("idGeneratorName=" + c.getIdGeneratorName()).append(", ");
		sb.append("idGenerator=" + c.getIdGenerator()).append(", ");
		sb.append("converterClassOrName=" + c.getConverterClassOrName()).append(", ");
		sb.append("entityField=" + c.getEntityField()).append(", ");
		sb.append("length=" + c.getLength()).append(", ");
		sb.append("precisio=" + c.getPrecision()).append(", ");
		sb.append("scale" + c.getScale()).append(", ");
		sb.append("valueExist=" + c.getValueExist()).append(", ");
		sb.append("value=" + c.getValue()).append(", ");
		sb.append("createTimestamp=" + c.isCreateTimestamp()).append(", ");
		sb.append("updateTimestamp=" + c.isUpdateTimestamp()).append(", ");
		sb.append("createdBy=" + c.isCreatedBy()).append(", ");
		sb.append("lastModifiedBy=" + c.isLastModifiedBy()).append(", ");
		
		return sb.toString();
	}

	public static String getFkeyDebugInfo(TableModel t) {
		StringBuilder sb = new StringBuilder();
		sb.append("Fkeys:\r\n");
		for (FKeyModel k : t.getFkeyConstraints()) {
			sb.append("FkeyName=" + k.getFkeyName());
			sb.append(", ColumnNames=" + k.getColumnNames());
			sb.append(", RefTableAndColumns=" + Arrays.deepToString(k.getRefTableAndColumns()));
			sb.append("\r\n");
		}
		return sb.toString();
	}

	public static String getTableModelDebugInfo(TableModel model) {
		StringBuilder sb = new StringBuilder("\r\n=======================================================\r\n");
		sb.append("tableName=" + model.getTableName()).append("\r\n");
		sb.append("getEntityClass=" + model.getEntityClass()).append("\r\n");
		sb.append("readOnly=" + model.getReadOnly()).append("\r\n");
		sb.append(getFkeyDebugInfo(model));
		List<ColumnModel> columns = model.getColumns();
		sb.append("Columns:\r\n");
		for (ColumnModel column : columns)
			sb.append(getColumnModelDebugInfo(column)).append("\r\n");

		return sb.toString();
	}

	public static String getTableModelsDebugInfo(TableModel[] models) {
		StringBuilder sb = new StringBuilder();
		for (TableModel model : models) {
			sb.append(getTableModelDebugInfo(model));
		}
		return sb.toString();
	}

	public static String getDialectFullInfo(Dialect d) {
		String s = "\r\n\r\n=======Dialect Debug Info======\r\n";
		String r = "\r\n";
		DDLFeatures l = d.ddlFeatures;
		s += "name=" + d.getName() + r;
		s += "===== Dialect pagination templates =====" + r;
		s += "sqlTemplate=" + d.sqlTemplate + r;
		s += "topLimitTemplate=" + d.topLimitTemplate + r;
		s += "===== Dialect DDL feature templates =====" + r;
		s += "addColumnString=" + l.addColumnString + r;
		s += "addColumnSuffixString=" + l.addColumnSuffixString + r;
		s += "addForeignKeyConstraintString=" + l.addForeignKeyConstraintString + r;
		s += "addFKeyRefPkeyString=" + l.addFKeyRefPkeyString + r;
		s += "addPrimaryKeyConstraintString=" + l.addPrimaryKeyConstraintString + r;
		s += "columnComment=" + l.columnComment + r;
		s += "createCatalogCommand=" + l.createCatalogCommand + r;
		s += "createMultisetTableString=" + l.createMultisetTableString + r;
		s += "createPooledSequenceStrings=" + l.createPooledSequenceStrings + r;
		s += "createSchemaCommand=" + l.createSchemaCommand + r;
		s += "createSequenceStrings=" + l.createSequenceStrings + r;
		s += "createTableString=" + l.createTableString + r;
		s += "currentSchemaCommand=" + l.currentSchemaCommand + r;
		s += "dropCatalogCommand=" + l.dropCatalogCommand + r;
		s += "dropForeignKeyString=" + l.dropForeignKeyString + r;
		s += "dropSchemaCommand=" + l.dropSchemaCommand + r;
		s += "dropSequenceStrings=" + l.dropSequenceStrings + r;
		s += "dropTableString=" + l.dropTableString + r;
		s += "hasAlterTable=" + l.hasAlterTable + r;
		s += "hasDataTypeInIdentityColumn=" + l.hasDataTypeInIdentityColumn + r;
		s += "identityColumnString=" + l.identityColumnString + r;
		s += "identityColumnStringBigINT=" + l.identityColumnStringBigINT + r;
		s += "identitySelectString=" + l.identitySelectString + r;
		s += "identitySelectStringBigINT=" + l.identitySelectStringBigINT + r;
		s += "needDropConstraintsBeforeDropTable=" + l.needDropConstraintsBeforeDropTable + r;
		s += "nullColumnString=" + l.nullColumnString + r;
		s += "requiresParensForTupleDistinctCounts=" + l.requiresParensForTupleDistinctCounts + r;
		s += "selectSequenceNextValString=" + l.selectSequenceNextValString + r;
		s += "sequenceNextValString=" + l.sequenceNextValString + r;
		s += "supportsColumnCheck=" + l.supportsColumnCheck + r;
		s += "supportsCommentOn=" + l.supportsCommentOn + r;
		s += "supportsIdentityColumns=" + l.supportsIdentityColumns + r;
		s += "supportsIfExistsAfterConstraintName=" + l.supportsIfExistsAfterConstraintName + r;
		s += "openQuote=" + l.openQuote + r;
		s += "closeQuote=" + l.closeQuote + r;

		s += "===== Dialect type mapping templates =====" + r;
		Map<Type, String> t = d.typeMappings;
		for (Type p : t.keySet())
			s += p + "=" + t.get(p) + r;

		s += "===== Dialect function templates =====" + r;
		Map<String, String> f = d.functions;
		for (String fn : f.keySet())
			s += fn + "=" + f.get(fn) + r;
		s += "======================================";
		return s;
	}

}
