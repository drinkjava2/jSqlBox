package com.github.drinkjava2.jsqlbox;

import com.github.drinkjava2.jdbpro.CustomizedSqlItem;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.AliasProxyUtil.AliasItemInfo;

public interface LambdSqlItem extends CustomizedSqlItem {
	public static interface ALIAS extends LambdSqlItem {// a.col as a_col
		public Object get();
	}

	public static interface C_ALIAS extends LambdSqlItem {//// NOSONAR , a.clo as a_col
		public Object get();
	}

	public static interface COL extends LambdSqlItem {// a.col
		public Object get();
	}

	@Override
	public default void doPrepare(PreparedSQL ps) {
		AliasProxyUtil.thdMethodName.remove();
		if (this instanceof ALIAS) {
			((ALIAS) this).get();
			AliasItemInfo a = AliasProxyUtil.thdMethodName.get();
			if (StrUtils.isEmpty(a.colName))
				throw new SqlBoxException("Column name not found.");// NOSONAR
			if (StrUtils.isEmpty(a.alias))
				ps.addSql(a.colName);
			else
				ps.addSql(a.alias).append(".").append(a.colName).append(" as ").append(a.alias).append("_")
						.append(a.colName);
		} else if (this instanceof C_ALIAS) {
			((C_ALIAS) this).get();
			AliasItemInfo a = AliasProxyUtil.thdMethodName.get();
			if (StrUtils.isEmpty(a.colName))
				throw new SqlBoxException("Column name not found.");
			if (StrUtils.isEmpty(a.alias))
				ps.addSql(", " + a.colName);
			else
				ps.addSql(", ").append(a.alias).append(".").append(a.colName).append(" as ").append(a.alias).append("_")
						.append(a.colName);
		} else if (this instanceof COL) {
			((COL) this).get();
			AliasItemInfo a = AliasProxyUtil.thdMethodName.get();
			if (StrUtils.isEmpty(a.colName))
				throw new SqlBoxException("Column name not found.");
			if (StrUtils.isEmpty(a.alias))
				ps.addSql(a.colName);
			else
				ps.addSql(a.alias).append(".").append(a.colName);
		} else
			throw new SqlBoxException("Unsupported CustomizedSqlItem found.");
	}
}