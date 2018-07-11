/**
 * Copyright (C) 2016 Original Author
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.functionstest;

import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jdialects.annotation.jpa.Entity;
import com.github.drinkjava2.jdialects.annotation.jpa.GeneratedValue;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.annotation.jpa.TableGenerator;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

/**
 * Unit test for SortedUUIDGenerator
 */
public class IdGeneratorInEntityTest extends TestBase {
	{
		regTables(TeamDemo.class);
	}
 
	@Test
	public void IdGeneratorInEntity1() {
		for (int i = 0; i < 5; i++)
			new TeamDemo().put("name", "Team" + i).insert(new PrintSqlHandler());
	}
	
	
	
	@Entity
	@Table(name = "teams")
	public static class TeamDemo extends ActiveRecord<TeamDemo> {
		@Id
		@TableGenerator(name = "ID_GENERATOR", table = "pk_table", pkColumnName = "pk_col", pkColumnValue = "pk_val", valueColumnName = "val_col", initialValue = 1, allocationSize = 1)
		@GeneratedValue(strategy = GenerationType.TABLE, generator = "ID_GENERATOR")
		private Integer id;

		private String name;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

}
