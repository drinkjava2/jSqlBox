package com.github.drinkjava2.jsqlbox.function.jdialects.typemapping;

import java.math.BigInteger;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * Test Date time type
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class TypeMappingTest extends TestBase {

	public static class NumberDemo extends ActiveRecord<NumberDemo> {
		@Id
		@UUID25
		String id;

		Integer i1;
		int i2;
		Long l3;
		long l4;
		Byte b5;
		byte b6;
		Short s7;
		short s8;
		Double d9;
		double d10;
		Float f11;
		float f12;
		Boolean bl13;
		boolean bl14;

		Character c15;
		char c16;

		BigInteger i17;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Integer getI1() {
			return i1;
		}

		public void setI1(Integer i1) {
			this.i1 = i1;
		}

		public int getI2() {
			return i2;
		}

		public void setI2(int i2) {
			this.i2 = i2;
		}

		public Long getL3() {
			return l3;
		}

		public void setL3(Long l) {
			this.l3 = l;
		}

		public long getL4() {
			return l4;
		}

		public void setL4(long l) {
			this.l4 = l;
		}

		public Byte getB5() {
			return b5;
		}

		public void setB5(Byte b) {
			this.b5 = b;
		}

		public byte getB6() {
			return b6;
		}

		public void setB6(byte b) {
			this.b6 = b;
		}

		public Short getS7() {
			return s7;
		}

		public void setS7(Short s) {
			this.s7 = s;
		}

		public short getS8() {
			return s8;
		}

		public void setS8(short s) {
			this.s8 = s;
		}

		public Double getD9() {
			return d9;
		}

		public void setD9(Double d) {
			this.d9 = d;
		}

		public double getD10() {
			return d10;
		}

		public void setD10(double d) {
			this.d10 = d;
		}

		public Float getF11() {
			return f11;
		}

		public void setF11(Float f) {
			this.f11 = f;
		}

		public float getF12() {
			return f12;
		}

		public void setF12(float f) {
			this.f12 = f;
		}

		public Boolean getBl13() {
			return bl13;
		}

		public void setBl13(Boolean b) {
			this.bl13 = b;
		}

		public boolean isBl14() {
			return bl14;
		}

		public void setBl14(boolean b) {
			this.bl14 = b;
		}

		public Character getC15() {
			return c15;
		}

		public void setC15(Character c) {
			this.c15 = c;
		}

		public char getC16() {
			return c16;
		}

		public void setC16(char c) {
			this.c16 = c;
		}

		public BigInteger getI17() {
			return i17;
		}

		public void setI17(BigInteger b) {
			this.i17 = b;
		}

	}

	@Test
	public void testNumberDemo() {
		quietDropTables(NumberDemo.class);
		createTables(NumberDemo.class);
		NumberDemo w = new NumberDemo();
		w.setI1(1);
		w.setI2(2);
		w.setB5((byte) 3);
		w.setB6((byte) 4);
		w.setS7((short) 1);
		w.setS8((short) 2);
		w.setL3(5L);
		w.setL4(6L);
		w.setD9(7.0);
		w.setD10(8.0);
		w.setF11(9.0f);
		w.setF12(10.0f);
		w.setBl13(true);
		w.setBl14(true);
		w.setC15('a');
		w.setC16('b');
		w.setI17(BigInteger.valueOf(5L));
		w.insert();

		NumberDemo r = new NumberDemo();
		r.setId(w.getId());
		r.load();
		Assert.assertNotNull(r.i1);
		Assert.assertNotNull(r.i2);
		Assert.assertNotNull(r.s7);
		Assert.assertNotNull(r.s8);
		Assert.assertNotNull(r.l3);
		Assert.assertNotNull(r.l4);
		Assert.assertNotNull(r.d9);
		Assert.assertNotNull(r.d10);
		Assert.assertNotNull(r.f11);
		Assert.assertNotNull(r.f12);
		Assert.assertNotNull(r.b5);
		Assert.assertNotNull(r.b6);
		Assert.assertNotNull(r.bl13);
		Assert.assertNotNull(r.bl14);
		Assert.assertNotNull(r.c15);
		Assert.assertNotNull(r.c16);
		Assert.assertNotNull(r.i17);

		Systemout.println(r.i1);
		Systemout.println(r.i2);
		Systemout.println(r.s7);
		Systemout.println(r.s8);
		Systemout.println(r.l3);
		Systemout.println(r.l4);
		Systemout.println(r.d9);
		Systemout.println(r.d10);
		Systemout.println(r.f11);
		Systemout.println(r.f12);
		Systemout.println(r.b5);
		Systemout.println(r.b6);
		Systemout.println(r.bl13);
		Systemout.println(r.bl14);
		Systemout.println(r.c15);
		Systemout.println(r.c16);
		Systemout.println(r.i17);

		dropTables(NumberDemo.class);
	}

	public static class DateDemo extends ActiveRecord<DateDemo> {
		@Id
		@UUID25
		String id;

		java.sql.Date date1;
		java.util.Date date2;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public java.sql.Date getDate1() {
			return date1;
		}

		public void setDate1(java.sql.Date date1) {
			this.date1 = date1;
		}

		public java.util.Date getDate2() {
			return date2;
		}

		public void setDate2(java.util.Date date2) {
			this.date2 = date2;
		}

	}

	@Test
	public void testTypeMapping() {
		quietDropTables(DateDemo.class);
		createTables(DateDemo.class);
		DateDemo w = new DateDemo();
		w.setDate1(new java.sql.Date(new java.util.Date().getTime()));
		w.setDate2(new Date());
		w.insert();

		DateDemo r = new DateDemo();
		r.setId(w.getId());
		r.load();
		Assert.assertNotNull(r.getDate1());
		Assert.assertNotNull(r.getDate2());
		dropTables(DateDemo.class);
	}

}
