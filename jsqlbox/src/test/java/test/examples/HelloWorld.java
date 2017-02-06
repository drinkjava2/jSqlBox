package test.examples;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Entity;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

public class HelloWorld {

	public static class User implements Entity {
		Integer id;
		String userName;
		String address;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

	}

	@Test
	public void doTest() {
		System.out.println("===============================Testing HelloWorld===============================");
		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setDriverClassName("org.h2.Driver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setMaximumPoolSize(3);

		SqlBoxContext ctx = new SqlBoxContext(ds);
		ctx.executeQuiet("drop table users");
		ctx.execute("create table users ", //
				"(id integer auto_increment ,", //
				"username Varchar (50) ,", //
				"Address Varchar (50) ", //
				")");
		ctx.refreshMetaData();

		User u = ctx.createEntity(User.class);
		u.setUserName("User1");
		u.setAddress("Address1");
		u.insert();

		User u2 = ctx.load(User.class, u.getId());
		Assert.assertEquals("Address1", u2.getAddress());
		u2.setAddress("Address2");
		u2.update();

		User u3 = ctx.load(User.class, u2.getId());
		Assert.assertEquals("Address2", u3.getAddress());
		Assert.assertNotNull(u3.getId());
		u3.delete();
		Assert.assertNull(u3.getId());
	}

}