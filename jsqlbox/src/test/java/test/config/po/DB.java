package test.config.po;

import java.util.HashMap;
import java.util.Map;

public class DB {
	public User user(String... alias) {
		return new User();
	};

	public Map<String, Object> map() {
		return new HashMap<>();
	};
}