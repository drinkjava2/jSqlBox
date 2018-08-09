package com.example.jsqlboxinspringboot.entity;

import com.github.drinkjava2.jdialects.annotation.jdia.UUID32;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveEntity;

public class Customer implements ActiveEntity<Customer> {

	@Id
	@UUID32
	String id;
	
	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}