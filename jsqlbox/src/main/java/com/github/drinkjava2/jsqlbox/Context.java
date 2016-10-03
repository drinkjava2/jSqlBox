package com.github.drinkjava2.jsqlbox;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

@SuppressWarnings("unchecked")
public class Context {
	public static final String daoIdentity = "Dao";
	public static final String daoMethod = daoIdentity.substring(0, 1).toLowerCase() + daoIdentity.substring(1);
	public static final Context defaultContext = new Context();
	private JdbcTemplate jdbc = new JdbcTemplate();

	public JdbcTemplate getJdbc() {
		return jdbc;
	}

	public void setJdbc(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public void setDataSource(DataSource dataSource) {
		jdbc.setDataSource(dataSource);
	}

	public <T> T create(Class<?> clazz) {
		Dao dao = SQLBoxUtils.findDao(clazz, this);
		return (T) SQLBoxUtils.createProxyBean(clazz, dao);
	}
}