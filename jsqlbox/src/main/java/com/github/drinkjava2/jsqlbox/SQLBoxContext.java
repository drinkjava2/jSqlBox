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
 * @author Yong Zhu (Yong9981@gmail.com)
 * @version 1.0.0
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class SQLBoxContext {

	public static final SQLBoxContext DEFAULT_SQLBOX_CONTEXT = new SQLBoxContext(null);
	public static final String SQLBOX_IDENTITY = "BX";

	private DataSource dataSource = null;

	private ConcurrentHashMap<String, Map<String, Column>> databaseStructure = new ConcurrentHashMap<>();

	public static final ThreadLocal<HashMap<Object, Object>> classExistCache = new ThreadLocal<HashMap<Object, Object>>() {
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

	/**
	 * Find and create a SQLBox instance according bean class or SQLBox Class
	 */
	protected SQLBox findAndBuildSQLBox(Class<?> beanOrSQLBoxClass) {
		Class<?> boxClass = null;
		if (beanOrSQLBoxClass == null) {
			SQLBoxUtils.throwEX(null, "SQLBoxContext findAndBuildSQLBox error! Bean Or SQLBox Class not set");
			return null;
		}
		if (SQLBox.class.isAssignableFrom(beanOrSQLBoxClass))
			boxClass = beanOrSQLBoxClass;
		if (boxClass == null)
			boxClass = SQLBoxUtils.checkSQLBoxClassExist(beanOrSQLBoxClass.getName() + SQLBOX_IDENTITY);
		if (boxClass == null)
			boxClass = SQLBoxUtils.checkSQLBoxClassExist(
					beanOrSQLBoxClass.getName() + "$" + beanOrSQLBoxClass.getSimpleName() + SQLBOX_IDENTITY);
		SQLBox box = null;
		if (boxClass == null) {
			box = new SQLBox(this);
			box.setBeanClass(beanOrSQLBoxClass);
		} else {
			try {
				box = (SQLBox) boxClass.newInstance();
				if (box.getBeanClass() == null)
					box.setBeanClass(beanOrSQLBoxClass);
				box.setContext(this);
			} catch (Exception e) {
				SQLBoxUtils.throwEX(e, "SQLBoxContext findAndBuildSQLBox error! Can not create SQLBox instance");
			}
		}
		if (box != null)
			box.initialize();
		return box;
	}

	/**
	 * create a PO bean instance
	 */
	public <T> T get(Class<?> beanOrSQLBoxClass) {
		SQLBox box = findAndBuildSQLBox(beanOrSQLBoxClass);
		Object bean = null;
		try {
			bean = box.getBeanClass().newInstance();
			Dao dao = new Dao(box);
			dao.setBean(bean);
			Method m = box.getBeanClass().getMethod("putDao", new Class[] { Dao.class });
			m.invoke(bean, new Object[] { dao });
		} catch (Exception e) {
			SQLBoxUtils.logException(e);
		}
		return (T) bean;
	}

	public boolean existTable(String tablename) {
		return databaseStructure.get(tablename) != null;
	}

	/**
	 * Load table MetaData structure for future use
	 */
	public void loadTableStructure(String tablename) {
		Dao dao = new Dao(new SQLBox(this));
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
			columns.put(metaData.getColumnName(i), col);
		}
		databaseStructure.put(tablename, columns);
	}

	public Map<String, Column> getTableStructure(String tablename) {
		return databaseStructure.get(tablename);
	}

}