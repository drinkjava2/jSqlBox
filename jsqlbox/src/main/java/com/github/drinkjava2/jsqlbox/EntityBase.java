package com.github.drinkjava2.jsqlbox;

import java.util.Set;

/**
 * Each entity bean class should extends from EntityBase or implements Entity
 * interface <br/>
 * But for some reason if don't want implements EntityBase class, just copy all
 * methods in EntityBase class to entity bean class
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class EntityBase implements Entity {
	@Override
	public SqlBox box() {
		return SqlBoxContext.getDefaultBox(this);
	}

	@Override
	public SqlBox box(SqlBoxContext context) {
		return context.getBox(this);
	}

	@Override
	public String table() {
		return box().table();
	}

	@Override
	public String all() {
		return box().all();
	}

	@Override
	public <T> T insert() {
		box().insert();
		return (T) this;
	}

	@Override
	public <T> T update() {
		box().update();
		return (T) this;
	}

	@Override
	public <T> T delete() {
		box().delete();
		return (T) this;
	}

	@Override
	public String nextUUID() {
		SqlBoxContext ctx = box().getSqlBoxContext();
		return (String) ctx.getDefaultUUIDGenerator().getNextID(null);
	}

	@Override
	public String fieldID(String realColumnName) {
		return box().fieldID(realColumnName);
	}

	@Override
	public String alias(String realColumnName) {
		return box().aliasByRealColumnName(realColumnName);
	}

	@Override
	public String aliasByFieldID(String fieldID) {
		return box().aliasByFieldID(fieldID);
	}

	@Override
	public <T> T configAlias(String tableAlias) {
		box().configAlias(tableAlias);
		return (T) this;
	}

	@Override
	public String configMapping(String... mappingSql) {
		return box().configMapping(mappingSql);
	}

	@Override
	public String automaticQuerySQL() {
		return box().automaticQuerySQL();
	}

	@Override
	public <T> Set<T> getUniqueNodeSet(Class<?> entityClass) {
		return (Set<T>) box().getUniqueNodeSet(entityClass);
	}

	@Override
	public <T> Set<T> getChildNodeSet(Class<?> entityClass, String fieldID) {
		return box().getChildNodeSet(entityClass, fieldID);
	}

	@Override
	public <T> Set<T> getChildNodeSet(Class<?> entityClass) {
		return box().getChildNodeSet(entityClass, null);
	}

	@Override
	public <T> T getChildNode(Class<?> entityClass, String fieldID) {
		return box().getChildNode(entityClass, fieldID);
	}

	@Override
	public <T> T getChildNode(Class<?> entityClass) {
		return box().getChildNode(entityClass, null);
	}

	@Override
	public <T> T getParentNode(Class<?> entityClass) {
		return box().getParentNode(entityClass);
	}

	@Override
	public String paginate(int pageNumber, int pageSize, String... sql) {
		StringBuilder sb = new StringBuilder();
		for (String str : sql)
			sb.append(str);
		return box().getDialect().paginate(pageNumber, pageSize, sb.toString());
	}

}