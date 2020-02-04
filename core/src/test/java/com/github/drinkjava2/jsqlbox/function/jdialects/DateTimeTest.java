package com.github.drinkjava2.jsqlbox.function.jdialects;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * Test Date time type
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class DateTimeTest extends TestBase {

	private void useField(int x) {// Use jDialect's dynamic configuration to disable other fields
		TableModel model = TableModelUtils.entity2Model(DT.class);
		for (int i = 1; i <= 8; i++)
			model.column("d" + i).setTransientable(true);
		model.column("d" + x).setTransientable(false);
		TableModelUtils.bindGlobalModel(DT.class, model);
		createAndRegTables(DT.class);
	}

	@Test
	public void testD1() {
		useField(1);
		DT in = new DT();
		in.setD1(new java.util.Date(0L));
		in.insert();
		DT out = new DT().setId(in.getId()).load();
		Assert.assertNotNull(out.getD1());
		Systemout.println(out.getD1());
	}

	@Test
	public void testD2() {
		useField(2);
		DT in = new DT();
		in.setD2(new java.sql.Date(0L));
		in.insert();
		DT out = new DT().setId(in.getId()).load();
		Assert.assertNotNull(out.getD2());
		Systemout.println(out.getD2());
	}

	@Test
	public void testD3() {
		useField(3);
		DT in = new DT();
		in.setD3(new java.sql.Time(0L));
		in.insert();
		DT out = new DT().setId(in.getId()).load();
		Assert.assertNotNull(out.getD3());
		Systemout.println(out.getD3());
	}

	@Test
	public void testD4() {
		useField(4);
		DT in = new DT();
		in.setD4(new java.sql.Timestamp(0L));
		in.insert();
		DT out = new DT().setId(in.getId()).load();
		Assert.assertNotNull(out.getD4());
		Systemout.println(out.getD4());
	}

	public static class DT extends ActiveRecord<DT> {
		@Id
		@UUID25
		String id;

		@Column
		java.util.Date d1;

		@Column
		java.sql.Date d2;

		@Column
		java.sql.Time d3;

		@Column
		java.sql.Timestamp d4;

		@Column
		java.util.Date d5;

		@Column
		java.sql.Date d6;

		@Column
		java.sql.Time d7;

		@Column
		java.sql.Timestamp d8;

		public String getId() {
			return id;
		}

		public DT setId(String id) {
			this.id = id;
			return this;
		}

		public java.util.Date getD1() {
			return d1;
		}

		public void setD1(java.util.Date d1) {
			this.d1 = d1;
		}

		public java.sql.Date getD2() {
			return d2;
		}

		public void setD2(java.sql.Date d2) {
			this.d2 = d2;
		}

		public java.sql.Time getD3() {
			return d3;
		}

		public void setD3(java.sql.Time d3) {
			this.d3 = d3;
		}

		public java.sql.Timestamp getD4() {
			return d4;
		}

		public void setD4(java.sql.Timestamp d4) {
			this.d4 = d4;
		}

		public java.util.Date getD5() {
			return d5;
		}

		public void setD5(java.util.Date d5) {
			this.d5 = d5;
		}

		public java.sql.Date getD6() {
			return d6;
		}

		public void setD6(java.sql.Date d6) {
			this.d6 = d6;
		}

		public java.sql.Time getD7() {
			return d7;
		}

		public void setD7(java.sql.Time d7) {
			this.d7 = d7;
		}

		public java.sql.Timestamp getD8() {
			return d8;
		}

		public void setD8(java.sql.Timestamp d8) {
			this.d8 = d8;
		}

	}

}
