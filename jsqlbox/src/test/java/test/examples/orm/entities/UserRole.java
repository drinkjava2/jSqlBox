package test.examples.orm.entities;

import com.github.drinkjava2.jsqlbox.Entity;

public class UserRole implements Entity {
	// userrole change to userroletb because userrole is Oracle's keyword
	public static final String CREATE_SQL = "create table userroletb (" + "userid varchar(32),"//
			+ "rid varchar(32) )";
	String uid;
	String rid;
	{
		this.box().configTable("userroletb");
		this.box().configColumnName("uid", "userid");// uid is Oracle's keyword
		this.box().configEntityIDs("uid", "rid");
		this.box().configAlias("ur");
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	public String UID() {
		return box().getColumnName("uid");
	}

	public String RID() {
		return box().getColumnName("rid");
	}
}