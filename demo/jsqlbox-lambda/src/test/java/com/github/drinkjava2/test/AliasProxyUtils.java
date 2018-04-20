package com.github.drinkjava2.test;

import java.lang.reflect.Method;

import com.github.drinkjava2.cglib3_2_0.proxy.Enhancer;
import com.github.drinkjava2.cglib3_2_0.proxy.MethodInterceptor;
import com.github.drinkjava2.cglib3_2_0.proxy.MethodProxy;
import com.github.drinkjava2.jdbpro.CustomSqlItem;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBoxException;

public class AliasProxyUtils {
	public static ThreadLocal<String[]> thdMethodName = new ThreadLocal<String[]>();

	public static class ProxyBean implements MethodInterceptor {
		private TableModel tableModel;

		public ProxyBean(TableModel tableModel) {
			this.tableModel = tableModel;
		}

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy cgLibMethodProxy)
				throws Throwable {
			thdMethodName.remove();
			if (method != null && tableModel != null) {
				String fieldName = method.getName().substring(3);
				String columnName = null;
				for (ColumnModel col : tableModel.getColumns())
					if (col.getEntityField().equalsIgnoreCase(fieldName))
						columnName = col.getColumnName();
				thdMethodName.set(new String[] { tableModel.getTableName(), tableModel.getAlias(), columnName });
			}
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T createAliasProxy(Class<T> c, String alias) {
		TableModel t = TableModelUtils.entity2Model(c);
		t.setAlias(alias);
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(c);
		enhancer.setCallback(new ProxyBean(t));
		return (T) enhancer.create();
	}

	public static void checkArrayStringExist(String[] a) {
		if (a == null)
			throw new SqlBoxException("No configuration found.");
		if (StrUtils.isEmpty(a[0]))
			throw new SqlBoxException("TableName name not found.");
		if (StrUtils.isEmpty(a[1]))
			throw new SqlBoxException("Table alias not set.");
	}

	/**
	 * SqlPieceCustomSqlItem Simply add the String into SQL
	 */
	public static class SqlPieceCustomSqlItem implements CustomSqlItem {
		String sqlPiece;

		public SqlPieceCustomSqlItem(String sqlPiece) {
			this.sqlPiece = sqlPiece;
		}

		@Override
		public void dealItem(PreparedSQL ps, StringBuilder sb) {
			sb.append(sqlPiece);
		}
	}

	public static CustomSqlItem acol(Object o) {
		try {
			String[] a = thdMethodName.get();
			checkArrayStringExist(a);
			if (StrUtils.isEmpty(a[2]))
				throw new SqlBoxException("Column name not found.");
			String sqlPiece = new StringBuilder(a[1]).append(".").append(a[2]).append(" as ").append(a[1]).append("_")
					.append(a[2]).toString();
			return new SqlPieceCustomSqlItem(sqlPiece);
		} finally {
			thdMethodName.remove();
		}
	}

	public static CustomSqlItem col(Object o) {
		try {
			String[] a = thdMethodName.get();
			checkArrayStringExist(a);
			if (StrUtils.isEmpty(a[2]))
				throw new SqlBoxException("Column name not found.");
			String sqlPiece = new StringBuilder(a[1]).append(".").append(a[2]).toString();
			return new SqlPieceCustomSqlItem(sqlPiece);
		} finally {
			thdMethodName.remove();
		}
	}

	public static CustomSqlItem table(Object o) {
		try {
			o.toString();
			String[] a = thdMethodName.get();
			checkArrayStringExist(a);
			String sqlPiece = new StringBuilder(a[0]).append(" ").append(a[1]).toString();
			return new SqlPieceCustomSqlItem(sqlPiece);
		} finally {
			thdMethodName.remove();
		}
	}

}