package com.github.drinkjava2.functionstest.entitynet.entities;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

@Table(name = "usertb")
public class User extends ActiveRecord {
	@Id
	String id;
	String userName;
 
	String teatherId; 
	String bossId;
	Integer age;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getTeatherId() {
		return teatherId;
	}

	public void setTeatherId(String teatherId) {
		this.teatherId = teatherId;
	}

	public String getBossId() {
		return bossId;
	}

	public void setBossId(String bossId) {
		this.bossId = bossId;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

}