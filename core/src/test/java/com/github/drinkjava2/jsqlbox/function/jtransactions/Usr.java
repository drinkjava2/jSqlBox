package com.github.drinkjava2.jsqlbox.function.jtransactions;

import com.github.drinkjava2.jdialects.annotation.jdia.UUID32;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

@Table(name = "users")
public class Usr extends ActiveRecord<Usr> {
	@UUID32
	@Id
	String id;
	String firstName;
	String lastName;
	Integer age;

	public String getId() {
		return id;
	}

	public Usr setId(String id) {
		this.id = id;
		return this;
	}

	public String getFirstName() {
		return firstName;
	}

	public Usr setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getLastName() {
		return lastName;
	}

	public Usr setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public Integer getAge() {
		return age;
	}

	public Usr setAge(Integer age) {
		this.age = age;
		return this;
	}

}
