package com.github.drinkjava2.benchmark;

import com.github.drinkjava2.jdialects.annotation.jdia.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Entity;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

@Table(name = "sys_order")
@Entity(name = "sys_order")
public class DemoOrder extends ActiveRecord<DemoOrder> {
	@Id
	private Integer id;

	private String name;

	@Column(name = "cust_id")
	@SingleFKey(refs = { "sys_customer", "id" })
	private Integer custId;

	DemoCustomer demoCustomer;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DemoCustomer getDemoCustomer() {
		return demoCustomer;
	}

	public void setDemoCustomer(DemoCustomer demoCustomer) {
		this.demoCustomer = demoCustomer;
	}

	public Integer getCustId() {
		return custId;
	}

	public void setCustId(Integer custId) {
		this.custId = custId;
	}

}
