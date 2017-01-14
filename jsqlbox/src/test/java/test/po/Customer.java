package test.po;

import java.util.List;

import com.github.drinkjava2.jsqlbox.IEntity;
import com.github.drinkjava2.jsqlbox.Ignore;
import com.github.drinkjava2.jsqlbox.id.UUIDGenerator;

/**
 * Entity class is not a POJO, need extends from EntityBase or implements EntityInterface interface<br/>
 * 
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class Customer implements IEntity {
	public static final String CREATE_SQL = "create table customer("//
			+ "id varchar(32),"//
			+ "customer_name varchar(50),"//
			+ "constraint customer_pk primary key (id)" //
			+ ")";//
	private String id;
	private String customerName;
	@Ignore
	private List<Order> ordersList;
	{
		this.box().configIdGenerator("id", UUIDGenerator.INSTANCE);
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

}