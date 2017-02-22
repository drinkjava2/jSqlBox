package test.examples.orm.entities;

import com.github.drinkjava2.jsqlbox.Entity;

public class Privilege implements Entity {
	public static final String CREATE_SQL = "create table privilegetb(" // privileges is Oracle reserved word
			+ "id varchar(32),"//
			+ "privilegeName varchar(32) )";
	String id;
	String privilegeName;
	{
		this.box().configTable("privilegetb");
		this.box().configAlias("p");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPrivilegeName() {
		return privilegeName;
	}

	public void setPrivilegeName(String privilegeName) {
		this.privilegeName = privilegeName;
	}

	public String ID() {
		return box().getColumnName("id");
	}
}