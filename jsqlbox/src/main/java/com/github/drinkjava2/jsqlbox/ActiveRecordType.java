package com.github.drinkjava2.jsqlbox;

public interface EntityType {

	public SqlBox box();

	public <T> T insert();

	public <T> T update();

	public <T> T delete();

	public <T> T load(Object id);

}