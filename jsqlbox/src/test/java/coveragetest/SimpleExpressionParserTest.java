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

import com.github.drinkjava2.jtinynet.parser.SimpleExpressionParser;

import functiontest.orm.entities.User;

/**
 * Coverage test for SimpleExpressionParser
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class SimpleExpressionParserTest {

	@Test
	public void testMath() {
		Assert.assertEquals(-48L, SimpleExpressionParser.instance.doParse(null, null, " -(9-(3+3))*8 -24 "));
		Assert.assertEquals(-48L, SimpleExpressionParser.instance.doParse(null, null, "-(9-(3+9/3))*8 -24"));
	}

	@Test
	public void testBoolen() {
		Assert.assertEquals(true, SimpleExpressionParser.instance.doParse(null, null, " not not ( not  not(true))"));

		Assert.assertEquals(true, SimpleExpressionParser.instance.doParse(null, null, "true"));

		Assert.assertEquals(false, SimpleExpressionParser.instance.doParse(null, null, "false"));

		Assert.assertEquals(false, SimpleExpressionParser.instance.doParse(null, null, "not(true)"));

		Assert.assertEquals(true, SimpleExpressionParser.instance.doParse(null, null, "not(false)"));

		String s = "(1+1)=2 and not(1=2) and not(false) and true and (1=1 or 1=2) and  'Tom' equalsIgnoreCase 'tom' ";
		Assert.assertEquals(true, SimpleExpressionParser.instance.doParse(null, null, s));
	}

	@Test
	public void testStringFunctions() {
		Assert.assertEquals(true, SimpleExpressionParser.instance.doParse(null, null, "'Tom' equals 'Tom'"));
		Assert.assertEquals(false, SimpleExpressionParser.instance.doParse(null, null, "'Tom' equals 'tom'"));
		Assert.assertEquals(true, SimpleExpressionParser.instance.doParse(null, null, "'Tom' equalsIgnoreCase 'tom'"));

		Assert.assertEquals(true, SimpleExpressionParser.instance.doParse(null, null, "'Tom' contains 'om'"));
		Assert.assertEquals(false, SimpleExpressionParser.instance.doParse(null, null, "'Tom' contains 'tom'"));
		Assert.assertEquals(true,
				SimpleExpressionParser.instance.doParse(null, null, "'Tom' containsIgnoreCase 'tom'"));

		Assert.assertEquals(true, SimpleExpressionParser.instance.doParse(null, null, "'Tom' startWith 'To'"));
		Assert.assertEquals(false, SimpleExpressionParser.instance.doParse(null, null, "'Tom' startWith 'to'"));
		Assert.assertEquals(true,
				SimpleExpressionParser.instance.doParse(null, null, "'Tom' startWithIgnoreCase 'to'"));

		Assert.assertEquals(true, SimpleExpressionParser.instance.doParse(null, null, "'Tom' endWith 'om'"));
		Assert.assertEquals(false, SimpleExpressionParser.instance.doParse(null, null, "'Tom' endWith 'OM'"));
		Assert.assertEquals(true, SimpleExpressionParser.instance.doParse(null, null, "'Tom' endWithIgnoreCase 'OM'"));
	}

	@Test
	public void testBeanField1() {
		String s = "userName equals 'Tom'";
		User user = new User();
		user.setUserName("Tom");
		Assert.assertEquals(true, SimpleExpressionParser.instance.doParse(user, null, s));
	}

}
