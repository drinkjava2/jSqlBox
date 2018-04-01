package com.github.drinkjava2.functionstest.entitynet.entities;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.annotation.jpa.Transient;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

@Table(name = "roletb")
public class Role extends ActiveRecord {
	@Id
	String id;

	String roleName;

	@Transient
	User user;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}