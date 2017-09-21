package com.github.drinkjava2.jsqlbox;

/**
 * Each entity bean class should extends from EntityBase or implements Entity
 * interface <br/>
 * But for some reason if don't want implements EntityBase class, just copy all
 * methods in EntityBase class to entity bean class
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class EntityBase implements EntityType {
 
	@Override
	public SqlBox box() {
		return null;
	}

	@Override
	public <T> T insert() {
		return null;
	}

	@Override
	public <T> T update() {
		return null;
	}

	@Override
	public <T> T delete() {
		return null;
	}

	@Override
	public <T> T load(Object id) {
		return null;
	}

}