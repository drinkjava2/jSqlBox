package test.test1_basic_crud;

import static com.github.drinkjava2.jsqlbox.SQLHelper.e;
import static com.github.drinkjava2.jsqlbox.SQLHelper.q;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SQLBox;

import test.test1_basic_crud.po.User;

public class Tester {
	public void tx_CrudDemo() {
		// User user = new User();
		// user.setUserName("User1");
		// user.setAddress("Address1");
		// user.setPhoneNumber("11111");
		// user.setAge(1);
		// user.dao().save();

		//TODO: user2 
		 User user2 = SQLBox.get(User.class);
		 user2.setUserName("User2");
		 user2.setPhoneNumber("222");
		 user2.setAddress("Address2");
		 user2.setAge(2);
		 user2.dao().save();

		System.out.println("tx_CrudDemo Done");
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
			Dao.dao.cacheSQL("insert user (username", e("user" + i), ",age", e("70"), ") values(?,?)");
		Dao.dao.executeCachedSQLs();
		System.out.println("tx_BatchInsertDemo Done");
	}

	public void tx_init() {
		Dao.dao.execute("drop table user");
		Dao.dao.execute("drop table user2");
		Dao.dao.execute("create table user", //
				"( ID integer auto_increment ,", //
				"constraint const1 primary key (ID),", //
				"UserName Varchar  (50) ,", //
				"PhoneNumber Varchar  (50) ,", //
				"Address Varchar  (50) ,", //
				"Age Integer )ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		Dao.dao.execute("create table user2", //
				"( id integer auto_increment ,", //
				"constraint const1 primary key (ID),", //
				"user_name Varchar  (50) ,", //
				"phone_number Varchar  (50) ,", //
				"address Varchar  (50) ,", //
				"age Integer )ENGINE=InnoDB DEFAULT CHARSET=utf8;");
	}

	public void tx_main() {
		tx_init();
		tx_CrudDemo();
		// tx_JdbcDemo();
		// tx_BatchInsertDemo();
	}

	public static void main(String[] args) {
		Tester tester = BeanBox.getBean(Tester.class);
		tester.tx_main();
	}

}