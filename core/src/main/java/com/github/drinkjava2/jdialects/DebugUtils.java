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

import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.FKeyModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * DebugUtils only for debug, will delete it
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public abstract class DebugUtils {//// NOSONAR

	public static String getColumnModelDebugInfo(ColumnModel c) {
		StringBuilder sb = new StringBuilder();
		sb.append("columnName=" + c.getColumnName()).append(", ");
		sb.append("transient=" + c.getTransientable()).append(", ");
		sb.append("type=" + c.getColumnType()).append(", ");
		sb.append("pkey=" + c.getPkey()).append(", ");
		sb.append("shardTable=" + c.getShardTable()).append(", ");
		sb.append("shardDatabase=" + c.getShardDatabase()).append(", ");
		sb.append("idGenerationType=" + c.getIdGenerationType()).append(", ");
		sb.append("idGeneratorName=" + c.getIdGeneratorName()).append(", ");
		sb.append("idGenerator=" + c.getIdGenerator()).append(", ");
		sb.append("lengths=");
		if (c.getLengths() != null)
			sb.append(Arrays.deepToString(c.getLengths()));
		sb.append(", ");
		sb.append("entityField=" + c.getEntityField()).append(", ");
		sb.append("length=" + c.getLength()).append(", ");
		sb.append("precisio=" + c.getPrecision()).append(", ");
		sb.append("scale" + c.getScale()).append(", ");
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
}
