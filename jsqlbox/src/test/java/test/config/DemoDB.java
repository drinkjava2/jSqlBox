package test.config;

import com.github.drinkjava2.jsqlbox.DB;

import test.config.po.User;

public class DemoDB extends DB {
	public User user() {
		return assemble(User.class);
	};

}