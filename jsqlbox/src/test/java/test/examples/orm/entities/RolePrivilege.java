package test.examples.orm.entities;

import com.github.drinkjava2.jsqlbox.Entity;

public class RolePrivilege implements Entity {
	public static final String CREATE_SQL = "create table roleprivilege("//
			+ "rid varchar(32),"//
			+ "pid varchar(32) )";
	String rid;
	String pid;
	{
		this.box().configEntityIDs("rid", "pid");
		this.box().configAlias("rp");
	}

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String RID() {
		return box().getColumnName("rid");
	}

	public String PID() {
		return box().getColumnName("pid");
	}
}