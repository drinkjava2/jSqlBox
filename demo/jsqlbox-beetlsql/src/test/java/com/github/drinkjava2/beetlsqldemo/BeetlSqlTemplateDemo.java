package com.github.drinkjava2.beetlsqldemo;

import static com.github.drinkjava2.jdbpro.JDBPRO.put;

import java.util.List;
import java.util.Map;

import org.beetl.sql.core.ClasspathLoader;
import org.beetl.sql.core.ConnectionSource;
import org.beetl.sql.core.ConnectionSourceHelper;
import org.beetl.sql.core.Interceptor;
import org.beetl.sql.core.SQLLoader;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.UnderlinedNameConversion;
import org.beetl.sql.core.db.DBStyle;
import org.beetl.sql.core.db.H2Style;
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This demo shows use BeetlSQL as SQL Template engine for jSqlBox (Although
 * BeetlSQL is already a separate DAO tool, but anyway here I just want extract
 * the SQL template function of it.)
 * 
 * SQL file 'user.md' is located in folder "test/resources/sql"
 */
public class BeetlSqlTemplateDemo {

	@Test
	public void doTest() {
		HikariDataSource ds = new HikariDataSource();
		ds.setDriverClassName("org.h2.Driver"); // H2 Memory database
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setUsername("sa");
		ds.setPassword("");

		ConnectionSource source = ConnectionSourceHelper.getSingle(ds);
		DBStyle dbstyle = new H2Style();
		SQLLoader loader = new ClasspathLoader("/sql");
		UnderlinedNameConversion nc = new UnderlinedNameConversion();
		SQLManager sqlManager = new SQLManager(dbstyle, loader, source, nc, new Interceptor[] {});
		// Set BeetlSqlTempalte as global default template engine

		SqlBoxContextConfig.setGlobalNextTemplateEngine(new BeetlSqlTempalte(sqlManager));
		SqlBoxContextConfig.setGlobalNextAllowShowSql(true); // Log output
		SqlBoxContext ctx = new SqlBoxContext(ds); // Here you go
		SqlBoxContext.setGlobalSqlBoxContext(ctx);

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

		Assert.assertEquals(100, ctx.nQueryForLongValue("select count(*) from users"));
		List<Map<String, Object>> result = ctx.tQueryForMapList("user.select", put("age", 50));
		Assert.assertEquals(50, result.size());

		List<User> users = ctx.tQuery("user.selectUserEntity", put("u", new User().put("age", 50, "name", "Foo100")),
				new EntityListHandler(User.class));
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(users.get(0).getAge().equals(100));

		ds.close();
	}

	@Table(name = "users")
	public static class User extends ActiveRecord {

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
