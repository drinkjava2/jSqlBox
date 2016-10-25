package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.sql.DataSource;

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

}