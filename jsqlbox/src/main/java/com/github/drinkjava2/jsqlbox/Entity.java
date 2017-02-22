package com.github.drinkjava2.jsqlbox;

import java.util.List;

/**
 * Each entity bean class should extends from EntityBase class(forDK7 and below) or implements Entity interface(For
 * JDK8+)
 * 
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

	public default String automaticQuerySQL() {
		return this.box().automaticQuerySQL();
	}

	public default <T> List<T> getUniqueNodeList(Class<?> entityClass) {
		return this.box().getUniqueNodeList(entityClass);
	}

	public default <T> List<T> getChildNodeList(Class<?> entityClass, String fieldID) {
		return this.box().getChildNodeList(entityClass, fieldID);
	}

	public default <T> List<T> getChildNodeList(Class<?> entityClass) {
		return this.box().getChildNodeList(entityClass, null);
	}

	public default <T> T getOneChildNode(Class<?> entityClass, String fieldID) {
		return this.box().getOneChildNode(entityClass, fieldID);
	}

	public default <T> T getOneChildNode(Class<?> entityClass) {
		return this.box().getOneChildNode(entityClass, null);
	}

}