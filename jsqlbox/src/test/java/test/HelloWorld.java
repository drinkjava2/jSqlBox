package test;

import com.github.drinkjava2.jsqlbox.SqlBox;

import test.config.TestPrepare;
import test.config.po.User2;

public class HelloWorld {

	public static void main(String[] args) {
		TestPrepare.dropAndRecreateTables();
		SqlBox.execute("insert into users (username) values('user1')");
		User2 person = new User2();
		person.setUserName("user1");
		person.insert();
		System.out.println(person.getId());// print "1"
	}
}