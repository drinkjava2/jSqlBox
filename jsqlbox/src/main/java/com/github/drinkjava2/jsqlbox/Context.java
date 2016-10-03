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

	DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		jdbc.setDataSource(dataSource);
	}

	public <T> T create(Class<?> clazz) {
		Class<Dao> daoClass = SQLBoxUtils.findSQLBoxClass(clazz, this);
		if (daoClass != null)
			try {
				Dao box = daoClass.newInstance().setContext(this);
				return box.create();
			} catch (Exception e) {
				SQLBoxUtils.throwEX(e, "SQLBox create error, clazz=" + clazz);
			}
		Dao dao = new Dao().setBeanClass(clazz).setContext(this);
		return (T) SQLBoxUtils.createProxyBean(clazz, dao);
	}

}