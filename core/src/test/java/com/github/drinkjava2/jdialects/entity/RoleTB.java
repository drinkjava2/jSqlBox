package com.github.drinkjava2.jdialects.entity;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.annotation.jpa.Transient;

@Table(name = "roletb")
public class RoleTB   {
	@Id
	String id;

	String roleName;

	@Transient
	UserTB user;

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

	public UserTB getUser() {
		return user;
	}

	public void setUser(UserTB user) {
		this.user = user;
	}

}