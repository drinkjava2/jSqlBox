package test.examples.ddd;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.Entity;
import com.github.drinkjava2.jsqlbox.id.AssignedGenerator;

public class Part implements Entity {
	public static final String CREATE_SQL = "create table part("//
			+ "partid varchar(32),"//
			+ "totalCurrentStock int,"//
			+ "stockOnHold int,"//
			+ "pendingPOs int,"//
			+ "stockAvailable int,"//
			+ "safetyStockLevel int,"//
			+ "shortage int)"//
	;
	String partID;
	Integer totalCurrentStock;
	Integer stockOnHold;
	Integer pendingPOs;
	Integer stockAvailable;
	Integer safetyStockLevel;
	Integer shortage;

	{
		this.box().configEntityIDs("partID");
		this.box().configIdGenerator("partID", AssignedGenerator.INSTANCE);
	}

	public String getPartID() {
		return partID;
	}

	public void setPartID(String partID) {
		this.partID = partID;
	}

	public Integer getTotalCurrentStock() {
		return totalCurrentStock;
	}

	public void setTotalCurrentStock(Integer totalCurrentStock) {
		this.totalCurrentStock = totalCurrentStock;
	}

	public Integer getStockOnHold() {
		return stockOnHold;
	}

	public void setStockOnHold(Integer stockOnHold) {
		this.stockOnHold = stockOnHold;
	}

	public Integer getPendingPOs() {
		return pendingPOs;
	}

	public void setPendingPOs(Integer pendingPOs) {
		this.pendingPOs = pendingPOs;
	}

	public Integer getStockAvailable() {
		return stockAvailable;
	}

	public void setStockAvailable(Integer stockAvailable) {
		this.stockAvailable = stockAvailable;
	}

	public Integer getSafetyStockLevel() {
		return safetyStockLevel;
	}

	public void setSafetyStockLevel(Integer safetyStockLevel) {
		this.safetyStockLevel = safetyStockLevel;
	}

	public Integer getShortage() {
		return shortage;
	}

	public void setShortage(Integer shortage) {
		this.shortage = shortage;
	}

	public void calculate_StockAvailable() {
		this.setStockAvailable(totalCurrentStock - stockOnHold + pendingPOs);
	}

	public void calculate_shortage() {
		this.setShortage(stockAvailable - safetyStockLevel);
	}

	public static Part insert(String partID, Integer initialQTY) {
		Part part = new Part();
		part.partID = partID;
		part.totalCurrentStock = initialQTY;
		part.stockOnHold = 0;
		part.pendingPOs = 0;
		part.stockAvailable = 0;
		part.safetyStockLevel = 10;
		part.shortage = 0;
		part.calculate_StockAvailable();
		part.calculate_shortage();
		LogPart.insert(partID, 0, initialQTY);
		part.insert();
		return part;
	}

	public static void update_pendingPOs(String partID) {
		Part part = Dao.load(Part.class, partID);
		int pendingPOs = Dao.queryForInteger("select sum(BackOrder) from PODetail where partID=", q(partID));
		part.setPendingPOs(pendingPOs);
		part.calculate_StockAvailable();
		part.calculate_shortage();
		part.update();
	}

	public static void receive_part(String partID, Integer receiveQTY) {
		Part part = Dao.load(Part.class, partID);
		int oldQTY = part.totalCurrentStock;
		part.setTotalCurrentStock(part.getTotalCurrentStock() + receiveQTY);
		part.calculate_StockAvailable();
		part.calculate_shortage();
		part.update();
		LogPart.insert(partID, oldQTY, part.getTotalCurrentStock());
	}

}