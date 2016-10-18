package com.github.drinkjava2.jsqlbox;

import java.util.HashMap;

import javax.sql.DataSource;

public class SQLBoxContext {
	public static final String SQLBOX_IDENTITY = "Dao";
	public static final SQLBoxContext defaultContext = new SQLBoxContext(null);
	private DataSource dataSource = null;
	public static final ThreadLocal<HashMap<Object, Object>> poCache = new ThreadLocal<HashMap<Object, Object>>() {
		@Override
		protected HashMap<Object, Object> initialValue() {
			return new HashMap<>();
		}
	};

	public SQLBoxContext(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}