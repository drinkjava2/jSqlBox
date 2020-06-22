package com.github.drinkjava2.jsqlbox.function.jtransactions.tinytx;

import static com.github.drinkjava2.jsqlbox.DB.tail;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID32;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.Tail;
import com.zaxxer.hikari.HikariDataSource;

public class TxMethodTest {

	HikariDataSource dataSource;
	DbContext ctx;

	@Before
	public void init() {
		dataSource = new HikariDataSource();// DataSource
		// H2 is a memory database
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		dataSource.setUsername("sa");
		dataSource.setPassword("");
		ctx = new DbContext(dataSource);
		ctx.executeDDL(ctx.toCreateDDL(Usrdemo.class));
		for (int i = 1; i <= 100; i++)
			new Usrdemo().putField("firstName", "Foo" + i, "lastName", "Bar" + i, "age", i).insert(ctx);
	}

	@After
	public void clean() {
		dataSource.close();
	}

	@Table(name = "Usrdemo")
	public static class Usrdemo extends ActiveRecord<Usrdemo> {
		@UUID32
		@Id
		String id;
		String firstName;
		String lastName;
		Integer age;

		public String getId() {
			return id;
		}

		public Usrdemo setId(String id) {
			this.id = id;
			return this;
		}

		public String getFirstName() {
			return firstName;
		}

		public Usrdemo setFirstName(String firstName) {
			this.firstName = firstName;
			return this;
		}

		public String getLastName() {
			return lastName;
		}

		public Usrdemo setLastName(String lastName) {
			this.lastName = lastName;
			return this;
		}

		public Integer getAge() {
			return age;
		}

		public Usrdemo setAge(Integer age) {
			this.age = age;
			return this;
		}

	}

	@Test
	public void DemoTest() {
		for (int i = 0; i < 1000; i++) {
			ctx.tryTx(() -> {
				Assert.assertEquals(100, ctx.eCountAll(Usrdemo.class));
				new Usrdemo().putField("firstName", "Foo").insert(ctx);
				Assert.assertEquals(101, ctx.eCountAll(Tail.class, tail("Usrdemo")));
				Systemout.println(1 / 0);
			});
		}
		Assert.assertEquals(100, ctx.eCountAll(Tail.class, tail("Usrdemo")));
		Assert.assertEquals(false, DB.getLastTxResult().isSuccess());

		ctx.tx(() -> {
			Assert.assertEquals(100, ctx.eCountAll(Usrdemo.class));
			new Usrdemo().putField("firstName", "Foo").insert(ctx);
			Assert.assertEquals(101, ctx.eCountAll(Tail.class, tail("Usrdemo")));
			new Usrdemo().putField("firstName", "Bar").insert(ctx);
		});
		Assert.assertEquals(102, ctx.eCountAll(Tail.class, tail("Usrdemo")));
		Assert.assertEquals(true, DB.getLastTxResult().isSuccess());
	}

}
