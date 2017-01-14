package test.examples;

import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.id.AutoGenerator;

import test.config.JBeanBoxConfig.DefaultSqlBoxContextBox;
import test.config.po.User;

/**
 * This HelloWorld shows some key features of jSqlBox project:
 * 
 * @see jSqlBox is based on ActiveRecord design, each entity class need implement IEntity interface(For JAVA8+) or
 *      extends EntityBase class(for Java7 and below), or at minimum requirement, put a box() method in a pure POJO
 *      class if you don't want implement or extend any other classes, like below: <br/>
 *      public SqlBox box() { <br/>
 *      return SqlBox.getBox(this);<br/>
 *      }<br/>
 * @see Each entity class has a "SqlBox" type configuration, it's a Java class usually naming like "xxxBOX" at same
 *      folder of entity class or just embedded inside of entity class, same concept like jBeanBox project. And usually
 *      for common entity classes there is no need explicitly write BOX configuration if followed Java Bean naming
 *      conventions, SqlBox will automatically map field name "someField" to "somefield" or "SOME_FIELD" database column
 *      name.
 * @see BOX configuration can be created/modified at runtime, like this example it shows change table and column name
 *      mapping at runtime.
 * @see Althoug support mutiple SqlBox contexts(= mutiple DataSource), jSqlBox recommend to use a default
 *      "SqlBoxContext" instance to simplify common database access, in this example, it wrapped in Dao class, and of
 *      cause, before use this global instance, need do some configurations(set DataSource for it)
 * @see Without invent a new language, jSqlBox find a way to make JDBC SQL support refactoring, it's not a key feature
 *      of jSqlBox but recommended to follow this practice. And also by using SqlHelper class, it make write SQL safer
 *      and easier.
 * @see There is L1 and L2 cache for jSqlBox project works similar but not identical to Hibernate, jSqlBox does not
 *      cache update/insert/delete SQL commands, it only cache entity objects itself include modified ones. Instead of
 *      automatically send SQL commands at flushing, jSqlBox just simply empty L1 cache when dirty data will be found,
 *      jSqlBox will not sent SQL behind you, it's not a entity management container, it's only a entity cache. entity
 *      can be send to view layer to use. Each entity has it's own box configuration, can still access database(usually
 *      read-only) even without transaction support.
 * @see There is no "one to many", "many to one", "lazy loading" concept in jSqlBox. You can get related entities by
 *      using query()/load() methods at runtime easily, and they will be cached in L1/L2 cache for better performance.
 *      There is a "DB.class" be used to help navigate between entities in query result.
 * @see jSqlBox based on Spring JdbcTemplate do the dirty lower layer database access job, by call box().getJdbc() can
 *      get a JdbcTemplate instance to use. And also jSqlBox use Spring's declaration transaction services. In future
 *      jSqlBox may kick Spring out, but not right now.
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class HelloWorld {

	@Test
	public void doTest() {
		System.out.println("===============================Testing HelloWorld===============================");
		SqlBoxContext.setDefaultSqlBoxContext(BeanBox.getBean(DefaultSqlBoxContextBox.class));
		Dao.execute("delete from users");
		Dao.execute("delete from users2");

		User u = new User();// default use users database table
		u.setUserName("James");
		u.insert();

		User u2 = Dao.load(User.class, u.getId());// NO SQL be sent, load from L1 cache
		Assert.assertEquals("James", u2.getUserName());

		u.box().configTable("users2");// to use database table "users2"
		u.box().configIdGenerator(u.ID(), AutoGenerator.INSTANCE);
		u.box().configColumnName("userName", u.ADDRESS()); // to map userName field to address column
		u.setUserName("Tom");
		u.insert();
		Assert.assertEquals("Tom",
				Dao.queryForString("select address from ", u.table(), " where ", u.USERNAME(), "=", q("Tom")));
		// SQL is: select address from USERS2 where ADDRESS=?

	}
}