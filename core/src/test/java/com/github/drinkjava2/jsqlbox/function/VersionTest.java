package com.github.drinkjava2.jsqlbox.function;

import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jdia.PKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Version;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * This is unit test for test @Version annotation
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public class VersionTest extends TestBase {
	{
		regTables(VersionDemo.class);
	}

	public static class VersionDemo extends ActiveRecord<VersionDemo> {
		@PKey
		@UUID25
		private String id;

		private String name;

		@Version
		private Integer optlock;

		@Version
		private Short shortOptLock;

		@Version
		private Long longOptLock;

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

		public Integer getOptlock() {
			return optlock;
		}

		public void setOptlock(Integer optlock) {
			this.optlock = optlock;
		}

		public Short getShortOptLock() {
			return shortOptLock;
		}

		public void setShortOptLock(Short shortOptLock) {
			this.shortOptLock = shortOptLock;
		}

		public Long getLongOptLock() {
			return longOptLock;
		}

		public void setLongOptLock(Long longOptLock) {
			this.longOptLock = longOptLock;
		}

	}

	@Test
	public void testVersion() {
		VersionDemo v = new VersionDemo();
		v.setName("Foo");
		v.insert();
		v.setName("Bar");
		v.update();
		v.setName("Bar2");
		v.update();
		v.update();
		v.delete();
	}
}
