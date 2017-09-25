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

import java.util.List;

import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * DebugUtils only for debug, will delete it
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public abstract class DebugUtils {

	public static String getColumnModelDebugInfo(ColumnModel c) {
		StringBuilder sb = new StringBuilder();
		sb.append("columnName=" + c.getColumnName()).append(", ");
		sb.append("type=" + c.getColumnType()).append(", ");
		sb.append("pkey=" + c.getPkey()).append(", ");
		sb.append("lengths=");
		if (c.getLengths() != null)
			for (Integer length : c.getLengths())
				sb.append(length).append(", ");
		sb.append("pojoField=" + c.getPojoField());
		return sb.toString();
	}

	public static String getTableModelDebugInfo(TableModel model) {
		StringBuilder sb = new StringBuilder();
		sb.append("\rtableName=" + model.getTableName()).append("\r");
		sb.append("pojoClass=" + model.getPojoClass()).append("\r");
		List<ColumnModel> columns = model.getColumns();
		for (ColumnModel column : columns)
			sb.append(getColumnModelDebugInfo(column)).append("\r");
		return sb.toString();

	}
}
