package com.github.drinkjava2.jsqlbox.benchmark;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.noPagin;

import java.util.List;

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
		this.countAll(" where 1=1 ", conditions, noPagin());
		// Or iQueryForLongValue("select count(1) from sys_user where 1=1 ", conditions,
		// noPagin());

		List<DemoUser> users = this.findAll(" where 1=1 ", conditions, " order by id");
		// Or iQueryForEntityList(DemoUser.class, "select * from sys_user where 1=1 ",
		// conditions, " order by id");

		if (users.isEmpty())
			throw new RuntimeException("pageQuery error");

	}

}