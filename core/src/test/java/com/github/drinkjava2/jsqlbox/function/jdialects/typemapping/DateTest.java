package com.github.drinkjava2.jsqlbox.function.jdialects.typemapping;
/*- JAVA8_BEGIN */
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Temporal;
import com.github.drinkjava2.jdialects.annotation.jpa.TemporalType;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveEntity;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * Test Date time type
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class DateTest extends TestBase implements ActiveEntity<DateTest> {
	private static final Date D3000 = new Date(40000000000000l); // year 3237
	private static final Date D2000 = new Date(1000000000000l); // year 2001
	private static final java.sql.Date SQLD3000 = new java.sql.Date(D3000.getTime()); // year 3237

	private void useField(int x) {// Use jDialect's dynamic configuration to disable other fields
		TableModel model = TableModelUtils.entity2Model(DateTest.class);
		for (int i = 1; i <= 20; i++)
			try {
				model.column("d" + i).setTransientable(true);
			} catch (Exception e) {
			}
		model.column("d" + x).setTransientable(false);
		TableModelUtils.bindGlobalModel(DateTest.class, model);
		quietCreateRegTables(DateTest.class);
	}

	@Temporal(TemporalType.DATE)
	java.util.Date d1;

	@Test
	public void testD1() {
		useField(1);
		DateTest in = new DateTest();
		in.setD1(D3000);
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD1());
		Systemout.println(out.getD1());
	}

	@Column
	java.sql.Date d2;

	@Test
	public void testD2() {
		useField(2);
		DateTest in = new DateTest();
		in.setD2(new java.sql.Date(D3000.getTime()));
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD2());
		Systemout.println(out.getD2());
	}

	@Column
	java.sql.Time d3;

	@Test
	public void testD3() {
		useField(3);
		DateTest in = new DateTest();
		in.setD3(new java.sql.Time(D3000.getTime()));
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD3());
		Systemout.println(out.getD3());
	}

	@Column
	java.sql.Timestamp d4;

	@Test
	public void testD4() {
		useField(4);
		DateTest in = new DateTest();
		in.setD4(new java.sql.Timestamp(D3000.getTime()));
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD4());
		Systemout.println(out.getD4());
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "timestamp")
	java.util.Date d5;

	@Test
	public void testD5() {
		useField(5);
		DateTest in = new DateTest();
		in.setD5(D2000);
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD5());
		Systemout.println(out.getD5());
	}

	@Column(columnDefinition = "timestamp")
	java.sql.Date d6;

	@Test
	public void testD6() {
		useField(6);
		DateTest in = new DateTest();
		in.setD6(new java.sql.Date(D2000.getTime()));
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD6());
		Systemout.println(out.getD6());
	}

	@Column(columnDefinition = "timestamp")
	java.sql.Timestamp d7;

	@Test
	public void testD7() {
		useField(7);
		DateTest in = new DateTest();
		in.setD7(new Timestamp(D2000.getTime()));
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD7());
		Systemout.println(out.getD7());
	}

	@Column
	java.util.Calendar d8;

	@Test
	public void testD8() {
		useField(8);
		DateTest in = new DateTest();
		Calendar c = Calendar.getInstance();
		c.setTime(D3000);
		in.setD8(c);
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD8());
		Systemout.println(out.getD8());
	}

	@Column(insertable = false, columnDefinition = "timestamp not null default now() comment 'CREATE TIME'")
	java.util.Date d9;

	@Test
	public void testD9() {
		if (!dialect.isMySqlFamily())
			return;
		useField(9);
		DateTest in = new DateTest();
		in.setD9(D2000);
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD9());
		System.out.println(out.getD9());
	}

	@Temporal(TemporalType.DATE)
	java.util.Date d10;

	@Test
	public void testD10() {
		useField(10);
		DateTest in = new DateTest();
		in.setD10(D3000);
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD10());
		Systemout.println(out.getD10());
	}

	@Temporal(TemporalType.TIME)
	java.util.Date d11;

	@Test
	public void testD11() {
		useField(11);
		DateTest in = new DateTest();
		in.setD11(D3000);
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD11());
		Systemout.println(out.getD11());
	}

	@Temporal(TemporalType.TIMESTAMP)
	java.util.Date d12;

	@Test
	public void testD12() {
		useField(12);
		DateTest in = new DateTest();
		in.setD12(D3000);
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD12());
		Systemout.println(out.getD12());
	}

	@Temporal(TemporalType.DATE)
	java.sql.Date d13;

	@Test
	public void testD13() {
		useField(13);
		DateTest in = new DateTest();
		in.setD13(SQLD3000);
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD13());
		Systemout.println(out.getD13());
	}

	@Temporal(TemporalType.TIME)
	java.sql.Date d14;

	@Test
	public void testD14() {
		useField(14);
		DateTest in = new DateTest();
		in.setD14(SQLD3000);
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD14());
		Systemout.println(out.getD14());
	}

	@Temporal(TemporalType.TIMESTAMP)
	java.sql.Date d15;

	@Test
	public void testD15() {
		useField(15);
		DateTest in = new DateTest();
		in.setD15(SQLD3000);
		in.insert();
		DateTest out = new DateTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getD15());
		Systemout.println(out.getD15());
	}

	public void getterSetters______________________() {
	}

	@Id
	@UUID25
	String id;

	public String getId() {
		return id;
	}

	public DateTest setId(String id) {
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

	public java.sql.Timestamp getD7() {
		return d7;
	}

	public void setD7(java.sql.Timestamp d7) {
		this.d7 = d7;
	}

	public java.util.Calendar getD8() {
		return d8;
	}

	public void setD8(java.util.Calendar d8) {
		this.d8 = d8;
	}

	public java.util.Date getD9() {
		return d9;
	}

	public void setD9(java.util.Date d9) {
		this.d9 = d9;
	}

	public java.util.Date getD10() {
		return d10;
	}

	public void setD10(java.util.Date d10) {
		this.d10 = d10;
	}

	public java.util.Date getD11() {
		return d11;
	}

	public void setD11(java.util.Date d11) {
		this.d11 = d11;
	}

	public java.util.Date getD12() {
		return d12;
	}

	public void setD12(java.util.Date d12) {
		this.d12 = d12;
	}

	public java.sql.Date getD13() {
		return d13;
	}

	public void setD13(java.sql.Date d13) {
		this.d13 = d13;
	}

	public java.sql.Date getD14() {
		return d14;
	}

	public void setD14(java.sql.Date d14) {
		this.d14 = d14;
	}

	public java.sql.Date getD15() {
		return d15;
	}

	public void setD15(java.sql.Date d15) {
		this.d15 = d15;
	}

}
/*- JAVA8_END */