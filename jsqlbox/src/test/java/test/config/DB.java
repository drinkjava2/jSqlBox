package test.config;

import java.util.HashMap;
import java.util.Map;

import test.po.User;

public class DB {
	public User user(String... alias) {
		return new User();
	};

	public Map<String, Object> map() {
		return new HashMap<>();
	};
}