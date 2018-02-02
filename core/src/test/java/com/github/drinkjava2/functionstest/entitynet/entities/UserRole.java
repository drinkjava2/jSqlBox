package com.github.drinkjava2.functionstest.entitynet.entities;

import com.github.drinkjava2.jdialects.annotation.jdia.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

@Table(name = "userroletb")
public class UserRole extends ActiveRecord {
	@Id
	@UUID25
	String id;

	@SingleFKey(refs = { "usertb", "id" })
	String userId;

	@SingleFKey(refs = { "roletb", "id" })
	String rid;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}
}