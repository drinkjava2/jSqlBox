
package com.github.drinkjava2.jsqlbox;

public class SqlAndParameters {
	private String sql;
	private Object[] parameters;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}
}
