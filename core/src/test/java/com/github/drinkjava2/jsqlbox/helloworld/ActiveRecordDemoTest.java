package com.github.drinkjava2.jsqlbox.helloworld;

import static com.github.drinkjava2.jsqlbox.DB.par;

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This demo show jSqlBox's ActiveRecord and jDialects' DDL, pagination and
 * translate feature.
 */
public class ActiveRecordDemoTest {

	@Table(name = "users")
	public static class UserEntity extends ActiveRecord<UserEntity> {
		@UUID25
		@Id
		private String id;

		private String firstName;

		private String lastName;

		private Integer age;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}
	}

	@Test
	public void doTest() {
		HikariDataSource ds = new HikariDataSource();// DataSource

		// H2 is a memory database
		ds.setDriverClassName("org.h2.Driver");
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setUsername("sa");
		ds.setPassword("");

		// MySQL
		// ds.setDriverClassName("com.mysql.jdbc.Driver");
		// ds.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useSSL=false");
		// ds.setUsername("root");
		// ds.setPassword("root888");

		// MS-SqlServer
		// ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		// ds.setJdbcUrl("jdbc:sqlserver://localhost:1433;databaseName=test");
		// ds.setUsername("sa");
		// ds.setPassword("root888");

		// ORACLE
		// ds.setDriverClassName("oracle.jdbc.OracleDriver");
		// ds.setJdbcUrl("jdbc:oracle:thin:@127.0.0.1:1521:XE");
		// ds.setUsername("root");
		// ds.setPassword("root888");

		DbContext ctx = new DbContext(ds);
		DbContext.setGlobalDbContext(ctx);

		String[] ddlArray = ctx.getDialect().toDropAndCreateDDL(UserEntity.class);
		for (String ddl : ddlArray)
			ctx.quiteExecute(ddl);

		for (int i = 1; i <= 100; i++) {
			UserEntity u = new UserEntity();
			u.setFirstName("Foo" + i);
			u.setLastName("Bar" + i);
			u.setAge(i);
			u.insert();
		}

		Assert.assertEquals(100L, ((Number) ctx.jdbcQueryForObject("select count(*) from users")).longValue());

		List<Map<String, Object>> users = ctx.qry(new MapListHandler(),
				ctx.pagin(2, 10, "select concat(firstName, ' ', lastName) as UserName, age from users where age>?"),
				par(50));

		Assert.assertEquals(10, users.size());

		for (Map<String, Object> map : users)
			Systemout.println("UserName=" + map.get("USERNAME") + ", age=" + map.get("AGE"));

		ds.close();
	}
}
