package com.github.drinkjava2.jsqlbox;

import javax.sql.DataSource;

@SuppressWarnings("unchecked")
public class SQLBoxContext {
	String boxIdentity = "Box";
	DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public SQLBoxContext setBoxIdentity(String boxIdentity) {
		this.boxIdentity = boxIdentity;
		return this;
	}

	public String getBoxIdentity() {
		return boxIdentity;
	}

	public <T> T create(Class<?> clazz) {
		Class<SQLBox> sqlbox = SQLBoxUtils.findSQLBoxClass(clazz);
		if (sqlbox != null)
			try {
				SQLBox box = sqlbox.newInstance().setContext(this);;
				return box.create(); 
			} catch (Exception e) {
				SQLBoxUtils.throwEX(e, "SQLBox create error, clazz=" + clazz);
			}
		return (T) SQLBoxUtils.createProxyPO(clazz, null, this);
	}

}