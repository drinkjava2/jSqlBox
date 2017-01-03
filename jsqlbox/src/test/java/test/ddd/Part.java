package test.ddd;

import com.github.drinkjava2.jsqlbox.IEntity;
import com.github.drinkjava2.jsqlbox.id.AssignedGenerator;

public class Part implements IEntity {
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
		this.setShortage(stockAvailable - safetyStockLevel);
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

	public static void onChange_TotalCurrentStock(Part part, Integer totalCurrentStock) {
		LogPart log = new LogPart();
		log.setPartID(part.partID);
		log.setOldQTY(part.totalCurrentStock);
		log.setNewQTY(totalCurrentStock);
		log.insert();

		part.totalCurrentStock = totalCurrentStock;
		part.setStockAvailable(totalCurrentStock - part.stockOnHold - part.pendingPOs);
		part.update();
	}

	public static Part create(String partID) {
		Part part = new Part();
		part.partID = partID;
		part.totalCurrentStock = 0;
		part.stockOnHold = 0;
		part.pendingPOs = 0;
		part.stockAvailable = 0;
		part.safetyStockLevel = 10;
		part.shortage = 0;
		part.insert();
		return part;
	}

}