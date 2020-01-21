/*
 * Copyright (C) 2016 Original Author
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jsqlbox.function;

import org.junit.Test;

import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * ActiveRecordDemoTest of DbUtil-Plus configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class DynamicPojoConfigTest extends TestBase {
	{
		regTables(PojoDemo.class);
	}

	public static class PojoDemo {
		private String id;
		private String name;

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

	}

	@Test
	public void doPojoConfigTest() {
		TableModel model = TableModelUtils.entity2Model(PojoDemo.class);
		model.column("id").pkey().uuid32();
		TableModelUtils.bindGlobalModel(PojoDemo.class, model);

		PojoDemo pojo = new PojoDemo();
		pojo.setName("Tom"); 
		ctx.eInsert(pojo);
		ctx.eDelete(pojo);
	} 
}