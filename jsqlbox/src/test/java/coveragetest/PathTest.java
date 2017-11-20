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

import com.github.drinkjava2.jtinynet.BeanValidator;
import com.github.drinkjava2.jtinynet.Path;

/**
 * Coverage test for Path
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class PathTest {
	private static class ChildChecker extends BeanValidator {

	}

	/**
	 * If Path has a non-empty uniqueIdString, the query result will be cached (by
	 * default), so it's important to test it, only unchangeable Path instance can
	 * allow have non-empty uniqueIdString
	 */
	@Test
	public void testPath() {
		Path p = new Path("C+", "someTable1").nextPath("P+", "someTable2").setValidator(new BeanValidator());
		System.out.println(p.getUniqueIdString());
		Assert.assertNull(p.getUniqueIdString());

		p = new Path("C+", "someTable1").nextPath("P+", "someTable2").setValidator(BeanValidator.class);
		System.out.println(p.getUniqueIdString());
		Assert.assertNotNull(p.getUniqueIdString());

		p = new Path("C+", "someTable1").nextPath("P+", "someTable2").setValidator(new ChildChecker());
		System.out.println(p.getUniqueIdString());
		Assert.assertNull(p.getUniqueIdString());

		p = new Path("C+", "someTable1").nextPath("P+", "someTable2").setValidator(ChildChecker.class);
		System.out.println(p.getUniqueIdString());
		Assert.assertNotNull(p.getUniqueIdString());

		p = new Path("C+", "someTable1").nextPath("P+", "someTable2").setValidator(ChildChecker.class)
				.nextPath("C+", "aaa").nextPath("C+", "bbb").nextPath("P+", "ccc").setValidator(new ChildChecker());
		System.out.println(p.getUniqueIdString());
		Assert.assertNull(p.getUniqueIdString());
	}

}
