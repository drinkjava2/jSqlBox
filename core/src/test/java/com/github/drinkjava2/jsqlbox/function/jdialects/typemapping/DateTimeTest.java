package com.github.drinkjava2.jsqlbox.function.jdialects.typemapping;

import java.util.Calendar;
import java.util.Date;

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
		for (int i = 1; i <= 17; i++)
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
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD1());
		Systemout.println(out.getD1());
	}

	@Test
	public void testD2() {
		useField(2);
		DT in = new DT();
		in.setD2(new java.sql.Date(0L));
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD2());
		Systemout.println(out.getD2());
	}

	@Test
	public void testD3() {
		useField(3);
		DT in = new DT();
		in.setD3(new java.sql.Time(0L));
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD3());
		Systemout.println(out.getD3());
	}

	@Test
	public void testD4() {
		useField(4);
		DT in = new DT();
		in.setD4(new java.sql.Timestamp(0L));
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD4());
		Systemout.println(out.getD4());
	}

	@Test
	public void testD5() {
		useField(5);
		DT in = new DT();
		in.setD5(new java.util.Date());
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD5());
		Systemout.println(out.getD5());
	}

	@Test
	public void testD6() {
		useField(6);
		DT in = new DT();
		in.setD6(new java.sql.Date(0L));
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD6());
		Systemout.println(out.getD6());
	}

	@Test
	public void testD7() {
		useField(7);
		DT in = new DT();
		in.setD7(new java.sql.Time(0L));
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD7());
		Systemout.println(out.getD7());
	}

	@Test
	public void testD8() {
		useField(8);
		DT in = new DT();
		in.setD8(new java.sql.Timestamp(0L));
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD8());
		Systemout.println(out.getD8());
	}

	@Test
	public void testD9() {
		useField(9);
		DT in = new DT();
		in.setD9(new java.util.Date());
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD9());
		Systemout.println(out.getD9());
	}

	@Test
	public void testD10() {
		useField(10);
		DT in = new DT();
		in.setD10(new java.sql.Date(0L));
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD10());
		Systemout.println(out.getD10());
	}

	@Test
	public void testD11() {
		useField(11);
		DT in = new DT();
		in.setD11(new java.sql.Time(0L));
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD11());
		Systemout.println(out.getD11());
	}

	@Test
	public void testD12() {
		useField(12);
		DT in = new DT();
		in.setD12(new java.sql.Timestamp(0L));
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD12());
		Systemout.println(out.getD12());
	}

	@Test
	public void test13() {
		useField(13);
		DT in = new DT();
		in.setD13(new java.util.Date());
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD13());
		Systemout.println(out.getD13());
	}

	@Test
	public void testD14() {
		useField(14);
		DT in = new DT();
		in.setD14(new java.sql.Date(0L));
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD14());
		Systemout.println(out.getD14());
	}

	@Test
	public void testD15() {
		useField(15);
		DT in = new DT();
		in.setD15(new java.sql.Time(0L));
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD15());
		Systemout.println(out.getD15());
	}

	@Test
	public void testD16() {
		useField(16);
		DT in = new DT();
		in.setD16(new java.sql.Timestamp(0L));
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD16());
		Systemout.println(out.getD16());
	}
	
	@Test
	public void testD17() {
		useField(17);
		DT in = new DT();
		Calendar c=Calendar.getInstance();
		c.setTime(new Date()); 
		in.setD17(c);
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD17());
		Systemout.println(out.getD17());
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

		@Column(columnDefinition = "timestamp")
		java.util.Date d5;

		@Column(columnDefinition = "timestamp not null") // JPA allow add extra string in columnDefinition
		java.sql.Date d6;

		@Column(columnDefinition = "timestamp")
		java.sql.Time d7;

		@Column(columnDefinition = "timestamp")
		java.sql.Timestamp d8;

		@Column(columnDefinition = "date")
		java.util.Date d9;

		@Column(columnDefinition = "date")
		java.sql.Date d10;

		@Column(columnDefinition = "date")
		java.sql.Time d11;

		@Column(columnDefinition = "date")
		java.sql.Timestamp d12;

		@Column(columnDefinition = "time")
		java.util.Date d13;

		@Column(columnDefinition = "time")
		java.sql.Date d14;

		@Column(columnDefinition = "time")
		java.sql.Time d15;

		@Column(columnDefinition = "time")
		java.sql.Timestamp d16;

		@Column
		java.util.Calendar d17;

		public DT() {
		}

		public DT(String id) {
			this.id = id;
		}

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

		public java.util.Date getD9() {
			return d9;
		}

		public void setD9(java.util.Date d9) {
			this.d9 = d9;
		}

		public java.sql.Date getD10() {
			return d10;
		}

		public void setD10(java.sql.Date d10) {
			this.d10 = d10;
		}

		public java.sql.Time getD11() {
			return d11;
		}

		public void setD11(java.sql.Time d11) {
			this.d11 = d11;
		}

		public java.sql.Timestamp getD12() {
			return d12;
		}

		public void setD12(java.sql.Timestamp d12) {
			this.d12 = d12;
		}

		public java.util.Date getD13() {
			return d13;
		}

		public void setD13(java.util.Date d13) {
			this.d13 = d13;
		}

		public java.sql.Date getD14() {
			return d14;
		}

		public void setD14(java.sql.Date d14) {
			this.d14 = d14;
		}

		public java.sql.Time getD15() {
			return d15;
		}

		public void setD15(java.sql.Time d15) {
			this.d15 = d15;
		}

		public java.sql.Timestamp getD16() {
			return d16;
		}

		public void setD16(java.sql.Timestamp d16) {
			this.d16 = d16;
		}

		public java.util.Calendar getD17() {
			return d17;
		}

		public void setD17(java.util.Calendar d17) {
			this.d17 = d17;
		}

	}

}
