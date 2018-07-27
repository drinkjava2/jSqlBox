package com.github.drinkjava2.test;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.iExecute;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.iQueryForLongValue;

import java.util.List;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveEntity;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.annotation.Sql;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;

/**
 * ActiveRecordJava8 is a interface has default methods only supported for
 * Java8+, so in Java8 and above, a POJO can implements ActiveRecordJava8
 * interface to obtain CRUD methods instead of extends ActiveRecord class
 */
public class ActiveEntityTest implements ActiveEntity<ActiveEntityTest> {
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

	@Sql("select * from ActiveEntityTest where age>=?")
	public List<ActiveEntityTest> selectUsers(Integer age) {
		return guess(age, new EntityListHandler(), ActiveEntityTest.class);
	};

	@Before
	public void init() {
		SqlBoxContext ctx = new SqlBoxContext(JdbcConnectionPool
				.create("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", ""));
		SqlBoxContext.setGlobalSqlBoxContext(ctx);
		for (String ddl : ctx.toCreateDDL(ActiveEntityTest.class))
			iExecute(ddl);
		for (int i = 0; i < 100; i++)
			new ActiveEntityTest().put("name", "name" + i, "age", i).insert();
		Assert.assertEquals(100, iQueryForLongValue("select count(*) from ActiveEntityTest"));
	}

	@Test
	public void doTest() {
		List<ActiveEntityTest> userList = new ActiveEntityTest().selectUsers(50);
		Assert.assertEquals(50, userList.size());
	}
}