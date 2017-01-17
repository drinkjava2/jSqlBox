package test.examples.ddd;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.Entity;
import com.github.drinkjava2.jsqlbox.id.UUID25Generator;

public class PODetail implements Entity {
	public static final String CREATE_SQL = "create table podetail("//
			+ "id varchar(32),"//
			+ "po varchar(32),"//
			+ "partID varchar(32),"//
			+ "poQTY int,"//
			+ "received int,"//
			+ "backOrder int)"//
	;
	String id;
	String po;
	String partID;
	Integer poQTY;
	Integer received;
	Integer backOrder;
	{
		this.box().configIdGenerator("id", UUID25Generator.INSTANCE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPo() {
		return po;
	}

	public void setPo(String po) {
		this.po = po;
	}

	public String getPartID() {
		return partID;
	}

	public void setPartID(String partID) {
		this.partID = partID;
	}

	public Integer getPoQTY() {
		return poQTY;
	}

	public void setPoQTY(Integer poQTY) {
		this.poQTY = poQTY;
	}

	public Integer getReceived() {
		return received;
	}

	public void setReceived(Integer received) {
		this.received = received;
	}

	public Integer getBackOrder() {
		return backOrder;
	}

	public void setBackOrder(Integer backOrder) {
		this.backOrder = backOrder;
	}

	public void calculateBackorder() {
		this.backOrder = poQTY - received;
	}

	public static PODetail insert(String po, String partID, Integer poQTY) {
		PODetail poDetail = new PODetail();
		poDetail.po = po;
		poDetail.partID = partID;
		poDetail.poQTY = poQTY;
		poDetail.received = 0;
		poDetail.backOrder = poQTY;
		poDetail.insert();
		return poDetail;
	}

	public static void onChange_backorder(PODetail poDetail) {
		Part part = Dao.load(Part.class, poDetail.getPartID());
		part.setPendingPOs(
				Dao.queryForInteger("select sum(backOrder) from podetail where partID=", q(poDetail.getPartID())));
	}

	public static void receivePartsFromPO(PODetail poDetail, Integer receiveQTY) {
		POReceiving poReceiving = new POReceiving();
		poReceiving.setPO(poDetail.getPo());
		poReceiving.setPartID(poDetail.getPartID());
		poReceiving.setReceiveQTY(receiveQTY);
		POReceiving.insert(poReceiving);

		poDetail.setReceived(poDetail.getReceived() + receiveQTY);
		poDetail.calculateBackorder();
		poDetail.update();
		Part.update_pendingPOs(poDetail.getPartID());
	}

}