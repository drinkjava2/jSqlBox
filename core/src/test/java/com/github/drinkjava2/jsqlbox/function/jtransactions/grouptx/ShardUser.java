package com.github.drinkjava2.jsqlbox.function.jtransactions.grouptx;

import com.github.drinkjava2.jdialects.annotation.jdia.ShardDatabase;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

public class ShardUser extends ActiveRecord<ShardUser> {
	@Id
	@ShardDatabase({ "MOD", "2" })
	Integer id;
	String name;

	public Integer getId() {
		return id;
	}

	public ShardUser setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public ShardUser setName(String name) {
		this.name = name;
		return this;
	}

}
