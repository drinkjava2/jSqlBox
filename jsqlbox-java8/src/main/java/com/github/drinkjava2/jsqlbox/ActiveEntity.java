package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdbpro.SqlOption;
import com.github.drinkjava2.jdialects.ArrayUtils;
import com.github.drinkjava2.jdialects.ClassCacheUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * ActiveEntity is a interface has default methods only supported for Java8+, so
 * in Java8 and above, a POJO can implements ActiveEntity interface to obtain
 * CRUD methods instead of extends ActiveRecord class
 */
@SuppressWarnings("unchecked")
public interface ActiveEntity<T> extends ActiveRecordSupport<T> {

	@Override
	public default SqlBoxContext ctx(Object... optionItems) {
		for (Object item : optionItems)
			if (item != null && item instanceof SqlBoxContext)
				return (SqlBoxContext) item;
		return SqlBoxContext.getGlobalSqlBoxContext();
	}

	@Override
	public default T insert(Object... optionItems) {
		return (T) ctx(optionItems).entityInsert(this, optionItems);
	}

	@Override
	public default T update(Object... optionItems) {
		return ctx(optionItems).entityUpdate(this, optionItems);
	}

	@Override
	public default int updateTry(Object... optionItems) {
		return ctx(optionItems).entityUpdateTry(this, optionItems);
	}

	@Override
	public default void delete(Object... optionItems) {
		ctx(optionItems).entityDelete(this, optionItems);
	}

	@Override
	public default int deleteTry(Object... optionItems) {
		return ctx(optionItems).entityDeleteTry(this, optionItems);
	}

	@Override
	public default void deleteById(Object id, Object... optionItems) {
		ctx(optionItems).entityDeleteById(this.getClass(), id, optionItems);
	}

	@Override
	public default int deleteByIdTry(Object id, Object... optionItems) {
		return ctx(optionItems).entityDeleteByIdTry(this.getClass(), id, optionItems);
	}

	@Override
	public default boolean exist(Object... optionItems) {
		return ctx(optionItems).entityExist(this, optionItems);
	}

	@Override
	public default boolean existById(Object id, Object... optionItems) {
		return ctx(optionItems).entityExistById(this.getClass(), id, optionItems);
	}

	@Override
	public default T load(Object... optionItems) {
		return (T) ctx(optionItems).entityLoad(this, optionItems);
	}

	@Override
	public default int loadTry(Object... optionItems) {
		return ctx(optionItems).entityLoadTry(this, optionItems);
	}

	@Override
	public default T loadById(Object id, Object... optionItems) {
		return (T) ctx(optionItems).entityLoadById(this.getClass(), id, optionItems);
	}

	@Override
	public default T loadByIdTry(Object id, Object... optionItems) {
		return (T) ctx(optionItems).entityLoadByIdTry(this.getClass(), id, optionItems);
	}

	@Override
	public default List<T> findAll(Object... optionItems) {
		return (List<T>) ctx(optionItems).entityFindAll(this.getClass(), optionItems);
	}

	@Override
	public default List<T> findByIds(Iterable<?> ids, Object... optionItems) {
		return (List<T>) ctx(optionItems).entityFindByIds(this.getClass(), ids, optionItems);
	}

	@Override
	public default List<T> findBySQL(Object... optionItems) {
		return (List<T>) ctx(optionItems).iQueryForEntityList(this.getClass(), optionItems);
	}

	@Override
	public default List<T> findBySample(Object sampleBean, Object... optionItems) {
		return ctx(optionItems).entityFindBySample(sampleBean, optionItems);
	}

	static Object[] insertThisClassIfNotHave(Object entity, Object... optionItems) {
		Object[] items = optionItems;
		TableModel[] models = SqlBoxContextUtils.findAllModels(optionItems);
		if (models.length == 0)
			throw new SqlBoxException("No TableMode found for entity.");
		TableModel model = models[0];
		if (!entity.getClass().equals(model.getEntityClass())) {// NOSONAR
			model = TableModelUtils.entity2ReadOnlyModel(entity.getClass());
			items = ArrayUtils.insertArray(model, items);
		}
		return items;
	}

	@Override
	public default <E> E findOneRelated(Object... optionItems) {
		Object[] items = insertThisClassIfNotHave(this, optionItems);
		return ctx(optionItems).entityFindRelatedOne(this, items);
	}

