package com.github.drinkjava2.test;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQuery;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQueryForLongValue;

import java.util.List;

import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.github.drinkjava2.jsqlbox.handler.PaginHandler;

@Table(name = "usertb")
public class User extends ActiveRecord {

	@UUID25
	@Id
	public String id;

	public String name;

	public String address;

	public Integer age;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public List<User> pageQuery(PaginHandler pagin, Object... conditions) {
		giQueryForLongValue("select count(1) from usertb where 1=1 ", conditions);
		return giQuery(new EntityListHandler(User.class), "select * from usertb where 1=1", conditions, pagin);
	}
}