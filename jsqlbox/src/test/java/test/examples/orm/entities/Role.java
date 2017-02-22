package test.examples.orm.entities;

import com.github.drinkjava2.jsqlbox.Entity;

public class Role implements Entity {
	public static final String CREATE_SQL = "create table roles("//
			+ "id varchar(32),"//
			+ "roleName varchar(32) )";
	String id;
	String roleName;
	{
		this.box().configTable("roles");
		this.box().configAlias("r");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String ID() {
		return box().getColumnName("id");
	}
}