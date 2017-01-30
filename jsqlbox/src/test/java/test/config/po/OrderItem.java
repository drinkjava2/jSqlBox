package test.config.po;

import com.github.drinkjava2.jsqlbox.Entity;
import com.github.drinkjava2.jsqlbox.id.SimpleGenerator;

/**
 * Entity class is not a POJO, need extends from EntityBase or implements EntityInterface interface<br/>
 * 
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class OrderItem implements Entity {
	public static final String CREATE_SQL = "create table orderitem("//
			+ "id varchar(32),"//
			+ "item_name varchar(50),"//
			+ "item_qty int,"//
			+ "received int,"//
			+ "order_id varchar(32),"//
			+ "constraint orderitem_pk primary key (id)," //
			+ "constraint orderid_fk foreign key (order_id) references orders(id)" //
			+ ")";//

	private String id;
	private String itemName;
	private Integer itemQty;
	private String orderId;
	{
		this.box().configIdGenerator("id", SimpleGenerator.INSTANCE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public Integer getItemQty() {
		return itemQty;
	}

	public void setItemQty(Integer itemQty) {
		this.itemQty = itemQty;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	// ==============
	public String ID() {
		return box().getColumnName("id");
	};

	public String ITEMNAME() {
		return box().getColumnName("itemName");
	};

	public String ITEMQTY() {
		return box().getColumnName("itemQty");
	};

	public String ORDERID() {
		return box().getColumnName("orderId");
	};

}