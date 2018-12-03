/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * 
 * @author Yong Z.
 * @since 1.0.6
 *
 */
public class TableModelTest {

 	@Test
	public void cloneTest() {
		TableModel t1 = new TableModel("customers");
		t1.column("name").STRING(20).pkey();
		t1.column("email").STRING(20).pkey().entityField("email").updatable(true).insertable(false);
		t1.column("address").VARCHAR(50).defaultValue("'Beijing'").comment("address comment");
		t1.column("phoneNumber").VARCHAR(50).singleIndex("IDX2");
		t1.column("age").INTEGER().notNull().check("'>0'");
		t1.index("idx3").columns("address", "phoneNumber").unique();

		Assert.assertNotNull(t1.getColumn("name").getTableModel());

		TableModel t2 = t1.newCopy();
		Systemout.println(t1);
		Systemout.println(t2);
		Assert.assertNotEquals(t1, t2);
		Assert.assertNotNull(t2.getColumn("name").getTableModel());

		Systemout.println("================");
		for (ColumnModel item : t1.getColumns()) {
			Systemout.println(item);
		}
		Systemout.println("================");
		for (ColumnModel item : t2.getColumns()) {
			Systemout.println(item);
		}
	}
}
