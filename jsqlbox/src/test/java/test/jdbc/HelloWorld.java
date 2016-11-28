package test.jdbc;

import com.github.drinkjava2.jsqlbox.Dao;

import test.config.InitializeDatabase;
import test.config.po.User;

public class HelloWorld {

	public static void main(String[] args) {
		InitializeDatabase.recreateTables();
		Dao.dao().execute("insert into users (username) values('user1')");
		User u = new User();
		u.dao();
		u.dao().execute("insert into users (username) values('user2')");
	}
}