/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects;

import org.junit.Assert;
import org.junit.Test;

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
		System.out.println(t1);
		System.out.println(t2);
		Assert.assertNotEquals(t1, t2);
		Assert.assertNotNull(t2.getColumn("name").getTableModel());

		System.out.println("================");
		for (ColumnModel item : t1.getColumns()) {
			System.out.println(item);
		}
		System.out.println("================");
		for (ColumnModel item : t2.getColumns()) {
			System.out.println(item);
		}
	}
}
