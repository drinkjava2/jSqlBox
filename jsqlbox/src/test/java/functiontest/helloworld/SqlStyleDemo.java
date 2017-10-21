package functiontest.helloworld;

import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.param;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.param0;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.question;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.question0;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.valuesQuesions;
import static com.github.drinkjava2.jdbpro.template.TemplateQueryRunner.put;
import static com.github.drinkjava2.jdbpro.template.TemplateQueryRunner.put0;
import static com.github.drinkjava2.jsqlbox.SqlBoxContext.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdbpro.template.NamedParamSqlTemplate;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Different SQL style demo
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class SqlStyleDemo {
	protected HikariDataSource dataSource;

	@Before
	public void init() {
		dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUsername("sa");
		dataSource.setPassword("");
	}

	@After
	public void cleanUp() {
		dataSource.close();
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

	@Test
	public void executeTest() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		ctx.setAllowShowSQL(true);
		for (String ddl : ctx.getDialect().toDropAndCreateDDL(User.class))
			try {
				ctx.nExecute(ddl);
			} catch (Exception e) {
			}

		System.out.println("==DbUtils old style, need close connection and catch SQLException===");
		Connection conn = null;
		try {
			conn = ctx.prepareConnection();
			ctx.execute(conn, "insert into users (name,address) values(?,?)", "Sam", "Canada");
			ctx.execute(conn, "update users set name=?, address=?", "Tom", "China");
			Assert.assertEquals(1L,
					ctx.queryForObject(conn, "select count(*) from users where name=? and address=?", "Tom", "China"));
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

		System.out.println("========= DbUtils old style, need catch SQLException========");
		try {
			ctx.execute("insert into users (name,address) values(?,?)", "Sam", "Canada");
			ctx.execute("update users set name=?, address=?", "Tom", "China");
			Assert.assertEquals(1L,
					ctx.queryForObject("select count(*) from users where name=? and address=?", "Tom", "China"));
			ctx.execute("delete from users where name=? or address=?", "Tom", "China");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("=== nXxxx methods, New JDBC style, no need catch Exception ===");
		ctx.nExecute("insert into users (name,address) values(?,?)", "Sam", "Canada");
		ctx.nExecute("update users set name=?, address=?", "Tom", "China");
		Assert.assertEquals(1L,
				ctx.nQueryForObject("select count(*) from users where name=? and address=?", "Tom", "China"));
		ctx.nExecute("delete from users where name=? or address=?", "Tom", "China");

		System.out.println("============= Ixxx methods, In-line style===================");
		ctx.iExecute("insert into users (", //
				" name ,", param0("Sam"), //
				" address ", param("Canada"), //
				") ", valuesQuesions());
		param0("Tom", "China");
		ctx.iExecute("update users set name=?,address=?");
		Assert.assertEquals(1L,
				ctx.iQueryForObject("select count(*) from users where name=? and address=?" + param0("Tom", "China")));
		ctx.iExecute("delete from users where name=", question0("Tom"), " or address=", question("China"));
		
		User sam = new User("Sam", "Canada");
		User tom = new User("Tom", "China");
		
		System.out.println("========== Another demo of In-line style=======================");
		ctx.iExecute("insert into users (", inline0(sam, "", ", ") + ") ", valuesQuesions());
		ctx.iExecute("update users set ", inline0(sam, "=?", ", "));
		Assert.assertEquals(1L, ctx.iQueryForObject("select count(*) from users where ", inline0(sam, "=?", " and ")));
		ctx.iExecute(param0(), "delete from users where ", inline(sam, "=?", " or "));
 

		System.out.println("========== Txxx methods, Template style ===================");
		put0("user", sam);
		ctx.tExecute("insert into users (name, address) values(#{user.name},#{user.address})");
		put0("user", tom);
		ctx.tExecute("update users set name=#{user.name}, address=#{user.address}");
		Assert.assertEquals(1L,
				ctx.tQueryForObject("select count(*) from users where ${col}=#{name} and address=#{addr}",
						put0("name", "Tom"), put("addr", "China"), replace("col", "name")));
		ctx.tExecute("delete from users where name=#{u.name} or address=#{u.address}", put0("u", tom));

		System.out.println("========== Txxx methods, Template style, another template Engine ===========");
		ctx.setSqlTemplateEngine(NamedParamSqlTemplate.instance());
		put0("user", sam);
		ctx.tExecute("insert into users (name, address) values(:user.name,:user.address)");
		put0("user", tom);
		ctx.tExecute("update users set name=:user.name, address=:user.address");
		Assert.assertEquals(1L, ctx.tQueryForObject("select count(*) from users where ${col}=:name and address=:addr",
				put0("name", "Tom"), put("addr", "China"), replace("col", "name")));
		ctx.tExecute("delete from users where name=:u.name or address=:u.address", put0("u", tom));

		System.out.println("================ Data Mapper style =================");
		ctx.insert(sam);// insert
		sam.setAddress("China");
		ctx.update(sam);// update
		User sam_1 = ctx.load(User.class, "Sam");// load
		ctx.delete(sam_1);// delete

		System.out.println("=============== ActiveRecord style ================");
		User sam2 = new User("Sam", "Canada");
		sam2.box().setContext(ctx); // set SqlBoxContent instance here
		sam2.insert();
		sam2.setAddress("China");
		sam2.update();
		User sam3 = sam2.load("Sam");
		sam3.delete();

		System.out.println("========= ActiveRecord style but use default global SqlBoxContext instance========");
		SqlBoxContext.setDefaultContext(ctx);
		User sam4 = new User("Sam", "Canada");
		sam4.insert();
		sam4.setAddress("China");
		sam4.update();
		User sam5 = ctx.load(User.class, "Sam");
		sam5.delete();
	}

}
