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

import com.github.drinkjava2.jtinynet.Checker;
import com.github.drinkjava2.jtinynet.DefaultChecker;
import com.github.drinkjava2.jtinynet.Node;
import com.github.drinkjava2.jtinynet.Path;
import com.github.drinkjava2.jtinynet.TinyNet;

/**
 * Coverage unit test for ClassCacheUtils
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class PathTest {
	private static class ChildChecker extends Checker {

		@Override
		public Boolean input(TinyNet tinyNet, Node node) {
			return true;
		}

		@Override
		public Boolean output(TinyNet tinyNet, Node node) {
			return true;
		}

	}

	@Test
	public void testPath() {
		Path p = new Path("C", "someTable1").nextPath("P", "someTable2").setChecker(new DefaultChecker());
		System.out.println(p.getUniqueIdString());
		Assert.assertNull(p.getUniqueIdString());

		p = new Path("C", "someTable1").nextPath("P", "someTable2").setChecker(DefaultChecker.class);
		System.out.println(p.getUniqueIdString());
		Assert.assertNotNull(p.getUniqueIdString());

		p = new Path("C", "someTable1").nextPath("P", "someTable2").setChecker(new ChildChecker());
		System.out.println(p.getUniqueIdString());
		Assert.assertNull(p.getUniqueIdString());

		p = new Path("C", "someTable1").nextPath("P", "someTable2").setChecker(ChildChecker.class);
		System.out.println(p.getUniqueIdString());
		Assert.assertNotNull(p.getUniqueIdString());

		p = new Path("C", "someTable1").nextPath("P", "someTable2").setChecker(ChildChecker.class).nextPath("C", "aaa")
				.nextPath("C", "bbb").nextPath("P", "ccc").setChecker(new ChildChecker());
		System.out.println(p.getUniqueIdString());
		Assert.assertNull(p.getUniqueIdString());

	}

}
