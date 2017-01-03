package test.ddd;

import com.github.drinkjava2.jsqlbox.IEntity;
import com.github.drinkjava2.jsqlbox.id.UUIDGenerator;

public class LogPart implements IEntity {
	public static final String CREATE_SQL = "create table logpart("//
			+ "id varchar(32),"//
			+ "partID varchar(32),"//
			+ "oldQTY int,"//
			+ "newQTY int,"//
			+ "changedQty int)"//
	;
	String id;
	String partID;
	Integer oldQTY;
	Integer newQTY;
	Integer changedQty;

	{
		this.box().configIdGenerator("id", UUIDGenerator.INSTANCE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPartID() {
		return partID;
	}

	public void setPartID(String partID) {
		this.partID = partID;
	}

	public Integer getOldQTY() {
		return oldQTY;
	}

	public void setOldQTY(Integer oldQTY) {
		this.oldQTY = oldQTY;
	}

	public Integer getNewQTY() {
		return newQTY;
	}

	public void setNewQTY(Integer newQTY) {
		this.newQTY = newQTY;
	}

	public Integer getChangedQty() {
		return changedQty;
	}

	public void setChangedQty(Integer changedQty) {
		this.changedQty = changedQty;
	}

}