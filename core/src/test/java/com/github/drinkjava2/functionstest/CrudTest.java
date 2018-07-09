package com.github.drinkjava2.functionstest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Usage of different SQL style and speed test
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class CrudTest {
	static long REPEAT_TIMES = 1;
	static boolean PRINT_TIMEUSED = false;

	protected HikariDataSource dataSource;

	public static class CrudUser extends ActiveRecord {
		@Id
		@Column(name = "name")
		String name;
		String address;
		Integer age;

		public CrudUser() {
		}

		public CrudUser(String name, String address) {
			this.name = name;
			this.address = address;
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

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

	}

	@Before
	public void init() {
		dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUsername("sa");// change to your user & password
		dataSource.setPassword("");
		// SqlBoxContext.setGlobalAllowShowSql(true);
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(null);
		for (String ddl : ctx.getDialect().toDropAndCreateDDL(CrudUser.class))
			try {
				ctx.nExecute(ddl);
			} catch (Exception e) {
			}
	}

	@After
	public void cleanUp() {
		dataSource.close();
	}

	@Test
	public void crudTest() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		CrudUser u1 = new CrudUser();
		u1.setName("Tom");
		u1.setAddress("China");
		ctx.insertEntity(u1);

		u1.setAddress("Canada");
		ctx.updateEntity(u1);

		CrudUser u2 = new CrudUser();
		u2.setName("Tom");
		ctx.loadEntity(u2);
		Assert.assertEquals("Canada", u2.getAddress());

		ctx.deleteEntity(u2);
		Assert.assertEquals(0, ctx.countAllEntity(CrudUser.class));
	}

	@Test
	public void activeRecordCrudTest() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);

		CrudUser u1 = new CrudUser().put("name", "Tom", "address", "China").insert();

		u1.put("address", "Canada").update();

		CrudUser u2 = new CrudUser().put("name", "Tom").load();
		Assert.assertEquals("Canada", u2.getAddress());

		u2.delete();
		Assert.assertEquals(0, u1.countAll());
	}

}
