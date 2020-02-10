package com.github.drinkjava2.jsqlbox.function.jdialects.typemapping;

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
		Long l1;
		long l2;
		Byte b1;
		byte b2;
		Short s1;
		short s2;
		Double d1;
		double d2;
		Float f1;
		float f2;
		Boolean bl1;
		boolean bl2;

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

		public Long getL1() {
			return l1;
		}

		public void setL1(Long l1) {
			this.l1 = l1;
		}

		public long getL2() {
			return l2;
		}

		public void setL2(long l2) {
			this.l2 = l2;
		}

		public Byte getB1() {
			return b1;
		}

		public void setB1(Byte b1) {
			this.b1 = b1;
		}

		public byte getB2() {
			return b2;
		}

		public void setB2(byte b2) {
			this.b2 = b2;
		}

		public Short getS1() {
			return s1;
		}

		public void setS1(Short s1) {
			this.s1 = s1;
		}

		public short getS2() {
			return s2;
		}

		public void setS2(short s2) {
			this.s2 = s2;
		}

		public Double getD1() {
			return d1;
		}

		public void setD1(Double d1) {
			this.d1 = d1;
		}

		public double getD2() {
			return d2;
		}

		public void setD2(double d2) {
			this.d2 = d2;
		}

		public Float getF1() {
			return f1;
		}

		public void setF1(Float f1) {
			this.f1 = f1;
		}

		public float getF2() {
			return f2;
		}

		public void setF2(float f2) {
			this.f2 = f2;
		}

		public Boolean getBl1() {
			return bl1;
		}

		public void setBl1(Boolean bl1) {
			this.bl1 = bl1;
		}

		public boolean isBl2() {
			return bl2;
		}

		public void setBl2(boolean bl2) {
			this.bl2 = bl2;
		}

	}

	@Test
	public void testNumberDemo() {
		quietDropTables(NumberDemo.class);
		createTables(NumberDemo.class);
		NumberDemo w = new NumberDemo();
		w.setI1(1);
		w.setI2(2);
		w.setB1((byte) 3);
		w.setB2((byte) 4);
		w.setS1((short) 1);
		w.setS2((short) 2);
		w.setL1(5L);
		w.setL2(6L);
		w.setD1(7.0);
		w.setD2(8.0);
		w.setF1(9.0f);
		w.setF2(10.0f);
		w.setBl1(true);
		w.setBl2(true);
		w.insert();

		NumberDemo r = new NumberDemo();
		r.setId(w.getId());
		r.load();
		Assert.assertNotNull(r.i1);
		Assert.assertNotNull(r.i2);
		Assert.assertNotNull(r.s1);
		Assert.assertNotNull(r.s2);
		Assert.assertNotNull(r.l1);
		Assert.assertNotNull(r.l2);
		Assert.assertNotNull(r.d1);
		Assert.assertNotNull(r.d2);
		Assert.assertNotNull(r.f1);
		Assert.assertNotNull(r.f2);
		Assert.assertNotNull(r.b1);
		Assert.assertNotNull(r.b2);
		Assert.assertNotNull(r.bl1);
		Assert.assertNotNull(r.bl2);

		Systemout.println(r.i1);
		Systemout.println(r.i2);
		Systemout.println(r.s1);
		Systemout.println(r.s2);
		Systemout.println(r.l1);
		Systemout.println(r.l2);
		Systemout.println(r.d1);
		Systemout.println(r.d2);
		Systemout.println(r.f1);
		Systemout.println(r.f2);
		Systemout.println(r.b1);
		Systemout.println(r.b2);
		Systemout.println(r.bl1);
		Systemout.println(r.bl2);


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
