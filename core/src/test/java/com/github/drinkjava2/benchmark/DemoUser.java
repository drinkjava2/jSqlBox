package com.github.drinkjava2.benchmark;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQueryForEntityList;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.giQueryForLongValue;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.noPagin;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

@Table(name = "sys_user")
public class DemoUser extends ActiveRecord<DemoUser> {

	@Id
	private Integer id;

	private String code;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void pageQuery(Object... conditions) {
		giQueryForLongValue("select count(1) from sys_user where 1=1 ", conditions, noPagin());
		giQueryForEntityList(DemoUser.class, "select * from sys_user where 1=1 ", conditions, " order by id");
	}

}