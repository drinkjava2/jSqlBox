package com.github.drinkjava2.jsqlbox;

import com.github.drinkjava2.jdbpro.SpecialSqlItemPreparer;

/**
 * If SqlBoxContext found this class exist, will call its init method
 */
public class SqlBoxContextInitializer {

	@SuppressWarnings("deprecation")
	public static void initialize(SqlBoxContext ctx) {
		ctx.setIocTool(JBeanBoxIocTool.instance);//NOSONAR
		ctx.setSpecialSqlItemPreparers(new SpecialSqlItemPreparer[] { new LambdSqlItemPreparer() });//NOSONAR
	}

}