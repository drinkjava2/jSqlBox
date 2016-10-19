package test.test1_basic_crud;

import static com.github.drinkjava2.jsqlbox.SQLHelper.e;
import static com.github.drinkjava2.jsqlbox.SQLHelper.q;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.test1_basic_crud.po.User;
import static test.test1_basic_crud.po.User.*;

public class Tester {
	public void tx_CrudDemo() {
		User user = new User();
		user.setUsername("Yong");
		user.setAddress("Nanjing");
		user.setAge(5);
		user.dao().save();
		System.out.println("tx_CrudDemo Done");
	}

	public void tx_JdbcDemo() {
		Dao.dao.execute("insert user (username,age) values(" + q("user1") + "," + q(10) + ")");
		Dao.dao.execute("insert user (username,age) values(" + q("user2", 20) + ")");
		Dao.dao.execute("insert user (username,age) values(?,?)" + e("user3") + e(30));
		Dao.dao.execute("insert user (username,age) values(?,?)" + e("user4", 40));
		Dao.dao.execute("insert " + USER + " (" + UserName + "," + Age + ") values(" + q("user5", 50) + ")");
		Dao.dao.execute("insert user ", //
				" (username", e("Andy"), //
				", address", e("Guanzhou"), //
				", age)", e("60"), //
				" values(?,?,?)");
		Dao.dao.execute("update user set username=?,address=? " + e("Sam", "BeiJing") + " where age=" + q(10));
		Dao.dao.execute("update user set username=", q("John"), ",address=", q("Shanghai"), " where age=", q(20));
		Dao.dao.execute("insert user set", //
				" username=?", e("PeterPeter"), //
				",address=? ", e("Nanjing")); //
		Dao.dao.execute("update user set", //
				" username=?", e("Peter"), //
				",address=? ", e("Nanjing"), //
				" where age=?", e(30));
		Dao.dao.execute("update user set", //
				" username=", q("Jeffery"), //
				",address=", q("Tianjing"), //
				" where age=", q(40));
		System.out.println("tx_JdbcDemo Done");
	}

	public void tx_BatchInsertDemo() {
		for (int i = 0; i < 10000; i++)
			Dao.dao.cacheSQL("insert user (username,age) values(?,?)" + e("Batch_User" + i, 70));
		Dao.dao.executeCachedSQLs();
		System.out.println("tx_BatchInsertDemo Done");
	}

	public void tx_main() {
		Dao.dao.execute("delete from user");
		tx_CrudDemo();
		tx_JdbcDemo();
		tx_BatchInsertDemo();
	}

	public static void main(String[] args) {
		Tester tester = BeanBox.getBean(Tester.class);
		tester.tx_main();
	}

}