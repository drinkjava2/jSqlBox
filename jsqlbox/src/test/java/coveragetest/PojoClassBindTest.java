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
package coveragetest;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.ModelUtils;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.SqlBox;

import config.TestBase;

/**
 * Coverage unit test for bind a box to PojoClass 
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class PojoClassBindTest extends TestBase {

	public static class PojoBeanSample {
		SqlBox box;

		@Id
		private String field1;

		public String getField1() {
			return field1;
		}

		public void setField1(String field1) {
			this.field1 = field1;
		}

	}

	@Test
	public void pojoBindTest() {
		ctx.setAllowShowSQL(true);
		TableModel[] tables = ModelUtils.entity2Model(PojoBeanSample.class);
		dropAndCreateDatabase(tables);
		PojoBeanSample pojo = new PojoBeanSample();
		pojo.setField1("test");
		ctx.insert(pojo);
		PojoBeanSample pojo2 = ctx.load(PojoBeanSample.class, "test");
		Assert.assertEquals("test", pojo2.getField1());
		System.out.println(pojo2.getField1());
	}

}
