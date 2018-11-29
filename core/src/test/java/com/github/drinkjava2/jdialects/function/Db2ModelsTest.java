/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jdialects.function;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

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
				System.out.println(tableModel.getTableName());
				for (ColumnModel columnModel : columns) {
					System.out.print(columnModel.getColumnName()+",");
					System.out.print(columnModel.getColumnType()+",");
					System.out.print(columnModel.getLength()+",");
					System.out.print(columnModel.getPrecision()+",");
					System.out.print(columnModel.getScale()+"\r");
				}
				System.out.println();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (con != null)
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}
}
