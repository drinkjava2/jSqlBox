package test.test1_basic_crud;

import static com.github.drinkjava2.jsqlbox.SQLHelper.e;
import static com.github.drinkjava2.jsqlbox.SQLHelper.q;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.test1_basic_crud.po.User;

public class Tester {

	public void tx_insertDemo() {
		Dao.dao.execute("delete from user");
		Dao.dao.execute("insert user (username,age) values(" + q("user1") + "," + q(10) + ")");
		Dao.dao.execute("insert user (username,age) values(" + q("user2", 20) + ")");
		Dao.dao.execute("insert user (username,age) values(?,?)" + e("user3") + e(30));
		Dao.dao.execute("insert user (username,age) values(?,?)" + e("user4", 40));
		Dao.dao.execute(
				"insert " + User.Table + " (" + User.UserName + "," + User.Age + ") values(" + q("user5", 50) + ")");
		Dao.dao.execute("insert user ", //
				" (username", e("Andy"), //
				", address", e("Guanzhou"), //
				", age)", e("60"), //
				" values(?,?,?)");
		Dao.dao.execute("update user set username=?,address=? " + e("Sam", "BeiJing") + " where age=" + q(10));
		Dao.dao.execute("update user set username=?,address=? ", e("John", "Shanghai"), " where age=", q(20));
		Dao.dao.execute("update user set", //
				" username=?", e("Peter"), //
				",address=? ", e("Nanjing"), //
				" where age=?", e(30));
		Dao.dao.execute("update user set", //
				" username=", q("Jeffery"), //
				",address=", q("Tianjing"), //
				" where age=", q(40));

		User user = new User();
		user.setUsername("user3");
		user.setAge(70);
		user.dao().save();// TODO not finished
		System.out.println("tx_insertDemo Done");
	}

	public void tx_batchInsertDemo() {
		for (int i = 7; i < 100000; i++)
			Dao.dao.cacheSQL("insert user (username,age) values(?,?)" + e("user" + i, 70));
		Dao.dao.executeCachedSQLs();
		System.out.println("tx_batchInsertDemo Done");
	}

	public void tx_main() {
		tx_insertDemo();
		tx_batchInsertDemo();
		// tx_insertDefaultProxyUser();
		// tx_insertCtxProxyUser();
	}

	public static void main(String[] args) {
		Tester tester = BeanBox.getBean(Tester.class);
		tester.tx_main();
	}

}