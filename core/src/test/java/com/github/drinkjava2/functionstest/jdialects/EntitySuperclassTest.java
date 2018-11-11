/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.functionstest.jdialects;

import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

/**
 * @author Yong Zhu
 * @since 2.4.9
 */
public class EntitySuperclassTest extends TestBase {
	{

		regTables(Child.class);
	}

	public static class Parent<M> extends ActiveRecord<M> {
		@Id
		@UUID25
		String id;

		String name;

		@Column(name="addr")
		String address;

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

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

	}

	public static class Child extends Parent<Child> {
	}

	static class A {
	}

	@Test
	public void testSuperclassInjection() {
		Child c = new Child();
		c.setName("Tom");
		c.setAddress("China");
		c.insert();
		
		new Child().putField("name","Sam").insert();

	}

}
