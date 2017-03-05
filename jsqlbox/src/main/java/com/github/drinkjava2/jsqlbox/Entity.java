package com.github.drinkjava2.jsqlbox;

import java.util.Set;

/**
 * Each entity bean should extends from EntityBase class (for DK7 and below) or simply implements Entity interface(For
 * JDK8+), This project only released JDK8 version, for JDK7 and below which does support "default" method in interface,
 * so need move all methods in Entity to EntityBase class and re-compile source code, that's the only work need to do.
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
		this.box().insert();
		return (T) this;
	}

	public default <T> T update() {
		this.box().update();
		return (T) this;
	}

	public default <T> T delete() {
		this.box().delete();
		return (T) this;
	}

	public default String fieldID(String realColumnName) {
		return this.box().fieldID(realColumnName);
	}

	public default String alias(String realColumnName) {
		return this.box().aliasByRealColumnName(realColumnName);
	}

	public default String aliasByFieldID(String fieldID) {
		return this.box().aliasByFieldID(fieldID);
	}

	public default <T> T configAlias(String tableAlias) {
		this.box().configAlias(tableAlias);
		return (T) this;
	}

	public default String configMapping(String... mappingSql) {
		return this.box().configMapping(mappingSql);
	}

	public default String automaticQuerySQL() {
		return this.box().automaticQuerySQL();
	}

	public default <T> Set<T> getUniqueNodeSet(Class<?> entityClass) {
		return (Set<T>) this.box().getUniqueNodeSet(entityClass);
	}

	public default <T> Set<T> getChildNodeSet(Class<?> entityClass, String fieldID) {
		return this.box().getChildNodeSet(entityClass, fieldID);
	}

	public default <T> Set<T> getChildNodeSet(Class<?> entityClass) {
		return this.box().getChildNodeSet(entityClass, null);
	}

	public default <T> T getChildNode(Class<?> entityClass, String fieldID) {
		return this.box().getChildNode(entityClass, fieldID);
	}

	public default <T> T getChildNode(Class<?> entityClass) {
		return this.box().getChildNode(entityClass, null);
	}

	public default <T> T getParentNode(Class<?> entityClass) {
		return this.box().getParentNode(entityClass);
	}

}