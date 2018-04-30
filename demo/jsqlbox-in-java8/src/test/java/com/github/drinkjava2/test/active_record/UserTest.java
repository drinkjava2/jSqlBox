package com.github.drinkjava2.test.active_record;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.annotation.Sql;
import com.github.drinkjava2.test.TestBase;

/**
 * ActiveRecordJava8 is a interface has default methods only supported for
 * Java8+, so in Java8 and above, a POJO can implements ActiveRecordJava8
 * interface to obtain CRUD methods instead of extends ActiveRecord class
 */

public class UserTest extends TestBase implements ActiveRecordJava8 {
	@Id
	private String name;
	private Integer age;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	@Sql("select u.** from UserTest u where u.age>=?")
	public List<UserTest> selectUsers(Integer age) {
		return guess(age);
	};

	@Override
	public void prepareTestData() {
		for (String ddl : ctx.toCreateDDL(UserTest.class))
			ctx.iExecute(ddl);
		for (int i = 0; i < 100; i++)
			new UserTest().put("name", "name" + i, "age", i).insert();
		Assert.assertEquals(100, ctx.iQueryForLongValue("select count(*) from UserTest"));
	}

	@Override
	public void deleteTestData() {
		for (String ddl : ctx.toDropDDL(UserTest.class))
			ctx.iExecute(ddl);
	}

	@Test
	public void doTest() {
		List<UserTest> userList = new UserTest().selectUsers(50);
		Assert.assertEquals(50, userList.size());
	}
}