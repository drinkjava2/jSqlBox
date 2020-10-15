package com.github.drinkjava2.beetlsqldemo;

import static com.github.drinkjava2.jsqlbox.DB.bind;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveEntity;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.zaxxer.hikari.HikariDataSource;

import sql.SQLs.SelectUsers1;
import sql.SQLs.SelectUsers2;

/**
 * This demo shows use Beetl3.0 as SQL Template engine
 */
public class BeetlSqlTemplateDemoTest {

	@Test
	public void doTest() throws IOException {
		HikariDataSource ds = new HikariDataSource();
		ds.setDriverClassName("org.h2.Driver"); // H2 Memory database
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setUsername("sa");
		ds.setPassword("");

		DbContext ctx = new DbContext(ds);
		SqlTemplateEngine templ= new BeetlSqlTempalte(); // Customized beetl SQL template!
		ctx.setAllowShowSQL(true); // Allow show SQL log
		DbContext.setGlobalDbContext(ctx);

		String[] ddlArray = ctx.toDropAndCreateDDL(User.class);
		for (String ddl : ddlArray)
			ctx.quiteExecute(ddl);

		try {
			ctx.nBatchBegin();
			for (int i = 1; i <= 100; i++) {
				User u = new User();
				u.setName("Foo" + i);
				u.setAge(i);
				u.insert();
			}
		} finally {
			ctx.nBatchEnd();
		}
		Assert.assertEquals(100, ctx.qryLongValue("select count(*) from users"));

		// ===============================================================
		// ======== table prepare finished, below is the test============
		// ===============================================================

		int result = ctx.qryIntValue(templ, "select count(1) from users where age>#{age} or name='${name}'",
				bind("age", 50, "name", null));
		Assert.assertEquals(50, result);

		List<Map<String, Object>> usrs = ctx.qryMapList(SelectUsers1.class, templ, bind("age", 50, "name", null));
		Assert.assertEquals(50, usrs.size());

		List<User> users = ctx.qry(SelectUsers2.class, templ, bind("u", new User().putField("age", 50, "name", "Foo100")),
				new EntityListHandler(), User.class);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.get(0).getAge().equals(100));

		ds.close();
	}

	@Table(name = "users")
	public static class User implements ActiveEntity<User> {

		@UUID25
		@Id
		private String id;

		private String name;

		private Integer age;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

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
	}
}
