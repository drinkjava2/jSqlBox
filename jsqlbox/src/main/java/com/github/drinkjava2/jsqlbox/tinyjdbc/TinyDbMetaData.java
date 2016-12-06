package com.github.drinkjava2.jsqlbox.tinyjdbc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.github.drinkjava2.jsqlbox.jpa.Column;

/**
 * MetaData of database
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class TinyDbMetaData {
	String jdbcDriverName;
	Map<String, String> tableNames = new HashMap<>();
	Map<String, Map<String, Column>> tables = new HashMap<>();

	public boolean existTable(String tableName) {
		return tables.containsKey(tableName);
	}

	public Map<String, Column> getOneTable(String tableName) {
		return tables.get(tableName);
	}
	// getter & setters==============

	public String getJdbcDriverName() {
		return jdbcDriverName;
	}

	public void setJdbcDriverName(String jdbcDriverName) {
		this.jdbcDriverName = jdbcDriverName;
	}

	public Map<String, Map<String, Column>> getTables() {
		return tables;
	}

	public void setTables(Map<String, Map<String, Column>> tables) {
		this.tables = tables;
	}

	public Map<String, String> getTableNames() {
		return tableNames;
	}

	public void setTableNames(Map<String, String> tableNames) {
		this.tableNames = tableNames;
	}

	public String getDebugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("\r\nTables:\r\n");
		sb.append(this.getTableNames().toString()).append("\r\n");

		for (Entry<String, Map<String, Column>> entry : tables.entrySet()) {
			String tableName = entry.getKey();
			sb.append("\r\n" + tableName).append("\r\n");
			Map<String, Column> onetable = entry.getValue();
			for (Entry<String, Column> t : onetable.entrySet()) {
				Column col = t.getValue();
				sb.append(col.getColumnName()).append(",");
				sb.append(col.getPropertyTypeName()).append(",");
				sb.append(col.getLength()).append("\r\n");
			}
		}
		return sb.toString();
	}
}