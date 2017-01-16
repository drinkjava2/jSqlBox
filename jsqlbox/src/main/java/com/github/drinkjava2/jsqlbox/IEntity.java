package com.github.drinkjava2.jsqlbox;

import java.util.List;

/**
 * Each entity bean class should extends from EntityBase class <br/>
 * But for some reason if don't want extends from EntityBase class, just copy all fields and methods in EntityBase class
 * to your entity bean class, IEntity only works for JDK8+
 * 
 */
public interface IEntity {

	public default SqlBox box() {
		return SqlBox.getBox(this);
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

	public default IEntity configAlias(String tableAlias) {
		this.box().configAlias(tableAlias);
		return this;
	}

	public default IEntity addNode(IEntity entity) {
		this.box().addNode(entity);
		return this;
	}

	public default List<IEntity> getList(int index) {
		return null;// TODO
	}

}