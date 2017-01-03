package test.ddd;

import com.github.drinkjava2.jsqlbox.IEntity;
import com.github.drinkjava2.jsqlbox.id.UUIDGenerator;

public class POReceiving implements IEntity {
	public static final String CREATE_SQL = "create table poreceiving("//
			+ "id varchar(32),"//
			+ "PO varchar(32),"//
			+ "partID varchar(32),"//
			+ "receiveQTY int)"//
	;
	String id;
	String PO;
	String partID;
	Integer receiveQTY;
	{
		this.box().configIdGenerator("id", UUIDGenerator.INSTANCE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPO() {
		return PO;
	}

	public void setPO(String pO) {
		PO = pO;
	}

	public String getPartID() {
		return partID;
	}

	public void setPartID(String partID) {
		this.partID = partID;
	}

	public Integer getReceiveQTY() {
		return receiveQTY;
	}

	public void setReceiveQTY(Integer receiveQTY) {
		this.receiveQTY = receiveQTY;
	}

}