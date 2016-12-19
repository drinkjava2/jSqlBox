package test;

import com.github.drinkjava2.jsqlbox.SqlBox;

import test.config.TestPrepare;
import test.config.po.User;

public class HelloWorld {

	public static void main(String[] args) {
		TestPrepare.dropAndRecreateTables();
		SqlBox.execute("insert into users (username) values('user1')");
		User u = new User();
		u.setUserName("user1");
		u.insert();
	}
}