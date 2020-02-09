/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects.function;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.SqlFormatter;

/**
 * This is for DDLFormatter Test
 * 
 * @author Yong Z.
 *
 */
public class SqlFormatterTest {

	@Test
	public void testDDLFormatter() {
		Systemout.println(
				SqlFormatter.format("select  \t     * from users \t  \n  where '1 =  1 '   or       '  or 2=2'\r\n"
						+ "or    3=3 order by    id"));
	}

}
