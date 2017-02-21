package test.examples.orm.entities;

import com.github.drinkjava2.jsqlbox.Entity;

public class User implements Entity {
	public static final String CREATE_SQL = "create table users("//
			+ "id varchar(32),"//
			+ "userName varchar(32) )";

	String id;
	String userName;
	{
		this.box().configAlias("u");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String ID() {
		return box().getColumnName("id");
	}
}