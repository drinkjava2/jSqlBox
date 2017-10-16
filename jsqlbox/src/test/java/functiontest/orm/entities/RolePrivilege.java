package functiontest.orm.entities;

import com.github.drinkjava2.jsqlbox.ActiveRecord;

public class RolePrivilege extends ActiveRecord {
	String rid;
	String pid;

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

}