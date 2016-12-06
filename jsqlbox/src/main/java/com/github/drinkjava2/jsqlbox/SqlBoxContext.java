package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.drinkjava2.jsqlbox.jpa.IdGenerator;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyDbMetaData;
import com.github.drinkjava2.jsqlbox.tinyjdbc.TinyJdbc;

/**
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class SqlBoxContext {
	private static final SqlBoxLogger log = SqlBoxLogger.getLog(SqlBoxContext.class);

	// print SQL to console or log depends logging.properties
	private boolean showSql = false;

	private static String sqlBoxConfigClass = "SqlBoxConfig";
	private static String getSqlBoxContextMethod = "getSqlBoxContext";

	public static final String SQLBOX_IDENTITY = "BOX";

	private JdbcTemplate jdbc = new JdbcTemplate();
	private DataSource dataSource = null;

	private TinyDbMetaData metaData;

	// ID Generator singleton cache
	private HashMap<String, IdGenerator> generatorCache = new HashMap<>();

	public static final ThreadLocal<HashMap<Object, Object>> classExistCache = new ThreadLocal<HashMap<Object, Object>>() {
		@Override
		protected HashMap<Object, Object> initialValue() {
			return new HashMap<>();
		}
	};

	public SqlBoxContext(DataSource dataSource) {
		this.dataSource = dataSource;
		if (dataSource != null) {
			this.jdbc.setDataSource(dataSource);
			refreshMetaData();
		}

	}

	public boolean existGeneratorInCache(String name) {
		synchronized (generatorCache) {
			return generatorCache.get(name) != null;
		}
	}

	public void putGeneratorToCache(String name, IdGenerator generator) {
		synchronized (generatorCache) {
			generatorCache.put(name, generator);
		}
	}

	public IdGenerator getGeneratorFromCache(String name) {
		synchronized (generatorCache) {
			return generatorCache.get(name);
		}
	}

	/**
	 * Config a global invoke method, used to get a default SqlBoxContext for global use<br/>
	 * The default method is: public static SqlBoxContext getSqlBoxContext() in SqlBoxConfig class
	 */
	public static void configDefaultContext(String configClassName, String invokeMethodName) {
		sqlBoxConfigClass = configClassName;
		getSqlBoxContextMethod = invokeMethodName;
	}

	public static void resetDefaultContext() {
		sqlBoxConfigClass = "SqlBoxConfig";
		getSqlBoxContextMethod = "getSqlBoxContext";
	}

	/**
	 * Return a default global SqlBoxContext <br/>
	 * Note: a config class SqlBoxConfig.java is needed in class root folder
	 */
	public static SqlBoxContext getDefaultSqlBoxContext() {
		final String errorinfo = "jSqlBox initialization error: class or method not found:";
		SqlBoxContext ctx = null;
		try {
			Class<?> configClass = Class.forName(sqlBoxConfigClass);
			Method method = configClass.getMethod(getSqlBoxContextMethod, new Class[] {});
			ctx = (SqlBoxContext) method.invoke(configClass, new Object[] {});
			if (ctx == null)
				SqlBoxException.throwEX(null, errorinfo + sqlBoxConfigClass + "." + getSqlBoxContextMethod + "()");
		} catch (Exception e1) {
			SqlBoxException.throwEX(e1, errorinfo + sqlBoxConfigClass + "." + getSqlBoxContextMethod + "()");
		} catch (Error error) {// NOSONAR
			log.error(errorinfo + sqlBoxConfigClass + "." + getSqlBoxContextMethod + "()");
			throw error;
		}
		return ctx;
	}

	/**
	 * Create an entity instance
	 */
	public <T> T createBean(Class<?> beanOrSqlBoxClass) {
		SqlBox box = findAndBuildSqlBox(beanOrSqlBoxClass);
		return (T) box.createBean();
	}

	/**
	 * Find and create a SqlBox instance according bean class or SqlBox Class
	 */
	public SqlBox findAndBuildSqlBox(Class<?> beanOrSqlBoxClass) {
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
			box.setEntityClass(beanOrSqlBoxClass);
		} else {
			try {
				box = (SqlBox) boxClass.newInstance();
				if (box.getEntityClass() == null)
					box.setEntityClass(beanOrSqlBoxClass);
				box.setContext(this);
			} catch (Exception e) {
				SqlBoxException.throwEX(e, "SqlBoxContext findAndBuildSqlBox error! Can not create SqlBox instance");
			}
		}
		return box;
	}

	/**
	 * Find real table name from database meta data
	 */
	public String findRealTableName(String tableName) {
		String realTableName;
		TinyDbMetaData meta = this.getMetaData();
		realTableName = meta.getTableNames().get(tableName.toLowerCase());
		if (!SqlBoxUtils.isEmptyStr(realTableName))
			return realTableName;
		realTableName = meta.getTableNames().get(tableName.toLowerCase() + 's');
		if (!SqlBoxUtils.isEmptyStr(realTableName))
			return realTableName;
		return null;
	}

	private String getJdbcDriverName() {
		return this.getMetaData().getJdbcDriverName();
	}

	public Object getDatabaseType() {// NOSONAR
		return DatabaseType.getType(getJdbcDriverName());
	}

	public void refreshMetaData() {
		this.metaData = TinyJdbc.getMetaData(dataSource);
	}

	// ================== getter & setters below============
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbc.setDataSource(dataSource);
		refreshMetaData();
	}

	public boolean isShowSql() {
		return showSql;
	}

	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}

	public JdbcTemplate getJdbc() {
		return jdbc;
	}

	public void setJdbc(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public TinyDbMetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(TinyDbMetaData metaData) {
		this.metaData = metaData;
	}

}