package test.examples.orm.entities;

import com.github.drinkjava2.jsqlbox.Entity;

public class UserRole implements Entity {
	public static final String CREATE_SQL = "create table userrole("//
			+ "uid varchar(32),"//
			+ "rid varchar(32) )";
	String uid;
	String rid;
	{
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