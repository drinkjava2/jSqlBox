package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;

import com.github.drinkjava2.jsqlbox.jpa.Column;

/**
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class SqlBoxContext {

	public static final SqlBoxContext DEFAULT_SQLBOX_CONTEXT = new SqlBoxContext(null);
	public static final String SQLBOX_IDENTITY = "OTHER";

	private DataSource dataSource = null;

	private ConcurrentHashMap<String, Map<String, Column>> databaseColumnsCache = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, String> databaseTableNameCache = new ConcurrentHashMap<>();

	// print SQL to console or log depends logging.properties
	private boolean showSql = false;

	public static final ThreadLocal<HashMap<Object, Object>> classExistCache = new ThreadLocal<HashMap<Object, Object>>() {
		@Override
		protected HashMap<Object, Object> initialValue() {
			return new HashMap<>();
		}
	};

	public SqlBoxContext(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public boolean isShowSql() {
		return showSql;
	}

	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}

	/**
	 * Find and create a SqlBox instance according bean class or SqlBox Class
	 */
	protected SqlBox findAndBuildSqlBox(Class<?> beanOrSqlBoxClass) {
		Class<?> boxClass = null;
		if (beanOrSqlBoxClass == null) {
			SqlBoxException.throwEX(null, "SqlBoxContext findAndBuildSqlBox error! Bean Or SqlBox Class not set");
			return null;
		}
		if (SqlBox.class.isAssignableFrom(beanOrSqlBoxClass))
			boxClass = beanOrSqlBoxClass;
		if (boxClass == null)
			boxClass = SqlBoxUtils.checkSqlBoxClassExist(beanOrSqlBoxClass.getName() + SQLBOX_IDENTITY);
		if (boxClass == null)
			boxClass = SqlBoxUtils.checkSqlBoxClassExist(
					beanOrSqlBoxClass.getName() + "$" + beanOrSqlBoxClass.getSimpleName() + SQLBOX_IDENTITY);
		SqlBox box = null;
		if (boxClass == null) {
			box = new SqlBox(this);
			box.setBeanClass(beanOrSqlBoxClass);
		} else {
			try {
				box = (SqlBox) boxClass.newInstance();
				if (box.getBeanClass() == null)
					box.setBeanClass(beanOrSqlBoxClass);
				box.setContext(this);
			} catch (Exception e) {
				SqlBoxException.throwEX(e, "SqlBoxContext findAndBuildSqlBox error! Can not create SqlBox instance");
			}
		}
		if (box != null)
			box.initialize();
		return box;
	}

	/**
	 * create a PO bean instance
	 */
	public <T> T get(Class<?> beanOrSqlBoxClass) {
		SqlBox box = findAndBuildSqlBox(beanOrSqlBoxClass);
		Object bean = null;
		try {
			bean = box.getBeanClass().newInstance();
			Dao dao = new Dao(box);
			dao.setBean(bean);
			Method m = box.getBeanClass().getMethod("putDao", new Class[] { Dao.class });
			m.invoke(bean, new Object[] { dao });
		} catch (Exception e) {
			SqlBoxUtils.eatException(e);
		}
		return (T) bean;
	}

	public boolean existTable(String tablename) {
		return databaseColumnsCache.get(tablename) != null;
	}

	/**
	 * Cache table MetaData in SqlBoxContext for future use, use lower case
	 * column name as key
	 */
	public String cacheTableStructure(String tableName) {
		String realTableName = null;
		DataSource ds = this.getDataSource();
		ResultSet rs = null;
		Connection con = DataSourceUtils.getConnection(ds);// NOSONAR
		try {
			rs = con.getMetaData().getTables(null, null, null, new String[] { "TABLE", "VIEW" });
			while (rs.next())
				databaseTableNameCache.put(rs.getString("TABLE_NAME").toLowerCase(), rs.getString("TABLE_NAME"));
			realTableName = databaseTableNameCache.get(tableName.toLowerCase());
			if (SqlBoxUtils.isEmptyStr(realTableName))
				SqlBoxException.throwEX(null,
						"SqlBoxContext cacheTableStructure error, table " + tableName + " does not exist");
			rs.close();
			rs = con.getMetaData().getColumns(null, null, realTableName, null);
			if (rs == null)
				SqlBoxException.throwEX(null,
						"SqlBoxContext cacheTableStructure error, table " + tableName + " does not exist");
			Map<String, Column> columns = new HashMap<>();
			while (rs != null && rs.next()) {
				Column col = new Column();
				col.setColumnDefinition(rs.getString("COLUMN_NAME"));
				col.setPropertyTypeName(rs.getString("TYPE_NAME"));
				columns.put(rs.getString("COLUMN_NAME").toLowerCase(), col);
			}
			databaseColumnsCache.put(realTableName, columns);
		} catch (Exception e) {
			SqlBoxException.throwEX(e, "SQLHelper exec error");
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					SqlBoxException.throwEX(e, "SqlBoxContext cacheTableStructure error, failed to close ResultSet");
				} finally {
					DataSourceUtils.releaseConnection(con, ds);
				}
			else
				DataSourceUtils.releaseConnection(con, ds);
		}
		return realTableName;
	}

	public Map<String, Column> getTableStructure(String tableName) {
		return databaseColumnsCache.get(tableName);
	}

}