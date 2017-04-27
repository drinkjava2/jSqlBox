package com.github.drinkjava2.jsqlbox;

import java.util.Set;

/**
 * Each entity bean should extends from EntityBase class (for DK7 and below) or
 * simply implements Entity interface(For JDK8+), This project only released
 * JDK8 version, for JDK7 and below which does support "default" method in
 * interface, so need move all methods in Entity to EntityBase class and
 * re-compile source code, that's the only work need to do.
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public interface Entity {

	public default SqlBox box() {
		return SqlBoxContext.getDefaultBox(this);
	}

	public default SqlBox box(SqlBoxContext context) {
		return context.getBox(this);
	}

	public default String table() {
		return box().table();
	}

	public default String all() {
		return box().all();
	}

	public default <T> T insert() {
		box().insert();
		return (T) this;
	}

	public default <T> T update() {
		box().update();
		return (T) this;
	}

	public default <T> T delete() {
		box().delete();
		return (T) this;
	}

	public default String nextUUID() {
		SqlBoxContext ctx = box().getSqlBoxContext();
		return (String) ctx.getDefaultUUIDGenerator().getNextID(ctx);
	}

	public default String fieldID(String realColumnName) {
		return box().fieldID(realColumnName);
	}

	public default String alias(String realColumnName) {
		return box().aliasByRealColumnName(realColumnName);
	}

	public default String aliasByFieldID(String fieldID) {
		return box().aliasByFieldID(fieldID);
	}

	public default <T> T configAlias(String tableAlias) {
		box().configAlias(tableAlias);
		return (T) this;
	}

	public default String configMapping(String... mappingSql) {
		return box().configMapping(mappingSql);
	}

	public default String automaticQuerySQL() {
		return box().automaticQuerySQL();
	}

	public default <T> Set<T> getUniqueNodeSet(Class<?> entityClass) {
		return (Set<T>) box().getUniqueNodeSet(entityClass);
	}

	public default <T> Set<T> getChildNodeSet(Class<?> entityClass, String fieldID) {
		return box().getChildNodeSet(entityClass, fieldID);
	}

	public default <T> Set<T> getChildNodeSet(Class<?> entityClass) {
		return box().getChildNodeSet(entityClass, null);
	}

	public default <T> T getChildNode(Class<?> entityClass, String fieldID) {
		return box().getChildNode(entityClass, fieldID);
	}

	public default <T> T getChildNode(Class<?> entityClass) {
		return box().getChildNode(entityClass, null);
	}

	public default <T> T getParentNode(Class<?> entityClass) {
		return box().getParentNode(entityClass);
	}

	public default String pagination(int pageNumber, int pageSize, String... sql) {
		StringBuilder sb = new StringBuilder();
		for (String str : sql)
			sb.append(str);
		return box().getDialect().paginate(pageNumber, pageSize, sb.toString());
	}

}