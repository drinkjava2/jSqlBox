package test.jdbc;

import static com.github.drinkjava2.jsqlbox.SqlHelper.e;
import static com.github.drinkjava2.jsqlbox.SqlHelper.q;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.config.Config;

public class JdbcTest {
	@Before
	public void recreateDatabase() {
		Config.recreateDatabase();
	}

	public void tx_JdbcDemo() {
		Dao.dao.execute("insert user (username,age) values(" + q("user1") + "," + q(10) + ")");
		Dao.dao.execute("insert user (username,age) values(" + q("user2", 20) + ")");
		Dao.dao.execute("insert user (username,age) values(?,?)" + e("user3") + e(30));
		Dao.dao.execute("insert user (username,age) values(?,?)" + e("user4", 40));
		Dao.dao.execute("insert user ", //
				" (username", e("Andy"), //
				", address", e("Guanzhou"), //
				", age)", e("60"), //
				" values(?,?,?)");
		Dao.dao.execute("update user set username=?,address=? " + e("Sam", "BeiJing") + " where age=" + q(10));
		Dao.dao.execute("update user set username=", q("John"), ",address=", q("Shanghai"), " where age=", q(20));
		Dao.dao.execute("insert user set", //
				" username=?", e("Peter"), //
				",address=? ", e("Nanjing")); //
		Dao.dao.execute("update user set", //
				" username=?", e("Tom"), //
				",address=? ", e("Nanjing"), //
				" where age=?", e(30));
		Dao.dao.execute("update user set", //
				" username=", q("Jeffery"), //
				",address=", q("Tianjing"), //
				" where age=", q(40));
		System.out.println("tx_JdbcDemo Done");
	}

	public void tx_BatchInsertDemo() {
		for (int i = 0; i < 10009; i++)
			Dao.dao.cacheSQL("insert user (username", e("user" + i), ",age", e("70"), ") values(?,?)");
		Dao.dao.executeCachedSQLs();
		System.out.println("tx_BatchInsertDemo Done");
	}

	@Test
	public void tx_main() {
		tx_JdbcDemo();
		tx_BatchInsertDemo();
	}

	public void test() {
		JdbcTest tester = BeanBox.getBean(JdbcTest.class);
		tester.tx_main();
	}

}