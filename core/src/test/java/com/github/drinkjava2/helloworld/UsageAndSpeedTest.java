package com.github.drinkjava2.helloworld;

import static com.github.drinkjava2.jdbpro.JDBPRO.PARA;
import static com.github.drinkjava2.jdbpro.JDBPRO.PARA0;
import static com.github.drinkjava2.jdbpro.JDBPRO.PARAMS;
import static com.github.drinkjava2.jdbpro.JDBPRO.QUES;
import static com.github.drinkjava2.jdbpro.JDBPRO.VALUESQUES;
import static com.github.drinkjava2.jdbpro.JDBPRO.bind;
import static com.github.drinkjava2.jdbpro.JDBPRO.notNull;
import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jdbpro.JDBPRO.question;
import static com.github.drinkjava2.jdbpro.JDBPRO.sql;
import static com.github.drinkjava2.jdbpro.JDBPRO.valuesQuestions;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdbpro.IocTool;
import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.springsrc.utils.ClassUtils;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
import com.github.drinkjava2.jsqlbox.annotation.New;
import com.github.drinkjava2.jsqlbox.annotation.Sql;
import com.zaxxer.hikari.HikariDataSource;

import activerecordtext.AbstractUser;
import activerecordtext.TextedUser;

/**
 * Usage of different SQL style and speed test
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class UsageAndSpeedTest {
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
		// SqlBoxContext.setGlobalAllowShowSql(true);
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(null);
		for (String ddl : ctx.getDialect().toDropAndCreateDDL(UserAR.class))
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
		try {
			PRINT_TIMEUSED = false;
			REPEAT_TIMES = 1;// warm up
			runTestMethods();
			PRINT_TIMEUSED = true;
			REPEAT_TIMES = 1;// Change to 10000 to do speed test
			System.out.println("Compare method execute time for repeat " + REPEAT_TIMES + " times:");
			runTestMethods();
		} finally {
			PRINT_TIMEUSED = false;
			REPEAT_TIMES = 1;
		}
	}

	private void runTestMethods() throws Exception {
		runMethod("pureJdbc");
		runMethod("xxxxStyleWithConnection");
		runMethod("xxxxStyle");
		runMethod("nXxxStyle");
		runMethod("iXxxStyle");
		runMethod("pXxxStyle");
		runMethod("INLINEmethods");
		runMethod("tXxxStyle");
		runMethod("dataMapperStyle");
		runMethod("activeRecordStyle");
		runMethod("activeRecordDefaultContext");
		runMethod("sqlMapperUseText");
		runMethod("sqlMapperSqlAnnotaion");
		runMethod("abstractSqlMapperUseText");
	}

	public void runMethod(String methodName) throws Exception {
		Method m = ClassUtils.getMethod(this.getClass(), methodName);
		long start = System.currentTimeMillis();
		m.invoke(this);
		long end = System.currentTimeMillis();
		String timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		if (PRINT_TIMEUSED)
			System.out.println(String.format("%35s: %6s s", methodName, timeused));
	}

	@Table(name = "users")
	public static class UserPOJO {
		@Id
		String name;
		String address;

		public UserPOJO() {
		}

		public UserPOJO(String name, String address) {
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

	@Table(name = UserAR.TABLE)
	public static class UserAR extends ActiveRecord {
		public static final String TABLE = "users";
		public static final String NAME = "name";
		public static final String ADDRESS = "address";

		@Id
		@Column(name = "name")
		String name;
		String address;
		Integer age;

		public UserAR() {
		}

		public UserAR(String name, String address) {
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

	public static class UserMapper extends UserAR {
		@Sql("insert into users (name,address) values(?,?)")
		public void insertOneUser(String name, String address) {
			this.guess(name, address);
		};

		@Sql("update users set name=?, address=?")
		public void updateAllUser(String name, String address) {
			this.guess(name, address);
		};

		@New(MapListHandler.class)
		@Sql("select * from users where name=? and address=?")
		public List<Map<String, Object>> selectUsers(String name, String address) {
			return this.guess(name, address);
		};

		@New(MapListHandler.class)
		@Sql("select * from users where name=:name and address=:address")
		public List<Map<String, Object>> selectUsersBindParam(String name, String address) {
			return this.guess(bind("name", name, "address", address));
		};

		@New(MapListHandler.class)
		@Sql("select * from users where name='${name}' and address=:address")
		public List<Map<String, Object>> selectUsersUnbindParam(String name, String address) {
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
	public void xxxxStyleWithConnection() {
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
	public void xxxxStyle() {
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

	/**
	 * INLINE Methods are designed for old DAO tools which only allow 1 SQL string
	 * in parameter list
	 */
	@Test
	public void INLINEmethods() {
		QueryRunner runner = new QueryRunner(dataSource);
		final String name = "Tom";
		final String age = null;
		for (int i = 0; i < REPEAT_TIMES; i++) {
			try {
				runner.execute("insert into users (" //
						+ " name " + PARA0("Sam") //
						+ " ,address" + PARA("Canada") //
						+ ")"//
						+ VALUESQUES(), PARAMS());
				runner.execute("update users set " + PARA0() //
						+ (name == null ? "" : "name=" + QUES("Tom")) //
						+ (age == null ? "" : "age=" + QUES(age)) //
						+ ", address=" + QUES("China")//
						, PARAMS());
				PARA0("Tom", "China");
				Assert.assertEquals(1L, (long) runner.query("select count(*) from users where name=? and address=?",
						new ScalarHandler<Long>(), PARAMS()));
				runner.execute("delete from users where name=? or address=?" + PARA0("Tom", "China"), PARAMS());
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
					notNull(" name ,", "Sam"), //
					notNull(" someother ,", null), //
					" address ", param("Canada"), //
					") ", valuesQuestions());
			ctx.iExecute("update users set name=?,address=?", param("Tom", "China"));
			Assert.assertEquals(1L, ctx.iQueryForObject("select count(*) from users where name=? and address=?",
					param("Tom", "China")));
			ctx.iExecute("delete from users where name=", question("Tom"), " or address=", question("China"));
		}
	}

	@Test
	public void pXxxStyle() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		for (int i = 0; i < REPEAT_TIMES; i++) {
			ctx.pExecute("insert into users (name,address,age) ", "Sam", "Canada", 10, valuesQuestions());
			ctx.pExecute("update users set name=?", "Tom", sql(", address=?"), "China", sql(", age=?"), null);
			Assert.assertEquals(1L, ctx.pQueryForObject(
					"select count(*) from users where name=? and address=? and age is ?", "Tom", "China", null));
			ctx.pExecute("delete from users where name=? or address=?", "Tom", "China");
		}
	}

	@Test
	public void tXxxStyle() {
		SqlBoxContext ctx2 = new SqlBoxContext(dataSource);
		Map<String, Object> paramMap = new HashMap<String, Object>();
		for (int i = 0; i < REPEAT_TIMES; i++) {
			UserAR sam = new UserAR("Sam", "Canada");
			UserAR tom = new UserAR("Tom", "China");
			paramMap.put("user", sam);
			ctx2.tExecute("insert into users (name, address) values(#{user.name},:user.address)", paramMap);
			ctx2.tExecute("update users set name=#{user.name}, address=:user.address", bind("user", tom));
			Assert.assertEquals(1L,
					ctx2.tQueryForObject("select count(*) from users where name=#{name} and address=:addr",
							bind("name", "Tom", "addr", "China")));
			ctx2.tExecute("delete from users where "//
					, " name=:name ", bind("name", "Tom")//
					, " or address=#{address}", bind("address", "China")//
			);
		}
	}

	@Test
	public void dataMapperStyle() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		for (int i = 0; i < REPEAT_TIMES; i++) {
			UserPOJO user = new UserPOJO();
			user.setName("Sam");
			user.setAddress("Canada");
			ctx.insert(user);
			user.setAddress("China");
			ctx.update(user);
			UserPOJO sam2 = ctx.loadById(UserPOJO.class, "Sam");
			ctx.delete(sam2);
		}
	}

	@Test
	public void activeRecordStyle() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		UserAR user = new UserAR();
		user.useContext(ctx); // Use ctx as SqlBoxContext
		for (int i = 0; i < REPEAT_TIMES; i++) {
			user.setName("Sam");
			user.setAddress("Canada");
			user.insert();
			user.setAddress("China");
			user.update();
			UserAR user2 = new UserAR().useContext(ctx).loadById("Sam");
			user2.delete();
		}
	}

	@Test
	public void activeRecordDefaultContext() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// use global default context
		UserAR user = new UserAR();
		for (int i = 0; i < REPEAT_TIMES; i++) {
			user.setName("Sam");
			user.setAddress("Canada");
			user.insert();
			user.setAddress("China");
			user.update();
			UserAR user2 = ctx.loadById(UserAR.class, "Sam");
			user2.delete();
		}
	}

	@Test
	public void sqlMapperSqlAnnotaion() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// use global default context
		UserMapper user = new UserMapper();
		for (int i = 0; i < REPEAT_TIMES; i++) {
			user.insertOneUser("Sam", "Canada");
			user.updateAllUser("Tom", "China");
			List<Map<String, Object>> users = user.selectUsers("Tom", "China");
			Assert.assertEquals(1, users.size());
			user.deleteUsers("Tom", "China");
			Assert.assertEquals(0, user.ctx().pQueryForLongValue("select count(*) from users"));
		}
	}

	@Test
	public void sqlMapperUseText() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// use global default context
		TextedUser user = new TextedUser();
		for (int i = 0; i < REPEAT_TIMES; i++) {
			user.insertOneUser("Sam", "Canada");
			user.ctx().pUpdate(user.updateAllUserPreSql("Tom", "China"));
			List<Map<String, Object>> u1 = user.selectUsersMapListByText("Tom", "China");
			Assert.assertEquals(1, u1.size());
			user.deleteUsers("Tom", "China");
			Assert.assertEquals(0, user.ctx().pQueryForLongValue("select count(*) from users"));
		}
	}

	@Test
	public void sqlMapperUseText2() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// use global default context
		TextedUser user = new TextedUser();
		for (int i = 0; i < REPEAT_TIMES; i++) {
			user.insertOneUser("Sam", "Canada");
			user.ctx().iUpdate(user.updateAllUserPreSql("Tom", "China"));
			List<Map<String, Object>> u1 = user.selectUsersMapListByText("Tom", "China");
			Assert.assertEquals(1, u1.size());

			List<Map<String, Object>> u2 = user.selectUsersMapListByText2("Tom", "China");
			Assert.assertEquals(1, u2.size());

			List<TextedUser> u3 = user.selectUsersByText2("Tom", "China");
			Assert.assertEquals(1, u3.size());

			List<TextedUser> u4 = user.selectUsersByText3("Tom", "China");
			Assert.assertEquals(1, u4.size());

			user.deleteUsers("Tom", "China");
			Assert.assertEquals(0, user.ctx().pQueryForLongValue("select count(*) from users"));
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void abstractSqlMapperUseText() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		ctx.setIocTool(new IocTool() {
			public <T> T getBean(Class<?> configClass) {
				return BeanBox.getBean(configClass);
			}
		});
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// use global default context
		AbstractUser mapper = SqlBoxContext.createMapper(AbstractUser.class);
		for (int i = 0; i < REPEAT_TIMES; i++) {
			mapper.insertOneUser("Sam", "Canada");
			mapper.updateUserPreparedSQL("Tom", "China");
			List<Map<String, Object>> users = mapper.selectUserListMap("Tom", "China");
			Assert.assertEquals(1, users.size());
			List<TextedUser> users2 = mapper.selectAbstractUserListUnBind("Tom", "China");
			Assert.assertEquals(1, users2.size());
			mapper.deleteUsers("Tom", "China");
			Assert.assertEquals(0, mapper.ctx().pQueryForLongValue("select count(*) from	 users"));
		}
	}

	protected void BelowNotForSpeedTest_______________________() {
		// below methods are test usages only, not join to speed test
	}

	@Test
	public void tXxxUseAnotherSqlTemplateEngine() {
		SqlBoxContextConfig config = new SqlBoxContextConfig();
		config.setTemplateEngine(new BasicSqlTemplate("[", "]", true, true));
		SqlBoxContext ctx = new SqlBoxContext(dataSource, config);
		UserAR user = new UserAR("Sam", "Canada");
		UserAR tom = new UserAR("Tom", "China");
		ctx.tExecute("insert into users (name, address) values([user.name], [user.address])", bind("user", user));
		ctx.tExecute("update users set name=[user.name], address=[user.address]", bind("user", tom));
		Assert.assertEquals(1L,
				ctx.tQueryForObject("select count(*) from users where ${col}= [name] and address=[addr]",
						bind("name", "Tom"), bind("addr", "China"), bind("$col", "name")));
		ctx.tExecute("delete from users where ${nm}='${t.name}' or address=:u.address", bind("u", tom), bind("$t", tom),
				bind("$nm", "name"));
	}

	@Test
	public void tXxxDynamicChangeTemplateEngine() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlTemplateEngine engine = new BasicSqlTemplate("[", "]", true, true);
		UserAR user = new UserAR("Sam", "Canada");
		UserAR tom = new UserAR("Tom", "China");
		ctx.tExecute("insert into users (name, address) values(#{user.name}, #{user.address})", bind("user", user));
		ctx.tExecute(engine, "update users set name=[user.name], address=[user.address]", bind("user", tom));
		Assert.assertEquals(1L,
				ctx.tQueryForObject(engine, "select count(*) from users where ${col}= [name] and address=[addr]",
						bind("name", "Tom"), bind("addr", "China"), bind("$col", "name")));
		ctx.tExecute("delete from users where ${nm}='${t.name}' or address=:u.address", bind("u", tom), bind("$t", tom),
				bind("$nm", "name"), engine);
	}

	/** Use const String can make SQL support Java Bean field refactoring */
	@Test
	public void iXxxxSupportRefactor() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		ctx.iExecute("insert into ", UserAR.TABLE, " ( ", //
				UserAR.NAME, ",", param("Sam"), //
				UserAR.ADDRESS, " ", param("Canada"), //
				") ", valuesQuestions());
		ctx.iExecute("delete from users where ", //
				UserAR.NAME, "=", question("Sam"), //
				" or ", UserAR.ADDRESS, "=", question("Canada")//
		);
	}

	@Test
	public void activeRecordLoadByIdMap() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		UserAR user = new UserAR();
		user.useContext(ctx); // Use ctx as SqlBoxContext
		user.setName("Sam");
		user.setAddress("Canada");
		user.insert();
		user.setAddress("China");
		user.update();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", "Sam");
		UserAR user2 = new UserAR().useContext(ctx).loadById(map);
		user2.delete();
	}

	@Test
	public void activeRecordLoadByQuery() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		UserAR user = new UserAR();
		user.useContext(ctx); // Use ctx as SqlBoxContext
		user.setName("Sam");
		user.setAddress("Canada");
		user.insert();
		UserAR user2 = new UserAR().useContext(ctx).loadByQuery("select * from ", UserAR.TABLE);
		Assert.assertEquals("Sam", user2.getName());
	}

	@Test
	public void dataMapperCrudTest() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		// ctx.setAllowShowSQL(true);
		UserAR user = new UserAR();
		for (int i = 1; i <= 10; i++) {
			user.setName("Tom" + i);
			user.setAddress("China" + i);
			ctx.insert(user);
		}
		user = new UserAR();
		user.setName("Tom8");
		ctx.load(user);
		Assert.assertEquals("China8", user.getAddress());

		user = ctx.loadById(UserAR.class, "Tom7");
		Assert.assertEquals("China7", user.getAddress());

		user.setAddress("Canada");
		ctx.update(user);
		Assert.assertEquals("Canada",  ctx.loadById(UserAR.class, "Tom7").getAddress());

		ctx.delete(user);
		ctx.delete(user, " or name=?", param("Tom2"));

		Assert.assertEquals(7, ctx.loadAll(UserAR.class, " where name>?", param("Tom1")).size());
	}

	@Test
	public void conditionsQuery() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		final String name = "Tom";
		final String age = null;
		final String address = "China";
		ctx.iExecute("insert into users (", //
				notNull(" name", name), //
				notNull(" ,age ", age), //
				" ,address ", param(address), //
				") ", valuesQuestions());
		ctx.pExecute("update users set ", //
				notNull(" name", "=", "?, ", name), //
				notNull(" age=?,", age), //
				sql(" address=? "), address //
		);
		Assert.assertEquals(1L, ctx.iQueryForLongValue(//
				"select count(*) from users where 1=1 ", //
				notNull(" and name=? ", name), //
				"Someother".equals(name) ? ctx.iPrepare(" and Someother>?  ", param(name)) : "", //
				"China".equals(address) ? ctx.pPrepare(" and address=?  ", address) : ""//
		));
		ctx.nExecute("delete from users");
	}

	@Test
	public void sqlMapperSqlAnnobindParam() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// use global default context
		UserMapper user = new UserMapper();
		user.insertOneUser("Sam", "Canada");
		user.updateAllUser("Tom", "China");
		List<Map<String, Object>> users = user.selectUsersBindParam("Tom", "China");
		Assert.assertEquals(1, users.size());
	}
 
	@Test
	public void sqlMapperSqlAnnoUnbindParam() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		//ctx.setAllowShowSQL(true);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// use global default context
		UserMapper user = new UserMapper();
		user.insertOneUser("Sam", "Canada");
		user.updateAllUser("Tom", "China");
		List<Map<String, Object>> users = user.selectUsersUnbindParam("Tom", "China");
		Assert.assertEquals(1, users.size());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void abstractSqlMapperUseTextUnbindAndBind() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// use global default context
		ctx.setIocTool(new IocTool() {
			public <T> T getBean(Class<?> configClass) {
				return BeanBox.getBean(configClass);
			}
		});
		AbstractUser mapper = SqlBoxContext.createMapper(AbstractUser.class);
		mapper.insertOneUser("Sam", "Canada");
		mapper.updateUserPreparedSQL("Tom", "China");
		List<TextedUser> users2 = mapper.selectAbstractUserListUnBind("Tom", "China");
		Assert.assertEquals(1, users2.size());

		TextedUser u = users2.get(0);
		List<TextedUser> users3 = mapper.selectAbstractUserListBind("Tom", u);
		Assert.assertEquals(1, users3.size());
	}
}
