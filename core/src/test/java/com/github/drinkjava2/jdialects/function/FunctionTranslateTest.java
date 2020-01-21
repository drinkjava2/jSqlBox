/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects.function;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.Dialect;

/**
 * 
 * @author Yong Z.
 * @since 1.0.6
 *
 */
public class FunctionTranslateTest {

	{
		// Dialect.setAllowShowDialectLog(true);
	}

	public static void main(String[] args) {
		Systemout.println(Dialect.Oracle12cDialect.paginAndTrans(3, 10,
				"select concat('a','b','c') as a, current_time() from user_tb"));
	}

	@Test
	public void doDialectTest() {

		Systemout.println(
				Dialect.MySQL55Dialect.pagin(3, 10, "select concat('a','b','c'), current_time() from user_tb"));
		Systemout.println(
				Dialect.Oracle12cDialect.pagin(3, 10, "select concat('a','b','c'), current_time() from user_tb"));
		Systemout.println(
				Dialect.SQLServer2005Dialect.pagin(1, 10, "select concat('a','b','c'), current_time() from user_tb"));

		Systemout.println("============================================");

		Systemout.println(Dialect.MySQL55Dialect.trans("select concat('a','b','c'), current_time()   from user_tb"));
		Systemout.println(Dialect.Oracle12cDialect.trans("select concat('a','b','c'), current_time()  from user_tb"));
		Systemout.println(
				Dialect.SQLServer2008Dialect.trans("select concat('a','b','c'), current_time()  from user_tb"));

		Systemout.println("============================================");

		Systemout.println(
				Dialect.MySQL55Dialect.paginAndTrans(3, 10, "select concat('a','b','c'), current_time() from user_tb"));
		Systemout.println(Dialect.Oracle12cDialect.paginAndTrans(3, 10,
				"select concat('a','b','c'), current_time() from user_tb"));
		Systemout.println(Dialect.SQLServer2005Dialect.paginAndTrans(1, 10,
				"select concat('a','b','c'), current_time() from user_tb"));
	}

	@Test
	public void doPrefixTest() {
		// Default is null, here change to "#" only for test
		Dialect.setGlobalSqlFunctionPrefix("#");
		String result = Dialect.MySQL55Dialect.trans(
				"Select username, #concat(#second(#second(99)),'a', #second(20) ), #current_time(), #PI(), #concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		result = Dialect.SQLiteDialect.trans(
				"Select username, #concat(#second(#second(99)),'a', #second(20) ),   #concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		result = Dialect.MySQL55Dialect.paginAndTrans(2, 10,
				"Select username, #concat(#second(#second(99)),'a', #second(20) ), #current_time(), #PI(), #concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		result = Dialect.SQLiteDialect.paginAndTrans(2, 10,
				"Select username, #concat(#second(#second(99)),'a', #second(20) ),   #concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		Systemout.println("============================================");

		result = Dialect.MySQL55Dialect.trans(
				"Select username, concat(second(second(99)),'a', second(20) ), current_time(), PI(), concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		result = Dialect.SQLiteDialect.trans(
				"Select username, concat(second(second(99)),'a', second(20) ),   concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		result = Dialect.MySQL55Dialect.paginAndTrans(2, 10,
				"Select username, concat(second(second(99)),'a', second(20) ), current_time(), PI(), concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		result = Dialect.SQLiteDialect.paginAndTrans(2, 10,
				"Select username, concat(second(second(99)),'a', second(20) ),   concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		Systemout.println("============================================");

		// Default is null, here change to "#" only for test
		Dialect.setGlobalSqlFunctionPrefix("$fn_");
		result = Dialect.MySQL55Dialect.trans(
				"Select username, $fn_concat($fn_second($fn_second(99)),'a', $fn_second(20) ), $fn_current_time(), $fn_PI(), $fn_concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		result = Dialect.SQLiteDialect.trans(
				"Select username, $fn_concat($fn_second($fn_second(99)),'a', $fn_second(20) ),   $fn_concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		result = Dialect.MySQL55Dialect.paginAndTrans(2, 10,
				"Select username, $fn_concat($fn_second($fn_second(99)),'a', $fn_second(20) ), $fn_current_time(), $fn_PI(), $fn_concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		result = Dialect.SQLiteDialect.paginAndTrans(2, 10,
				"Select username, $fn_concat($fn_second($fn_second(99)),'a', $fn_second(20) ),   $fn_concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		Systemout.println("============================================");

		Dialect.setGlobalSqlFunctionPrefix(null);
		result = Dialect.MySQL55Dialect.trans(
				"Select username, concat(second(second(99)),'a', second(20) ), current_time(), PI(), concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		result = Dialect.SQLiteDialect.trans(
				"Select username, concat(second(second(99)),'a', second(20) ),   concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		result = Dialect.MySQL55Dialect.paginAndTrans(2, 10,
				"Select username, concat(second(second(99)),'a', second(20) ), current_time(), PI(), concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

		result = Dialect.SQLiteDialect.paginAndTrans(2, 10,
				"Select username, concat(second(second(99)),'a', second(20) ),   concat('a', b, c) as b from usertable as tb");
		Systemout.println(result);

	}

}
