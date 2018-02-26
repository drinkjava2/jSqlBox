package com.github.drinkjava2.helloworld;

import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.param;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.param0;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.question;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.question0;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.valuesQuesions;
import static com.github.drinkjava2.jdbpro.template.TemplateQueryRunner.put;
import static com.github.drinkjava2.jdbpro.template.TemplateQueryRunner.put0;
import static com.github.drinkjava2.jdbpro.template.TemplateQueryRunner.replace;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.springsrc.utils.ClassUtils;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.Config;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.annotation.Handler;
import com.github.drinkjava2.jsqlbox.annotation.Sql;
import com.zaxxer.hikari.HikariDataSource;

import activerecordtext.AbstractUser;
import activerecordtext.TextedUser;

/**
 * Usuage of different SQL style and speed test
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class UsuageAndSpeedTest {
	static long REPEAT_TIMES = 1;
	static boolean PRINT_TIMEUSED = false;

	protected HikariDataSource dataSource;

	@Before
	public void init() {
		dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUsername("sa");// change to your user & password
		dataSource.setPassword("");
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(null);
		for (String ddl : ctx.getDialect().toDropAndCreateDDL(User.class))
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
	public void speedTest() throws Exception {
		PRINT_TIMEUSED = false;
		REPEAT_TIMES = 100;// warm up
		runTestMethods();
		PRINT_TIMEUSED = true;
		REPEAT_TIMES = 100;
		System.out.println("Compare method execute time for repeat " + REPEAT_TIMES + " times:");
		runTestMethods();
		PRINT_TIMEUSED = false;
		REPEAT_TIMES = 1;
	}

	private void runTestMethods() throws Exception {
		runMethod("pureJdbc");
		runMethod("dbUtilsWithConnMethod");
		runMethod("dbUtilsNoConnMethod");
		runMethod("nXxxStyle");
		runMethod("iXxxStyle");
		runMethod("tXxxStyle");
		runMethod("xXxxStyle");
		runMethod("xXxxStyle_BasicTemplate");
		runMethod("dataMapperStyle");
		runMethod("activeRecordStyle");
		runMethod("activeRecordDefaultContext");
		runMethod("activeSqlAnnotaion");
		runMethod("activeSqlUseText");
		runMethod("abstractSqlUseText");
	}

	public void runMethod(String methodName) throws Exception {
		Method m = ClassUtils.getMethod(this.getClass(), methodName);
		long start = System.currentTimeMillis();
		m.invoke(this);
		long end = System.currentTimeMillis();
		String timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		if (PRINT_TIMEUSED)
			System.out.println(String.format("%28s: %6s s", methodName, timeused));
	}

	@Table(name = "users")
	public static class UserEntity {
		@Id
		String name;
		String address;

		public UserEntity() {
		}

		public UserEntity(String name, String address) {
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
	}

	@Table(name = "users")
	public static class User extends ActiveRecord {
		@Id
		String name;
		String address;

		public User() {
		}

		public User(String name, String address) {
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

	}

	public static class User2 extends User {
		@Sql("insert into users (name,address) values(?,?)")
		public void insertOneUser(String name, String address) {
			this.guess(name, address);
		};

		@Sql("update users set name=?, address=?")
		public void updateAllUser(String name, String address) {
			this.guess(name, address);
		};

		@Handler(MapListHandler.class)
		@Sql("select * from users where name=? and address=?")
		public List<Map<String, Object>> selectUsers(String name, String address) {
			return this.guess(name, address);
		};

		@Sql("delete from users where name=? or address=?")
		public void deleteUsers(String name, String address) {
			this.guess(name, address);
		};
	}

	@Test
	public void pureJdbc() {
		for (int i = 0; i < REPEAT_TIMES; i++) {
			Connection conn = null;
			PreparedStatement pst = null;
			ResultSet rs = null;
			try {
				conn = dataSource.getConnection();
				pst = conn.prepareStatement("insert into users (name,address) values(?,?)");
				pst.setString(1, "Sam");
				pst.setString(2, "Canada");
				pst.execute();
				pst.close();

				pst = conn.prepareStatement("update users set name=?, address=?");
				pst.setString(1, "Tom");
				pst.setString(2, "China");
				pst.execute();
				pst.close();

				pst = conn.prepareStatement("select count(*) from users where name=? and address=?");
				pst.setString(1, "Tom");
				pst.setString(2, "China");
				rs = pst.executeQuery();
				rs.next();
				Assert.assertEquals(1L, rs.getLong(1));

				pst = conn.prepareStatement("delete from users where name=? or address=?");
				pst.setString(1, "Tom");
				pst.setString(2, "China");
				pst.execute();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				if (rs != null)
					try {
						rs.close();
					} catch (SQLException e) {
					}
				if (pst != null)
					try {
						pst.close();
					} catch (SQLException e) {
					}
				if (conn != null)
					try {
						conn.close();
					} catch (SQLException e) {
					}
			}
		}
	}

	@Test
	public void dbUtilsWithConnMethod() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		for (int i = 0; i < REPEAT_TIMES; i++) {
			Connection conn = null;
			try {
				conn = ctx.prepareConnection();
				ctx.execute(conn, "insert into users (name,address) values(?,?)", "Sam", "Canada");
				ctx.execute(conn, "update users set name=?, address=?", "Tom", "China");
				Assert.assertEquals(1L, ctx.queryForObject(conn,
						"select count(*) from users where name=? and address=?", "Tom", "China"));
				ctx.execute(conn, "delete from users where name=? or address=?", "Tom", "China");
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					ctx.close(conn);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void dbUtilsNoConnMethod() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		for (int i = 0; i < REPEAT_TIMES; i++) {
			try {
				ctx.execute("insert into users (name,address) values(?,?)", "Sam", "Canada");
				ctx.execute("update users set name=?, address=?", "Tom", "China");
				Assert.assertEquals(1L,
						ctx.queryForObject("select count(*) from users where name=? and address=?", "Tom", "China"));
				ctx.execute("delete from users where name=? or address=?", "Tom", "China");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void nXxxStyle() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		for (int i = 0; i < REPEAT_TIMES; i++) {
			ctx.nExecute("insert into users (name,address) values(?,?)", "Sam", "Canada");
			ctx.nExecute("update users set name=?, address=?", "Tom", "China");
			Assert.assertEquals(1L,
					ctx.nQueryForObject("select count(*) from users where name=? and address=?", "Tom", "China"));
			ctx.nExecute("delete from users where name=? or address=?", "Tom", "China");
		}
	}

	@Test
	public void iXxxStyle() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		for (int i = 0; i < REPEAT_TIMES; i++) {
			ctx.iExecute("insert into users (", //
					" name ,", param0("Sam"), //
					" address ", param("Canada"), //
					") ", valuesQuesions());
			param0("Tom", "China");
			ctx.iExecute("update users set name=?,address=?");
			Assert.assertEquals(1L, ctx
					.iQueryForObject("select count(*) from users where name=? and address=?" + param0("Tom", "China")));
			ctx.iExecute("delete from users where name=", question0("Tom"), " or address=", question("China"));
		}
	}

	@Test
	public void tXxxStyle() {
		SqlBoxContext ctx2 = new SqlBoxContext(dataSource);
		Map<String, Object> params = new HashMap<String, Object>();
		for (int i = 0; i < REPEAT_TIMES; i++) {
			User sam = new User("Sam", "Canada");
			User tom = new User("Tom", "China");
			params.put("user", sam);
			ctx2.tExecute("insert into users (name, address) values(#{user.name},:user.address)", params);
			params.put("user", tom);
			ctx2.tExecute("update users set name=#{user.name}, address=:user.address", params);
			params.clear();
			params.put("name", "Tom");
			params.put("addr", "China");
			Assert.assertEquals(1L,
					ctx2.tQueryForObject("select count(*) from users where name=#{name} and address=:addr", params));
			params.put("u", tom);
			ctx2.tExecute("delete from users where name=:u.name or address=#{u.address}", params);
		}
	}

	@Test
	public void xXxxStyle() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		for (int i = 0; i < REPEAT_TIMES; i++) {
			User user = new User("Sam", "Canada");
			User tom = new User("Tom", "China");
			put0("user", user);
			ctx.xExecute("insert into users (name, address) values(#{user.name},:user.address)");
			put0("user", tom);
			ctx.xExecute("update users set name=#{user.name}, address=:user.address");
			Assert.assertEquals(1L,
					ctx.xQueryForObject("select count(*) from users where ${col}=#{name} and address=#{addr}",
							put0("name", "Tom"), put("addr", "China"), replace("col", "name")));
			ctx.xExecute("delete from users where name=#{u.name} or address=#{u.address}", put0("u", tom));
		}
	}

	@Test
	public void xXxxStyle_BasicTemplate() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource, new Config().setTemplateEngine(BasicSqlTemplate.instance()));
		for (int i = 0; i < REPEAT_TIMES; i++) {
			User user = new User("Sam", "Canada");
			User tom = new User("Tom", "China");
			put0("user", user);
			ctx.xExecute("insert into users (name, address) values(#{user.name},#{user.address})");
			put0("user", tom);
			ctx.xExecute("update users set name=#{user.name}, address=#{user.address}");
			Assert.assertEquals(1L,
					ctx.xQueryForObject("select count(*) from users where ${col}=#{name} and address=#{addr}",
							put0("name", "Tom"), put("addr", "China"), replace("col", "name")));
			ctx.xExecute("delete from users where name=#{u.name} or address=#{u.address}", put0("u", tom));
		}
	}

	@Test
	public void dataMapperStyle() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		for (int i = 0; i < REPEAT_TIMES; i++) {
			UserEntity user = new UserEntity();
			user.setName("Sam");
			user.setAddress("Canada");
			ctx.insert(user);
			user.setAddress("China");
			ctx.update(user);
			UserEntity sam2 = ctx.load(UserEntity.class, "Sam");
			ctx.delete(sam2);
		}
	}

	@Test
	public void activeRecordStyle() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		User user = new User();
		user.box().setContext(ctx); // set SqlBoxContent
		for (int i = 0; i < REPEAT_TIMES; i++) {
			user.setName("Sam");
			user.setAddress("Canada");
			user.insert();
			user.setAddress("China");
			user.update();
			User user2 = user.load("Sam");
			user2.delete();
		}
	}

	@Test
	public void activeRecordDefaultContext() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// use global default context
		User user = new User();
		for (int i = 0; i < REPEAT_TIMES; i++) {
			user.setName("Sam");
			user.setAddress("Canada");
			user.insert();
			user.setAddress("China");
			user.update();
			User user2 = ctx.load(User.class, "Sam");
			user2.delete();
		}
	}

	@Test
	public void activeSqlAnnotaion() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// use global default context
		User2 user = new User2();
		for (int i = 0; i < REPEAT_TIMES; i++) {
			user.insertOneUser("Sam", "Canada");
			user.updateAllUser("Tom", "China");
			List<Map<String, Object>> users = user.selectUsers("Tom", "China");
			Assert.assertEquals(1, users.size());
			user.deleteUsers("Tom", "China");
			Assert.assertEquals(0, user.ctx().nQueryForLongValue("select count(*) from users"));
		}
	}

	@Test
	public void activeSqlUseText() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// use global default context
		TextedUser user = new TextedUser();
		for (int i = 0; i < REPEAT_TIMES; i++) {
			user.insertOneUser("Sam", "Canada");
			user.updateAllUser("Tom", "China");
			List<Map<String, Object>> users = user.selectUsersByText("Tom", "China");
			Assert.assertEquals(1, users.size());
			user.deleteUsers("Tom", "China");
			Assert.assertEquals(0, user.ctx().nQueryForLongValue("select count(*) from users"));
		}
	}

	@Test
	public void activeSqlUseText2() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// use global default context
		TextedUser user = new TextedUser();
		for (int i = 0; i < REPEAT_TIMES; i++) {
			user.insertOneUser("Sam", "Canada");
			user.updateAllUser("Tom", "China");
			List<User> users2 = user.selectUsersByText2("Tom", "China");
			Assert.assertEquals(1, users2.size());
			user.deleteUsers("Tom", "China");
			Assert.assertEquals(0, user.ctx().nQueryForLongValue("select count(*) from users"));
		}
	}

	@Test
	public void abstractSqlUseText() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// use global default context
		AbstractUser user = ActiveRecord.create(AbstractUser.class);
		for (int i = 0; i < REPEAT_TIMES; i++) {
			user.insertOneUser("Sam", "Canada");
			user.updateAllUser("Tom", "China");
			List<Map<String, Object>> users = user.selectUsersByText("Tom", "China");
			Assert.assertEquals(1, users.size());
			user.deleteUsers("Tom", "China");
			Assert.assertEquals(0, user.ctx().nQueryForLongValue("select count(*) from users"));
		}
	}
}
