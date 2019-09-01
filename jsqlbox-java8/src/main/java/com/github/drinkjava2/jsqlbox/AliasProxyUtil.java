package com.github.drinkjava2.jsqlbox;

import java.lang.reflect.Method;

import com.github.drinkjava2.cglib3_2_0.proxy.Enhancer;
import com.github.drinkjava2.cglib3_2_0.proxy.MethodInterceptor;
import com.github.drinkjava2.cglib3_2_0.proxy.MethodProxy;
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

/**
 * AliasProxyUtils used to create alias proxy
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class AliasProxyUtil {
	public static ThreadLocal<AliasItemInfo> aliasItemInfo = new ThreadLocal<AliasItemInfo>();// NOSONAR

	static class AliasItemInfo {
		public String tableName = null;// NOSONAR
		public String alias = null;// NOSONAR
		public String colName = null;// NOSONAR

		public AliasItemInfo(String tableName, String alias, String colName) {
			this.tableName = tableName;
			this.alias = alias;
			this.colName = colName;
		}
	}

	/**
	 * This proxy bean return null for all methods call, but based on current method
	 * name write TableName, Alias,ColumnName these info into a ThreadLocal variant
	 * aliasItemInfo.
	 * 
	 * The MethodInterceptor depends on CgLib, not AOP alliance, CgLib is included
	 * in jBeanBox(changed package name), jBeanBox is included in jBeanBox's Java8
	 * version
	 */
	static class ProxyBean implements MethodInterceptor {
		private TableModel tableModel;
		private String alias;

		public ProxyBean(TableModel tableModel, String alias) {
			this.tableModel = tableModel;
			this.alias = alias;
		}

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy cgLibMethodProxy)
				throws Throwable {
			aliasItemInfo.remove();
			if (method != null && tableModel != null) {
				String fieldName;
				String methodNm = method.getName();
				if (methodNm.startsWith("get"))
					fieldName = methodNm.substring(3);
				else
					fieldName = methodNm.substring(2);
				String columnName = null;
				for (ColumnModel col : tableModel.getColumns())
					if (col.getEntityField().equalsIgnoreCase(fieldName))
						columnName = col.getColumnName();
				aliasItemInfo.set(new AliasItemInfo(tableModel.getTableName(), alias, columnName));
			}
			return null;
		}
	}

	/**
	 * Create a alias proxy for iQuery query
	 * 
	 * @param clazz
	 *            target class
	 * @return proxy bean which make all methods only return null
	 */
	public static <T> T createAliasProxy(Class<T> c) {
		return createAliasProxy(c, null);
	}

	/**
	 * Create a alias proxy for iQuery query
	 * 
	 * @param clazz
	 *            The target entity class
	 * @param alias
	 *            The alias in SQL
	 * @return Proxy entity bean which make all methods only return null
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createAliasProxy(Class<T> c, String alias) {
		TableModel t = TableModelUtils.entity2ReadOnlyModel(c);
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(c);
		enhancer.setCallback(new ProxyBean(t, alias));
		return (T) enhancer.create();
	}

	public static SqlItem clean() {
		aliasItemInfo.remove();
		return new SqlItem("");
	}

	public static SqlItem alias(Object o) {// NOSONAR
		try {
			AliasItemInfo a = aliasItemInfo.get();
			if (StrUtils.isEmpty(a.colName))
				throw new SqlBoxException("Column name not found.");
			if (StrUtils.isEmpty(a.alias))
				return new SqlItem(a.colName);
			else
				return new SqlItem(new StringBuilder(a.alias).append(".").append(a.colName).append(" as ")
						.append(a.alias).append("_").append(a.colName).toString());
		} finally {
			aliasItemInfo.remove();
		}
	}

	public static SqlItem c_alias(Object o) {// NOSONAR
		try {
			AliasItemInfo a = aliasItemInfo.get();
			if (StrUtils.isEmpty(a.colName))
				throw new SqlBoxException("Column name not found.");
			if (StrUtils.isEmpty(a.alias))
				return new SqlItem(", " + a.colName);
			else
				return new SqlItem(new StringBuilder(", ").append(a.alias).append(".").append(a.colName).append(" as ")
						.append(a.alias).append("_").append(a.colName).toString());
		} finally {
			aliasItemInfo.remove();
		}
	}

	public static SqlItem col(Object o) {// NOSONAR
		try {
			AliasItemInfo a = aliasItemInfo.get();
			if (StrUtils.isEmpty(a.colName))
				throw new SqlBoxException("Column name not found.");
			if (StrUtils.isEmpty(a.alias))
				return new SqlItem(a.colName);
			else
				return new SqlItem(new StringBuilder(a.alias).append(".").append(a.colName).toString());
		} finally {
			aliasItemInfo.remove();
		}
	}

	public static SqlItem table(Object o) {
		try {
			o.toString();
			AliasItemInfo a = aliasItemInfo.get();
			if (StrUtils.isEmpty(a.alias))
				return new SqlItem(a.tableName);
			else
				return new SqlItem(new StringBuilder(a.tableName).append(" ").append(a.alias).toString());
		} finally {
			aliasItemInfo.remove();
		}
	}

	public static SqlItem TABLE(Object o) {// NOSONAR
		return table(o);
	}

}