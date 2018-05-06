package com.github.drinkjava2.test.refactor_sql;

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SpecialSqlItem;
import com.github.drinkjava2.jdbpro.SpecialSqlItemPreparer;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.SqlBoxException;
import com.github.drinkjava2.test.refactor_sql.AliasProxyUtils.AliasItemInfo;

public class LambdSqlItemPreparer implements SpecialSqlItemPreparer {
	public static interface ALIAS extends SpecialSqlItem {// a.col as a_col
		public Object get();
	}

	public static interface C_ALIAS extends SpecialSqlItem {// , a.clo as a_col
		public Object get();
	}

	public static interface COL extends SpecialSqlItem {// a.col
		public Object get();
	}

	@Override
	public boolean doPrepare(PreparedSQL ps, StringBuilder sql, SpecialSqlItem item) {
		AliasProxyUtils.thdMethodName.remove();
		if (item instanceof ALIAS) {
			((ALIAS) item).get();
			AliasItemInfo a = AliasProxyUtils.thdMethodName.get();
			if (StrUtils.isEmpty(a.colName))
				throw new SqlBoxException("Column name not found.");
			if (StrUtils.isEmpty(a.alias))
				sql.append(a.colName);
			else
				sql.append(new StringBuilder(a.alias).append(".").append(a.colName).append(" as ").append(a.alias)
						.append("_").append(a.colName).toString());
		} else if (item instanceof C_ALIAS) {
			((C_ALIAS) item).get();
			AliasItemInfo a = AliasProxyUtils.thdMethodName.get();
			if (StrUtils.isEmpty(a.colName))
				throw new SqlBoxException("Column name not found.");
			if (StrUtils.isEmpty(a.alias))
				sql.append(", " + a.colName);
			else
				sql.append(new StringBuilder(", ").append(a.alias).append(".").append(a.colName).append(" as ")
						.append(a.alias).append("_").append(a.colName).toString());
		} else if (item instanceof COL) {
			((COL) item).get();
			AliasItemInfo a = AliasProxyUtils.thdMethodName.get();
			if (StrUtils.isEmpty(a.colName))
				throw new SqlBoxException("Column name not found.");
			if (StrUtils.isEmpty(a.alias))
				sql.append(a.colName);
			else
				sql.append(new StringBuilder(a.alias).append(".").append(a.colName).toString());
		} else
			return false;// Not my job, return false to tell jSqlBox use other SpecialSqlItemPreparer
		return true;
	}
}