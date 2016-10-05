package com.github.drinkjava2.jsqlbox;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

@SuppressWarnings("unchecked")
public class SQLBoxContext {
	public static final String daoIdentity = "Dao";
	public static final SQLBoxContext defaultContext = new SQLBoxContext(null);
	private JdbcTemplate jdbc = new JdbcTemplate();

	public ThreadLocal<HashMap<Object, Object>> poCache = new ThreadLocal<HashMap<Object, Object>>() {
		protected HashMap<Object, Object> initialValue() {
			return new HashMap<Object, Object>();
		}
	};

	public SQLBoxContext(DataSource dataSource) {
		jdbc.setDataSource(dataSource);
	}

	public JdbcTemplate getJdbc() {
		return jdbc;
	}

	public void setJdbc(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public void setDataSource(DataSource dataSource) {
		jdbc.setDataSource(dataSource);
	}

	public DataSource getDataSource() {
		return jdbc.getDataSource();
	}

	public static <T> T createDefaultProxy(Class<?> clazz) {
		return defaultContext.createProxy(clazz);
	}

	public <T> T createProxy(Class<?> clazz) {
		Dao dao = SQLBoxUtils.findDao(clazz, this);
		return (T) SQLBoxUtils.createProxyBean(clazz, dao);
	}
}