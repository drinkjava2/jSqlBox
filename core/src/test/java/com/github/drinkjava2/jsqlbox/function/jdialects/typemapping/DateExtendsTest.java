package com.github.drinkjava2.jsqlbox.function.jdialects.typemapping;
/*- JAVA8_BEGIN */

import java.util.Date;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID26;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Temporal;
import com.github.drinkjava2.jdialects.annotation.jpa.TemporalType;
import com.github.drinkjava2.jsqlbox.ActiveEntity;
import com.github.drinkjava2.jsqlbox.config.TestBase;

// Test Date type Extends  
public class DateExtendsTest extends TestBase {

	public static class Father {

		@Temporal(TemporalType.TIMESTAMP)
		java.util.Date createTime;

		public java.util.Date getCreateTime() {
			return createTime;
		}

		public void setCreateTime(java.util.Date createTime) {
			this.createTime = createTime;
		}

	}

	public static class Child extends Father implements ActiveEntity<Child> {
		@Id
		@UUID26
		String id;

		@Temporal(TemporalType.TIMESTAMP)
		java.util.Date birthDay;

		public String getId() {
			return id;
		}

		public Child setId(String id) {
			this.id = id;
			return this;
		}

		public java.util.Date getBirthDay() {
			return birthDay;
		}

		public void setBirthDay(java.util.Date birthDay) {
			this.birthDay = birthDay;
		}

	}

	@Test
	public void testD1() {
		this.createAndRegTables(Child.class);
		Child c = new Child();
		c.setCreateTime(new Date(40000000000000L));
		c.setBirthDay(new Date(40000000000001L));
		Systemout.println(c.getCreateTime());
		Systemout.println(c.getBirthDay());
		c.insert();
		Systemout.println(c.getCreateTime());
		Systemout.println(c.getBirthDay());
		Systemout.println(c.getCreateTime().getClass());

		Child c2 = new Child().setId(c.getId()).load();
		Systemout.println(c2.getCreateTime());
		Systemout.println(c2.getBirthDay());
		Systemout.println(c2.getCreateTime().getClass());

		Child c3 = new Child();
		c3.setCreateTime(c2.getCreateTime());
		c3.setBirthDay(c2.getBirthDay());
		c3.insert();

		Child c4 = new Child().setId(c3.getId()).load();
		Systemout.println(c4.getCreateTime());
		Systemout.println(c4.getBirthDay());
		Systemout.println(c4.getCreateTime().getClass());

	}

}

/* JAVA8_END */