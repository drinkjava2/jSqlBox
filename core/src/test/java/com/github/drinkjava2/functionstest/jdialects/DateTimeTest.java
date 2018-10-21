package com.github.drinkjava2.functionstest.jdialects;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

/**
 * Test Date time type
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class DateTimeTest extends TestBase {
	{
		regTables(DateDemo.class);
	}

	public static class DateDemo extends ActiveRecord<DateDemo> {
		@Id
		@UUID25
		String id;

		@Column(columnDefinition = "TIMESTAMP")
		java.util.Date d1;

		java.util.Date d2;

		java.sql.Date d3;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public java.util.Date getD1() {
			return d1;
		}

		public void setD1(java.util.Date d1) {
			this.d1 = d1;
		}

		public java.util.Date getD2() {
			return d2;
		}

		public void setD2(java.util.Date d2) {
			this.d2 = d2;
		}

		public java.sql.Date getD3() {
			return d3;
		}

		public void setD3(java.sql.Date d3) {
			this.d3 = d3;
		}
	}

	@Test
	public void testDateTime() {
		DateDemo d = new DateDemo();
		d.setD1(new java.util.Date());
		d.setD2(new java.util.Date());
		d.setD3(new java.sql.Date(0L));
		d.insert();

		DateDemo d2 = new DateDemo();
		d2.setId(d.getId());
		d2.load();
		Assert.assertNotNull(d2.getD1());
		Assert.assertNotNull(d2.getD2());
		Assert.assertNotNull(d2.getD3());

	}

}
