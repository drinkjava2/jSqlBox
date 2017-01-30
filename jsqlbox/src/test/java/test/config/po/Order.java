package test.config.po;

import java.util.Date;

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
public class Order implements Entity {
	public static final String CREATE_SQL = "create table orders("//
			+ "id varchar(32),"//
			+ "order_name varchar(50),"//
			+ "order_date date,"//
			+ "customer_id varchar(32),"//
			+ "constraint orders_pk primary key (id)," //
			+ "constraint customerid_fk foreign key (customer_id) references customer(id)" //
			+ ")";//

	private String id;
	private String orderName;
	private Date orderDate;
	private String customerId;

	{
		this.box().configIdGenerator("id", SimpleGenerator.INSTANCE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrderName() {
		return orderName;
	}

	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	// ===============
	public String ID() {
		return box().getColumnName("id");
	};

	public String ORDERNAME() {
		return box().getColumnName("orderName");
	};

	public String ORDERDATE() {
		return box().getColumnName("orderDate");
	};

	public String CUSTOMERID() {
		return box().getColumnName("customerId");
	};
}