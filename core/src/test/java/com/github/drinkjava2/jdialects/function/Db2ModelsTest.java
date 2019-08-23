/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects.function;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.config.JdialectsTestBase;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * Unit test for SortedUUIDGenerator
 */
public class Db2ModelsTest extends JdialectsTestBase {

	@Test
	public void testDb2Model() {
		TableModel t = new TableModel("testTable");
		t.column("id").LONG().pkey();
		t.column("b1").BOOLEAN();
		t.column("d2").DOUBLE();
		t.column("f3").FLOAT(5);
		t.column("i4").INTEGER();
		t.column("l5").LONG();
		t.column("s6").SHORT();
		t.column("b7").BIGDECIMAL(10, 2);
		t.column("s8").STRING(20);
		t.column("d9").DATE();
		t.column("t10").TIME();
		t.column("t11").TIMESTAMP();
		t.column("v12").VARCHAR(300);

		String[] ddls = guessedDialect.toDropDDL(t);
		quietExecuteDDLs(ddls);
		ddls = guessedDialect.toCreateDDL(t);
		executeDDLs(ddls);

		Connection con = null;
		TableModel[] tableModels = null;
		try {
			con = ds.getConnection();
			tableModels = TableModelUtils.db2Models(con, guessedDialect);
			for (TableModel tableModel : tableModels) {
				List<ColumnModel> columns = tableModel.getColumns();
				Systemout.println(tableModel.getTableName());
				for (ColumnModel columnModel : columns) {
					Systemout.print(columnModel.getColumnName()+",");
					Systemout.print(columnModel.getColumnType()+",");
					Systemout.print(columnModel.getLength()+",");
					Systemout.print(columnModel.getPrecision()+",");
					Systemout.print(columnModel.getScale()+"\r");
				}
				Systemout.println();
			}

		} catch (SQLException e) {
			Systemout.println("Exception found: " + e.getMessage());
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (SQLException e) {
				Systemout.println("Exception found: " + e.getMessage());
			}
		}

	}
}
