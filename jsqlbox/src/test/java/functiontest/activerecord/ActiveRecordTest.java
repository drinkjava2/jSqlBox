/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package functiontest.activerecord;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import config.TestBase;

/**
 * Demo of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class ActiveRecordTest extends TestBase {

	public static class UserDemo extends ActiveRecord {
		private String name;
		private String address;

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

	@Test
	public void doText() {
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		String[] ddls = ctx.pojos2CreateDDLs(UserDemo.class);
		for (String ddl : ddls)
			ctx.nExecute(ddl);

		UserDemo u = new UserDemo();
		u.setName("Sam");
		ctx.insert(u);

		u.setName("Tom");
		u.insert();

		Assert.assertEquals(2L, ctx.nQueryForObject("select count(*) from " + u.table()));
	}

}