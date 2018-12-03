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

import com.github.drinkjava2.jdbpro.LinkArrayList;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

/**
 * For one field with @Convert annotation, define how it customize SQL, read and
 * write to/from entity bean
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public interface FieldConverter {

	/**
	 * handle SQL, can insert/modify sqlBody and sqlWhere items
	 * 
	 * @param sqlOption
	 *            can be INSERT/UPDATE/DELETE/LOAD
	 * @param ctx
	 *            the SqlBoxContext instance
	 * @param col
	 *            the ColumnModel
	 * @param sqlBody
	 *            the Sql "Body" part, this is a LinkStyleArrayList
	 * @param sqlWhere
	 *            the Sql "where" part, this is a LinkStyleArrayList
	 */
	public void handleSQL(SqlOption sqlOption, SqlBoxContext ctx, ColumnModel col, Object entity,
			LinkArrayList<Object> sqlBody, LinkArrayList<Object> sqlWhere);

	/**
	 * Read value from entity Bean field, convert to Database value so can write to
	 * database
	 * 
	 * @param col
	 *            the ColumnModel
	 * @param entity
	 *            the entity Bean
	 * @return the value read from bean or tail
	 */
	public Object entityFieldToDbValue(ColumnModel col, Object entity);

	/**
	 * A value loaded from Database, write it to Entity's correspond field
	 * 
	 * @param entity
	 *            the entity Bean
	 * @param col
	 *            the ColumnModel
	 * @param value
	 *            the value will be written to entity or tail
	 */
	public void writeDbValueToEntityField(Object entity, ColumnModel col, Object value);
}