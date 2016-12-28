package com.github.drinkjava2.jsqlbox;

/**
 * Each entity bean class should extends from EntityBase class <br/>
 * But for some reason if don't want extends from EntityBase class, just copy all fields and methods in EntityBase class
 * to your entity bean class
 * 
 */
public class EntityBase {

	private Box box;

	public void putBox(Box box) {
		this.box = box;
	}

	public Box box() {
		return Box.getBox(this, box);
	}

	public String table() {
		return box().getRealTable();
	}

	public String star() {
		return box().getStar();
	}

	public void insert() {
		this.box().insert();
	}

	public void update() {
		this.box().update();
	}

	public void delete() {
		this.box().delete();
	}

	public String fieldID(String realColumnName) {
		return this.box().fieldID(realColumnName);
	}

}