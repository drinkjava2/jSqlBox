package com.github.drinkjava2.jsqlbox.function;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jdia.PKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.EnumType;
import com.github.drinkjava2.jdialects.annotation.jpa.Enumerated;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * This is unit test for test @Version annotation
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public class EnumTest extends TestBase {
	{
		regTables(EnumDemoBean.class);
	}

	public static enum EnumDemo {
		PART_TIME, FULL_TIME, CONTRACT
	}

	public static class EnumDemoBean extends ActiveRecord<EnumDemoBean> {
		@PKey
		@UUID25
		private String id;

		private String name;

		@Enumerated // EnumType.ORDINAL is default
		private EnumDemo enum1 = EnumDemo.PART_TIME;

		@Enumerated(EnumType.STRING)
		private EnumDemo enum2 = EnumDemo.PART_TIME;

		@Enumerated
		private EnumDemo enum3 = null; // null test

		@Enumerated(EnumType.STRING)
		private EnumDemo enum4 = null; // null test

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public EnumDemo getEnum1() {
			return enum1;
		}

		public void setEnum1(EnumDemo enum1) {
			this.enum1 = enum1;
		}

		public EnumDemo getEnum2() {
			return enum2;
		}

		public void setEnum2(EnumDemo enum2) {
			this.enum2 = enum2;
		}

		public EnumDemo getEnum3() {
			return enum3;
		}

		public void setEnum3(EnumDemo enum3) {
			this.enum3 = enum3;
		}

		public EnumDemo getEnum4() {
			return enum4;
		}

		public void setEnum4(EnumDemo enum4) {
			this.enum4 = enum4;
		}

	}

	@Test
	public void testEnum() {
		// Systemout.println(DebugUtils.getTableModelDebugInfo(TableModelUtils.entity2Model(EnumDemoBean.class)));

		EnumDemoBean v = new EnumDemoBean();
		v.setName("Foo");
		v.setEnum1(EnumDemo.FULL_TIME);
		v.setEnum2(EnumDemo.FULL_TIME);
		v.insert();
		v.setName("Bar");
		v.setEnum1(EnumDemo.CONTRACT);
		v.setEnum2(EnumDemo.CONTRACT);
		v.update();
		EnumDemoBean v2 = new EnumDemoBean().putField("id", v.getId()).load();
		Assert.assertEquals(EnumDemo.CONTRACT, v2.getEnum1());
		Assert.assertEquals(EnumDemo.CONTRACT, v2.getEnum2());
		v2.delete();
	}
}
