package com.github.drinkjava2.jsqlbox;

/**
 * Each entity bean class should extends from EntityBase class <br/>
 * But for some reason if don't want extends from EntityBase class, just copy all fields and methods in EntityBase class
 * to your entity bean class
 * 
 */
public class EntityBase implements IEntity {

	@Override
	public SqlBox box() {
		return SqlBox.getBox(this);
	}

	@Override
	public String table() {
		return box().table();
	}

	@Override
	public String star() {
		return box().star();
	}

	@Override
	public void insert() {
		this.box().insert();
	}

	@Override
	public void update() {
		this.box().update();
	}

	@Override
	public void delete() {
		this.box().delete();
	}

	@Override
	public String fieldID(String realColumnName) {
		return this.box().fieldID(realColumnName);
	}

}