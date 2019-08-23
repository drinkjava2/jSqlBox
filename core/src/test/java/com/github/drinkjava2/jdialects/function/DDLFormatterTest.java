/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects.function;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.DDLFormatter;

/**
 * This is for DDLFormatter Test
 * 
 * @author Yong Z.
 *
 */
public class DDLFormatterTest {

	@Test
	public void testDDLFormatter() {
		Systemout.println(DDLFormatter.format(
				"create column table customertable (id varchar(32) not null, customer_name varchar(30), primary key (id))"));
		Systemout.println(DDLFormatter.format(
				"create table customertable (id varchar(32) not null, customer_name varchar(30), primary key (id)) engine=InnoDB"));
	}

}
