package com.github.drinkjava2.jsqlbox;

/*- JAVA8_BEGIN */
import com.github.drinkjava2.jdbpro.SqlItem;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jsqlbox.AliasProxyUtil.AliasItemInfo;
import com.github.drinkjava2.jsqlbox.LambdSqlItem.ALIAS;
import com.github.drinkjava2.jsqlbox.LambdSqlItem.COL;
 
public class JAVA8 {//NOSONAR 
	// Create a proxy bean to help build refactable SQL  
	public static <T> T proxy(Class<T> claz) {
		return AliasProxyUtil.createAliasProxy(claz);
	}

	// Create a proxy bean to help build refactable SQL, give it an alias name 
	public static <T> T proxy(Class<T> claz, String alias) {
		return AliasProxyUtil.createAliasProxy(claz, alias);
	}

	// Return current proxy's sql table name  
	public static SqlItem table(Object proxy) {
		return AliasProxyUtil.TABLE(proxy);
	}

	// Return current entity method's pure column name, ignore alias setting  
	public static String pure$(COL lambda) {// NOSONAR
		AliasProxyUtil.aliasItemInfo.remove();
		lambda.get();// AOP magic
		AliasItemInfo a = AliasProxyUtil.aliasItemInfo.get();
		if (StrUtils.isEmpty(a.colName))
			throw new DbException("Column name not found.");
		return a.colName;
	}

	// Return current entity method's pure column name, if have alias, return alias.column format 
	public static String $(COL lambda) {// NOSONAR
		AliasProxyUtil.aliasItemInfo.remove();
		lambda.get();// AOP magic
		AliasItemInfo a = AliasProxyUtil.aliasItemInfo.get();
		if (StrUtils.isEmpty(a.colName))
			throw new DbException("Column name not found.");
		if (StrUtils.isEmpty(a.alias))
			return a.colName;
		else
			return new StringBuilder(a.alias).append(".").append(a.colName).toString();
	}

	// Return column as alias format 
	public static String a$(ALIAS lambda) {// NOSONAR
		AliasProxyUtil.aliasItemInfo.remove();
		lambda.get();// AOP magic
		AliasItemInfo a = AliasProxyUtil.aliasItemInfo.get();
		if (StrUtils.isEmpty(a.colName))
			throw new DbException("Column name not found.");// NOSONAR
		if (StrUtils.isEmpty(a.alias))
			return a.colName;
		else
			return new StringBuilder(a.alias).append(".").append(a.colName).append(" as ").append(a.alias).append("_")
					.append(a.colName).toString();
	}

	// Return ", column as alias" format 
	public static String c$(ALIAS lambda) {// NOSONAR
		AliasProxyUtil.aliasItemInfo.remove();
		lambda.get();// AOP magic
		AliasItemInfo a = AliasProxyUtil.aliasItemInfo.get();
		if (StrUtils.isEmpty(a.colName))
			throw new DbException("Column name not found.");
		if (StrUtils.isEmpty(a.alias))
			return ", " + a.colName;
		else
			return new StringBuilder(", ").append(a.alias).append(".").append(a.colName).append(" as ").append(a.alias)
					.append("_").append(a.colName).toString();
	}
	 
}
/* JAVA8_END */