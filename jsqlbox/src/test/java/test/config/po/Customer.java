package test.config.po;

import java.util.List;

import com.github.drinkjava2.jsqlbox.Entity;
import com.github.drinkjava2.jsqlbox.IgnoreField;
import com.github.drinkjava2.jsqlbox.id.SimpleGenerator;

/**
 * Entity class is not a POJO, need extends from EntityBase or implements EntityInterface interface<br/>
 * 
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class Customer implements Entity {
	public static final String CREATE_SQL = "create table customer("//
			+ "id varchar(32),"//
			+ "customer_name varchar(50),"//
			+ "constraint customer_pk primary key (id)" //
			+ ")";//
	private String id;
	private String customerName;
	@IgnoreField
	private List<Order> ordersList;
	{
		this.box().configIdGenerator("id", SimpleGenerator.INSTANCE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public List<Order> getOrdersList() {
		return ordersList;
	}

	public void setOrdersList(List<Order> ordersList) {
		this.ordersList = ordersList;
	}

	// ====================
	public String ID() {
		return box().getColumnName("id");
	};

	public String CUSTOMERNAME() {
		return box().getColumnName("customerName");
	};

	public String ORDERLIST() {
		return box().getColumnName("ordersList");
	};

}