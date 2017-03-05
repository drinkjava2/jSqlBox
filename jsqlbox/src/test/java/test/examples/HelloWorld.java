package test.examples;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Entity;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This HelloWorld shows some key features of jSqlBox project:
 * 
 * @see jSqlBox is based on ActiveRecord design, each entity class need implement Entity interface(For JAVA8+) or
 *      extends EntityBase class(for Java7 and below).
 * @see Each entity class has a "SqlBox" type configuration, it's a Java class usually named like "xxxBOX" at same
 *      folder of entity class or just embedded inside of entity class, same concept like jBeanBox project. And usually
 *      for common entity classes there is no need explicitly write BOX configuration if followed Java Bean naming
 *      conventions, SqlBox will automatically map field name "someField" to "somefield" or "SOME_FIELD" database column
 *      name, and map "BeanName" to "BEANNAME" or "BEANNAMES" database table.
 * @see BOX configuration can be created/modified at runtime, like this example it shows change table and column name
 *      mapping at runtime.
 * @see Althoug support mutiple SqlBox contexts(= mutiple DataSource), jSqlBox recommend to use a default
 *      "SqlBoxContext" instance to simplify common database access, in this example, it wrapped in Dao class, and of
 *      cause, before use this global instance, need do some configurations (set a DataSource for it)
 * @see Without invent a new language, jSqlBox find a way to let JDBC SQL support refactoring, it's not a key feature of
 *      jSqlBox but recommended to follow this practice. And also by using SqlHelper class, it make write SQL safer and
 *      easier to wrap parameters in PreparedStatement.
 * @see (In developing) There is L1 and L2 cache for jSqlBox project works similar but not identical to Hibernate,
 *      jSqlBox does not cache update/insert/delete SQL commands, it only cache entity objects itself include modified
 *      ones. Instead of automatically send SQL commands at flushing, jSqlBox just simply empty L1 cache when dirty data
 *      will be created, jSqlBox will not sent SQL at background, it's not a entity management container but only has a
 *      entity cache maintained by user self. Entities can be send to view layer to use. Each entity has it's own box
 *      configuration, can still access database(usually read-only) even without transaction support.
 * @see There is no "lazy loading" mechanism in jSqlBox. You can get related entities by using load method or query
 *      methods at runtime.
 * @see jSqlBox based on Spring JdbcTemplate, it do the dirty job to access database, by call box().getJdbc() can get a
 *      JdbcTemplate instance to use. And also jSqlBox use Spring's declaration transaction services. In future jSqlBox
 *      may kick Spring out, but not right now, I can not find a better database tool than Spring's JdbcTemplate
 *      supporting declaration transaction.
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class HelloWorld {

	public static class User implements Entity {
		public static final String CREATE_SQL = "create table users " //
				+ "(id integer auto_increment ," //
				+ "username Varchar (50) ," //
				+ "Address Varchar (50) " //
				+ ")";
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
		ctx.execute(User.CREATE_SQL);
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