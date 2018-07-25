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
package com.github.drinkjava2.jsqlbox;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlItem;

/**
 * If a entity class implements ActiveRecordSupport interface, it will have CRUD
 * Method. This interface is designed for jSqlBox Java8 version because Java8
 * support default method interface is not useful for Java7 and below.
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public interface ActiveRecordSupport<T> {// NOSONAR

	static final ThreadLocal<String[]> lastTimePutFieldsCache = new ThreadLocal<String[]>();

	/** @return current SqlBoxContext instance */
	public SqlBoxContext ctx(Object... optionItems);

	/** Insert entity to database, if not 1 row updated, throw SqlBoxException */
	public T insert(Object... optionItems);

	/** Update entity in database, if not 1 row updated, throw SqlBoxException */
	public T update(Object... optionItems);

	/** Update entity in database, return how many rows affected */
	public int updateTry(Object... optionItems);

	/** Delete entity in database, if not 1 row deleted, throw SqlBoxException */
	public void delete(Object... optionItems);

	/** Delete entity in database, return how many rows affected */
	public int deleteTry(Object... optionItems);

	/** Delete entity by given id, if not 1 row deleted, throw SqlBoxException */
	public void deleteById(Object id, Object... optionItems);

	/** Delete entity by given id, return how many rows deleted */
	public int deleteByIdTry(Object id, Object... optionItems);

	/** Check if entity exist by its id */
	public boolean exist(Object... optionItems);

	/** Check if entity exist by given id */
	public boolean existById(Object id, Object... optionItems);

	/** Return how many records for current entity class */
	public int countAll(Object... optionItems);

	/** Load entity according its id, if not 1 row round, throw SqlBoxException */
	public T load(Object... optionItems);

	/** Load entity according its id, return how many rows found */
	public int loadTry(Object... optionItems);

	/** Load entity by given id, if not 1 row found, throw SqlBoxException */
	public T loadById(Object id, Object... optionItems);

	/** Load entity by given id, if not found, return null */
	public T loadByIdTry(Object id, Object... optionItems);

	/** Find all entity of same entity class, if not found, return empty list */
	public List<T> findAll(Object... optionItems);

	/** Find all entity according its id, if not found, return empty list */
	public List<T> findByIds(Iterable<?> ids, Object... optionItems);

	/** Find entity according SQL, if not found, return empty list */
	public List<T> findBySQL(Object... optionItems);

	/**
	 * Find entity according a sample bean, ignore null fields, if not found, return
	 * empty list
	 */
	public List<T> findBySample(Object sampleBean, Object... optionItems);

	/** Find one related entity */
	public <E> E findOneRelated(Object... sqlItems);

	/** Find related entity List */
	public <E> List<E> findRelatedList(Object... sqlItems);

	/** Find related entity Set */
	public <E> Set<E> findRelatedSet(Object... sqlItems);

	/** Find related entity Map */
	public <E> Map<Object, E> findRelatedMap(Object... sqlItems);

	/**
	 * Link style set values for entity field, format like:
	 * user.put("id","id1").put("name","Sam").put("address","Beijing","phone","12345",
	 * "email","abc@123.com")
	 */
	public T put(Object... fieldAndValues);

	/** Cache a field array in ThreadLocal for putValues method use */
	public T putFields(String... fieldNames);

	/**
	 * Put values for entity fields, field names should be cached by call putFields
	 * method first
	 */
	public T putValues(Object... values);

	/**
	 * In SqlMapper style, based on current method @Sql annotated String or Text(see
	 * user manual) in comments(need put Java file in resources folder, see user
	 * manual) and parameters, guess a best fit query/update/delete/execute method
	 * and run it
	 */
	public <U> U guess(Object... params);

	/**
	 * In SqlMapper style, return current method's SQL String based on current
	 * method @Sql annotated String or Text(see user manual) in comments(need put
	 * Java file in resources folder, see user manual)
	 */
	public String guessSQL();

	/** In SqlMapper style, return current method's prepared SQL */
	public PreparedSQL guessPreparedSQL(Object... params);

	/**
	 * For tXxxx style templateEngine use, return a SqlItemType.PUT type SqlItem
	 * instance,
	 * 
	 * Usage: put("key1",value1,"key2",value2...);
	 */
	public SqlItem bind(Object... parameters);

	/** Return current entity's shardTable according its sharding key values */
	public String shardTB(Object... optionItems);

	/** Return current entity's shardDatabase according its sharding key values */
	public SqlBoxContext shardDB(Object... optionItems);

}