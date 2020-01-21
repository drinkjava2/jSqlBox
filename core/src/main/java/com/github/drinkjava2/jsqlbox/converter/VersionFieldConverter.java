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
package com.github.drinkjava2.jsqlbox.converter;

import static com.github.drinkjava2.jdbpro.JDBPRO.param;

import com.github.drinkjava2.jdbpro.LinkArrayList;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.DbContextUtils;
import com.github.drinkjava2.jsqlbox.DbException;

/**
 * TailType has a tails() method return a map instance stored tail values
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */
public class VersionFieldConverter extends BaseFieldConverter {

	/**
	 * If version is null, build the first version value
	 */
	private static Object getFirstVersion(ColumnModel col, Object version) {
		if (version != null)
			return version;
		if (Type.INTEGER.equals(col.getColumnType())) {
			return 0;
		} else if (Type.SMALLINT.equals(col.getColumnType())) {
			return (short) 0;
		} else if (Type.BIGINT.equals(col.getColumnType())) {
			return 0l;
		} else
			throw new DbException(
					"Can not use " + col.getColumnType() + " as version field for '" + col.getEntityField() + "'");
	}

	private static Object getNextVersion(ColumnModel col, Object version) {
		Object nextVersion;
		if (Type.INTEGER.equals(col.getColumnType())) {
			nextVersion = (Integer) version + 1;
		} else if (Type.SMALLINT.equals(col.getColumnType())) {
			nextVersion = ((Integer) ((Short) version + 1)).shortValue();
		} else if (Type.BIGINT.equals(col.getColumnType())) {
			nextVersion = (Long) version + 1;
		} else
			throw new DbException(
					"Can not use " + col.getColumnType() + " as version field for '" + col.getEntityField() + "'");
		return nextVersion;
	}

	@Override
	public void handleSQL(SqlOption sqlOption, DbContext ctx, ColumnModel col, Object entity,
			LinkArrayList<Object> sqlBody, LinkArrayList<Object> sqlWhere) {
		Object oldVersion = DbContextUtils.readValueFromBeanFieldOrTail(col, entity);
		Object firstOrOldVersion = getFirstVersion(col, oldVersion);
		Object nextVersion = getNextVersion(col, firstOrOldVersion);
		if (SqlOption.UPDATE.equals(sqlOption)) {
			DbContextUtils.writeValueToBeanFieldOrTail(col, entity, nextVersion);
			if (!sqlBody.isEmpty())
				sqlBody.append(", ");
			sqlBody.append(col.getColumnName()).append("=?").append(param(nextVersion));

			if (!sqlWhere.isEmpty())
				sqlWhere.append(" and ");// NOSONAR
			sqlWhere.append(col.getColumnName()).append("=?").append(param(firstOrOldVersion));
		} else if (SqlOption.DELETE.equals(sqlOption)) {
			if (!sqlWhere.isEmpty())
				sqlWhere.append(" and ");// NOSONAR
			sqlWhere.append(col.getColumnName()).append("=?");
			sqlWhere.append(param(firstOrOldVersion));
		} else if (SqlOption.INSERT.equals(sqlOption)) {
			if (oldVersion != firstOrOldVersion)
				DbContextUtils.writeValueToBeanFieldOrTail(col, entity, firstOrOldVersion);
			sqlBody.append(col.getColumnName());
			sqlBody.append(param(firstOrOldVersion)).append(",");
		}
	}

}