package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import com.github.drinkjava2.jsqlbox.jpa.Column;

/**
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class SqlBoxContext {

	public static final SqlBoxContext DEFAULT_SQLBOX_CONTEXT = new SqlBoxContext(null);
	public static final String SQLBOX_IDENTITY = "Other";

	private DataSource dataSource = null;

	private ConcurrentHashMap<String, Map<String, Column>> databaseStructure = new ConcurrentHashMap<>();
	private boolean showSql = false;// print SQL to console or log depends logging.properties

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
		return databaseStructure.get(tablename) != null;
	}

	/**
	 * Cache table MetaData for future use, use lower case column name as key
	 */
	public void loadTableStructure(String tablename) {
		Dao dao = new Dao(new SqlBox(this));
		SqlRowSet rowSet = dao.getJdbc().queryForRowSet("select * from " + tablename + " limit 0");
		SqlRowSetMetaData metaData = rowSet.getMetaData();
		if (metaData == null) {
			databaseStructure.remove(tablename);
			return;
		}
		Map<String, Column> columns = new HashMap<>();
		int columnCount = metaData.getColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			Column col = new Column();
			col.setColumnDefinition(metaData.getColumnName(i));
			col.setScale(metaData.getScale(i));
			col.setPrecision(metaData.getPrecision(i));
			col.setPropertyTypeName(metaData.getColumnTypeName(i));
			columns.put(metaData.getColumnName(i).toLowerCase(), col);
		}
		databaseStructure.put(tablename, columns);
	}

	public Map<String, Column> getTableStructure(String tableName) {
		return databaseStructure.get(tableName);
	}

}