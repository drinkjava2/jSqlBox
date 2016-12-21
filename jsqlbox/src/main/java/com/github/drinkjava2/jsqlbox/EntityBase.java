package com.github.drinkjava2.jsqlbox;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBox;

/**
 * Each entity bean class should extends from EntityBase class <br/>
 * But for some reason if don't want extends from EntityBase class, just copy all fields and methods in EntityBase class
 * to your entity bean class
 * 
 */
public class EntityBase {
	private Dao dao;

	public void putDao(Dao dao) {
		this.dao = dao;
	}

	public Dao dao() {
		return Dao.getDao(this, dao);
	}

	public String table() {
		return box().getRealTable();
	}

	public SqlBox box() {
		return dao().getBox();
	}

	public void insert() {
		this.dao().insert();
	}

	public void update() {
		this.dao().update();
	}

	public void delete() {
		this.dao().delete();
	}

}