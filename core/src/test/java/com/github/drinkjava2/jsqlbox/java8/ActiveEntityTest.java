package com.github.drinkjava2.jsqlbox.java8;
/*- JAVA8_BEGIN
import static com.github.drinkjava2.jdbpro.JDBPRO.param;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveEntity;
import com.github.drinkjava2.jsqlbox.config.TestBase;
 
public class ActiveEntityTest  extends TestBase implements ActiveEntity<ActiveEntityTest>{
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

	@Test
	public void doTest() {
		quietDropTables(ActiveEntityTest.class);
		createAndRegTables(ActiveEntityTest.class);
		for (int i = 0; i < 100; i++)
			new ActiveEntityTest().putField("name", "name" + i, "age", i).insert();
		Assert.assertEquals(100, iQueryForLongValue("select count(*) from ActiveEntityTest"));
		List<ActiveEntityTest> userList = new ActiveEntityTest()
				.findBySQL("select * from ActiveEntityTest where age>=?", param(50));
		Assert.assertEquals(50, userList.size());
	}
	 
}
JAVA8_END */