	@Override
	public default <E> List<E> findRelatedList(Object... optionItems) {
		Object[] items = insertThisClassIfNotHave(this, optionItems);
		return ctx(optionItems).entityFindRelatedList(this, items);
	}

	@Override
	public default <E> Set<E> findRelatedSet(Object... optionItems) {
		Object[] items = insertThisClassIfNotHave(this, optionItems);
		return ctx(optionItems).entityFindRelatedSet(this, items);
	}

	@Override
	public default <E> Map<Object, E> findRelatedMap(Object... optionItems) {
		Object[] items = insertThisClassIfNotHave(this, optionItems);
		return ctx(optionItems).entityFindRelatedMap(this, items);
	}

	@Override
	public default int countAll(Object... optionItems) {
		return ctx(optionItems).entityCountAll(this.getClass(), optionItems);
	}

	@Override
	public default T put(Object... fieldAndValues) {
		for (int i = 0; i < fieldAndValues.length / 2; i++) {
			String field = (String) fieldAndValues[i * 2];
			Object value = fieldAndValues[i * 2 + 1];
			Method writeMethod = ClassCacheUtils.getClassFieldWriteMethod(this.getClass(), field);
			try {
				writeMethod.invoke(this, value);
			} catch (Exception e) {
				throw new SqlBoxException(e);
			}
		}
		return (T) this;
	}

	@Override
	public default T putFields(String... fieldNames) {
		lastTimePutFieldsCache.set(fieldNames);
		return (T) this;
	}

	@Override
	public default T putValues(Object... values) {
		String[] fields = lastTimePutFieldsCache.get();
		if (values.length == 0 || fields == null || fields.length == 0)
			throw new SqlBoxException("putValues fields or values can not be empty");
		if (values.length != fields.length)
			throw new SqlBoxException("putValues fields and values number not match");
		for (int i = 0; i < fields.length; i++) {
			Method writeMethod = ClassCacheUtils.getClassFieldWriteMethod(this.getClass(), fields[i]);
			if (writeMethod == null)
				throw new SqlBoxException(
						"Not found writeMethod for '" + this.getClass() + "' class's method '" + fields[i] + "'");
			try {
				writeMethod.invoke(this, values[i]);
			} catch (Exception e) {
				throw new SqlBoxException(e);
			}
		}
		return (T) this;
	}

	@Override
	public default <U> U guess(Object... params) {// NOSONAR
		return ctx().getSqlMapperGuesser().guess(ctx(), this, params);
	}

	@Override
	public default String guessSQL() {
		return ctx().getSqlMapperGuesser().guessSQL(ctx(), this);
	}

	@Override
	public default PreparedSQL guessPreparedSQL(Object... params) {
		return ctx().getSqlMapperGuesser().doGuessPreparedSQL(ctx(), this, params);
	}

	@Override
	public default SqlItem bind(Object... parameters) {
		return new SqlItem(SqlOption.BIND, parameters);
	}

	@Override
	public default String shardTB(Object... optionItems) {
		TableModel model = SqlBoxContextUtils.findTableModel(this.getClass(), optionItems);
		ColumnModel col = model.getShardTableColumn();
		if (col == null || col.getShardTable() == null || col.getShardTable().length == 0)
			throw new SqlBoxException("Not found ShardTable setting for '" + model.getEntityClass() + "'");
		Object shardKey1 = ClassCacheUtils.readValueFromBeanField(this, col.getColumnName());
		return SqlBoxContextUtils.getShardedTB(ctx(), model.getEntityClass(), shardKey1);
	}

	@Override
	public default SqlBoxContext shardDB(Object... optionItems) {
		TableModel model = SqlBoxContextUtils.findTableModel(this.getClass(), optionItems);
		ColumnModel col = model.getShardDatabaseColumn();
		if (col == null || col.getShardDatabase() == null || col.getShardDatabase().length == 0)
			throw new SqlBoxException("Not found ShardTable setting for '" + model.getEntityClass() + "'");
		Object shardKey1 = ClassCacheUtils.readValueFromBeanField(this, col.getColumnName());
		return SqlBoxContextUtils.getShardedDB(ctx(), model.getEntityClass(), shardKey1);
	}
}