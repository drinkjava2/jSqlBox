package com.github.drinkjava2.jsqlbox;

import javax.sql.DataSource;

@SuppressWarnings("unchecked")
public class Context {
	public static final String daoMethod = "dao";
	private String daoIdentity = "Dao";

	public String getBoxIdentity() {
		return daoIdentity;
	}

	public void setBoxIdentity(String daoIdentity) {
		this.daoIdentity = daoIdentity;
	}

	DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public <T> T create(Class<?> clazz) {
		Class<BaseDao> daoClass = SQLBoxUtils.findSQLBoxClass(clazz, this);
		if (daoClass != null)
			try {
				BaseDao box = daoClass.newInstance().setContext(this);
				return box.create();
			} catch (Exception e) {
				SQLBoxUtils.throwEX(e, "SQLBox create error, clazz=" + clazz);
			}
		BaseDao dao = new BaseDao().setBeanClass(clazz).setContext(this);
		return (T) SQLBoxUtils.createProxyPO(clazz, dao);
	}

}