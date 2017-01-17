package com.github.drinkjava2.jsqlbox;

import java.util.List;

/**
 * Each entity bean class should extends from EntityBase class(forDK7 and below) or implements Entity interface(For
 * JDK8+)
 * 
 */
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

	public default void insert() {
		this.box().insert();
	}

	public default void update() {
		this.box().update();
	}

	public default void delete() {
		this.box().delete();
	}

	public default String fieldID(String realColumnName) {
		return this.box().fieldID(realColumnName);
	}

	public default String alias(String realColumnName) {
		return this.box().getAlias() + "_" + realColumnName;
	}

	public default Entity configAlias(String tableAlias) {
		this.box().configAlias(tableAlias);
		return this;
	}

	public default Entity addNode(Entity entity) {
		this.box().addNode(entity);
		return this;
	}

	public default List<Entity> getList(int index) {
		return null;// TODO work on it
	}

}