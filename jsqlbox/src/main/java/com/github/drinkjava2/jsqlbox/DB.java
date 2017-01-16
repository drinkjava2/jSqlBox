package com.github.drinkjava2.jsqlbox;

import java.util.Map;

public class DB {
	
	private Map<String, Object> map;

	public Map<String, Object> getMap() {
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}

	public Object get(String key) {
		return map.get(key);
	}

	public <T> T assemble(Class<T> clazz) {
		return (T) null;
	}

}