package functiontest.speedtest;

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
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdbpro.template.NamedParamSqlTemplate;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.springsrc.utils.ClassUtils;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Different SQL style demo
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class SpeedTest {
	protected HikariDataSource dataSource;
	SqlBoxContext ctx;

	@Before
	public void init() {
		dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUsername("sa");// change to your user & password
		dataSource.setPassword("");
		ctx = new SqlBoxContext(dataSource);
		ctx.setAllowShowSQL(false);
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
	public void executeTest() throws Exception {
		Long times = 10000L;
		System.out.println("Compare method execute time for repeat " + times + " times:");
		runMethod("pureJdbc", times);
		runMethod("dbUtilsWithConnMethod", times);
		runMethod("dbUtilsNoConnMethod", times);
		runMethod("nXxxJdbcStyle", times);
		runMethod("iXxxInlineStyle", times);
		runMethod("tXxxTemplateStyle", times);		
		runMethod("tXxxTemplateAndInlineStyle", times);
		runMethod("tXxxNamingParamTemplateStyle", times);
		runMethod("dataMapperStyle", times);
		runMethod("activeRecordStyle", times);
		runMethod("activeRecordDefaultContext", times); 
	}

	@Table(name = "users")
	public static class UserEntity {
		@Id
		String name;
		String address;
		SqlBox box;

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

	public void runMethod(String methodName, Long times) throws Exception {
		Method m = ClassUtils.getMethod(this.getClass(), methodName, Long.class);
		m.invoke(this, 5L);// warm up 500 times first
		long start = System.currentTimeMillis();
		m.invoke(this, times);
		long end = System.currentTimeMillis();
		String timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
		System.out.println(String.format("%28s: %6s s", methodName, timeused));
	}

	public void pureJdbc(Long times) {
		for (int i = 0; i < times; i++) {
			Connection conn = null;
			PreparedStatement pst = null;
			ResultSet rs = null;
			try {
				conn = dataSource.getConnection();
				pst = conn.prepareStatement("insert into users (name,address) values(?,?)");
				pst.setString(1, "Sam");
				pst.setString(2, "Canada");
				pst.execute();

				pst = conn.prepareStatement("update users set name=?, address=?");
				pst.setString(1, "Tom");
				pst.setString(2, "China");
				pst.execute();

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

	public void dbUtilsWithConnMethod(Long times) {
		for (int i = 0; i < times; i++) {
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

	public void dbUtilsNoConnMethod(Long times) {
		for (int i = 0; i < times; i++) {
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

	public void nXxxJdbcStyle(Long times) {
		for (int i = 0; i < times; i++) {
			ctx.nExecute("insert into users (name,address) values(?,?)", "Sam", "Canada");
			ctx.nExecute("update users set name=?, address=?", "Tom", "China");
			Assert.assertEquals(1L,
					ctx.nQueryForObject("select count(*) from users where name=? and address=?", "Tom", "China"));
			ctx.nExecute("delete from users where name=? or address=?", "Tom", "China");
		}
	}

	public void iXxxInlineStyle(Long times) {
		for (int i = 0; i < times; i++) {
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

	public void tXxxTemplateStyle(Long times) {
		Map<String, Object> params=new HashMap<String, Object>();
		for (int i = 0; i < times; i++) {
			User sam = new User("Sam", "Canada");
			User tom = new User("Tom", "China");
			params.put("user", sam);
			ctx.tExecute(params,"insert into users (name, address) values(#{user.name},#{user.address})");
			params.put("user", tom); 
			ctx.tExecute(params,"update users set name=#{user.name}, address=#{user.address}");
			params.clear();
			params.put("name", "Tom");
			params.put("addr", "China");
			Assert.assertEquals(1L,
					ctx.tQueryForObject(params,"select count(*) from users where name=#{name} and address=#{addr}")); 
			params.put("u", tom);
			ctx.tExecute(params, "delete from users where name=#{u.name} or address=#{u.address}");
		}
	}
	

	public void tXxxTemplateAndInlineStyle(Long times) {
		for (int i = 0; i < times; i++) {
			User user = new User("Sam", "Canada");
			User tom = new User("Tom", "China");
			put0("user", user);
			ctx.tExecute("insert into users (name, address) values(#{user.name},#{user.address})");
			put0("user", tom);
			ctx.tExecute("update users set name=#{user.name}, address=#{user.address}");
			Assert.assertEquals(1L,
					ctx.tQueryForObject("select count(*) from users where ${col}=#{name} and address=#{addr}",
							put0("name", "Tom"), put("addr", "China"), replace("col", "name")));
			ctx.tExecute("delete from users where name=#{u.name} or address=#{u.address}", put0("u", tom));
		}
	}

	public void tXxxNamingParamTemplateStyle(Long times) {
		for (int i = 0; i < times; i++) {
			User user = new User("Sam", "Canada");
			User tom = new User("Tom", "China");
			ctx.setSqlTemplateEngine(NamedParamSqlTemplate.instance());
			put0("user", user);
			ctx.tExecute("insert into users (name, address) values(:user.name,:user.address)");
			ctx.tExecute("update users set name=:user.name, address=:user.address"+put0("user",tom));
			Assert.assertEquals(1L,
					ctx.tQueryForObject("select count(*) from users where ${col}=:name and address=:addr",
							put0("name", "Tom"), put("addr", "China"), replace("col", "name")));
			ctx.tExecute("delete from users where name=:u.name or address=:u.address", put0("u", tom));
		}
	}

	public void dataMapperStyle(Long times) {
		for (int i = 0; i < times; i++) {
			UserEntity  user = new  UserEntity();
			user.setName("Sam");
			user.setAddress("Canada");
			ctx.insert(user);
			user.setAddress("China");
			ctx.update(user);
			UserEntity sam2 = ctx.load(UserEntity.class, "Sam");
			ctx.delete(sam2);
		}
	}

	public void activeRecordStyle(Long times) {
		User user = new User();
		user.box().setContext(ctx); // set SqlBoxContent
		for (int i = 0; i < times; i++) {
			user.setName("Sam");
			user.setAddress("Canada");
			user.insert();
			user.setAddress("China");
			user.update();
			User user2 = user.load("Sam");
			user2.delete();
		}
	}

	public void activeRecordDefaultContext(Long times) {
		SqlBoxContext.setDefaultContext(ctx);// use global default context
		User user = new User();
		for (int i = 0; i < times; i++) {
			user.setName("Sam");
			user.setAddress("Canada");
			user.insert();
			user.setAddress("China");
			user.update();
			User user2 = ctx.load(User.class, "Sam");
			user2.delete();
		}
	}

}
