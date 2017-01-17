package test.examples.ddd;

import com.github.drinkjava2.jsqlbox.Entity;
import com.github.drinkjava2.jsqlbox.id.UUID25Generator;

public class POReceiving implements Entity {
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
		this.box().configIdGenerator("id", UUID25Generator.INSTANCE);
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

	public static void insert(POReceiving poReceiving) {
		poReceiving.insert();
		Part.receive_part(poReceiving.getPartID(), poReceiving.receiveQTY);
	}

}