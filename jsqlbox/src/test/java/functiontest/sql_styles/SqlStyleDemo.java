package functiontest.sql_styles;

import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.inline;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.inline0;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.param;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.param0;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.question;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.question0;
import static com.github.drinkjava2.jdbpro.inline.InlineQueryRunner.valuesQuesions;
import static com.github.drinkjava2.jdbpro.template.TemplateQueryRunner.put;
import static com.github.drinkjava2.jdbpro.template.TemplateQueryRunner.put0;
import static com.github.drinkjava2.jdbpro.template.TemplateQueryRunner.replace;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.Id;
import com.github.drinkjava2.jdialects.annotation.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Different SQL style demo
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class SqlStyleDemo  {
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

		for (String ddl : ctx.pojos2CreateDDLs(User.class))
			ctx.nExecute(ddl);

		User user = new User();
		user.setName("Sam");
		user.setAddress("Canada");

		System.out.println("===============================================================");
		System.out.println("DbUtils old style, need close connection and catch SQLException");
		Connection conn = null;
		try {
			conn = ctx.prepareConnection();
			ctx.execute(conn, "insert into users (name,address) values(?,?)", "Sam", "Canada");
			ctx.execute(conn, "update users set name=?, address=?", "Sam", "Canada");
			Assert.assertEquals(1L,
					ctx.queryForObject(conn, "select count(*) from users where name=? and address=?", "Sam", "Canada"));
			ctx.execute(conn, "delete from users where name=? or address=?", "Sam", "Canada");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				ctx.close(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		System.out.println("===============================================================");
		System.out.println("DbUtils old style, need catch SQLException");
		try {
			ctx.execute("insert into users (name,address) values(?,?)", "Sam", "Canada");
			ctx.execute("update users set name=?, address=?", "Sam", "Canada");
			Assert.assertEquals(1L,
					ctx.queryForObject("select count(*) from users where name=? and address=?", "Sam", "Canada"));
			ctx.execute("delete from users where name=? or address=?", "Sam", "Canada");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("===============================================================");
		System.out.println("nXxxx methods, new Sql style, no need catch SQLException");
		ctx.nExecute("insert into users (name,address) values(?,?)", "Sam", "Canada");
		ctx.nExecute("update users set name=?, address=?", "Sam", "Canada");
		Assert.assertEquals(1L,
				ctx.nQueryForObject("select count(*) from users where name=? and address=?", "Sam", "Canada"));
		ctx.nExecute("delete from users where name=? or address=?", "Sam", "Canada");

		System.out.println("===============================================================");
		System.out.println("iXxxx methods, In-line style");
		ctx.iExecute("insert into users (", //
				" name ,", param0("Sam"), //
				" address ", param("Canada"), //
				") ", valuesQuesions());
		param0("Sam", "Canada");
		ctx.iExecute("update users set name=?,address=?");
		Assert.assertEquals(1L, ctx.iQueryForObject("select count(*) from users where name=" + question0("Sam")));
		ctx.iExecute("delete from users where name=", question0("Sam"), " and address=", question("Canada"));

		System.out.println("===============================================================");
		System.out.println("Another demo of In-line style");
		ctx.iExecute("insert into users (", inline0(user, "", ", ") + ") ", valuesQuesions());
		ctx.iExecute("update users set ", inline0(user, "=?", ", "));
		Assert.assertEquals(1L, ctx.iQueryForObject("select count(*) from users where ", inline0(user, "=?", " and ")));
		ctx.iExecute(param0(), "delete from users where ", inline(user, "=?", " or "));

		System.out.println("===============================================================");
		System.out.println("tXxxx methods, Template style");
		put0("user", user);
		ctx.tExecute("insert into users (name, address) values(#{user.name},#{user.address})");
		put0("name", "Sam");
		put("addr", "Canada");
		ctx.tExecute("update users set name=#{name}, address=#{addr}");
		Assert.assertEquals(1L,
				ctx.tQueryForObject("select count(*) from users where ${col}=#{name} and address=#{addr}",
						put0("name", "Sam"), put("addr", "Canada"), replace("col", "name")));
		ctx.tExecute("delete from users where name=#{name} or address=#{addr}", put0("name", "Sam"),
				put("addr", "Canada"));

		System.out.println("===============================================================");
		System.out.println("Data Mapper style");
		ctx.insert(user);
		user.setName("Sam");
		user.setAddress("Canada");
		ctx.update(user);
		User user2 = ctx.load(User.class, "Sam");
		ctx.delete(user2);

		System.out.println("===============================================================");
		System.out.println("ActiveRecord style");
		SqlBoxContext.setDefaultContext(ctx);
		System.out.println(user.box());
		System.out.println(user.context());
		user.insert();
		user.setName("Sam");
		user.setAddress("Canada");
		user.update();
		User user3 = user.load("Sam");
		user3.delete();
	}

}
