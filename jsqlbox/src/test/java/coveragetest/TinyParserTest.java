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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jtinynet.parser.TinyParser;

import functiontest.tinynet.entities.User;

/**
 * Coverage test for TinyParser
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class TinyParserTest {

	@Test
	public void testMath() {
		Assert.assertEquals(-48L, TinyParser.instance.doParse(null, null, " -(9-(3+3))*8 -24 "));
		Assert.assertEquals(-47L, TinyParser.instance.doParse(null, null, "1-(9-(3+9/3))*8 -24"));
		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "1.0/100000000000=(2-1.0)/100000000000"));
	}

	@Test
	public void testBoolen() {
		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, " not not ( not  not(true))"));

		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "true"));

		Assert.assertEquals(false, TinyParser.instance.doParse(null, null, "false"));

		Assert.assertEquals(false, TinyParser.instance.doParse(null, null, "not(true)"));

		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "not(false)"));

		String s = "(1+1)=2 and not(1=2) and not(false) and true and (1=1 or 1=2) and  'Tom' equalsIgnoreCase 'tom' ";
		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, s));
	}

	@Test
	public void testStringFunctions() {
		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "'Tom' = 'Tom'"));
		Assert.assertEquals(false, TinyParser.instance.doParse(null, null, "'Tom' = 'tom'"));
		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "'Tom' <> 'tom'"));

		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "'Tom' equals 'Tom'"));
		Assert.assertEquals(false, TinyParser.instance.doParse(null, null, "'Tom' equals 'tom'"));
		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "'Tom' equalsIgnoreCase 'tom'"));

		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "'Tom' contains 'om'"));
		Assert.assertEquals(false, TinyParser.instance.doParse(null, null, "'Tom' contains 'tom'"));
		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "'Tom' containsIgnoreCase 'tom'"));

		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "'Tom' startWith 'To'"));
		Assert.assertEquals(false, TinyParser.instance.doParse(null, null, "'Tom' startWith 'to'"));
		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "'Tom' startWithIgnoreCase 'to'"));

		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "'Tom' endWith 'om'"));
		Assert.assertEquals(false, TinyParser.instance.doParse(null, null, "'Tom' endWith 'OM'"));
		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "'Tom' endWithIgnoreCase 'OM'"));
	}

	@Test
	public void testBeanField() {
		User user = new User();
		user.setUserName("Tom");
		user.setAge(5);
		Assert.assertEquals(true, TinyParser.instance.doParse(user, null, "userName equals 'Tom'"));
		Assert.assertEquals(false, TinyParser.instance.doParse(user, null, "userName equals 'tom'"));
		Assert.assertEquals(true, TinyParser.instance.doParse(user, null, "userName equalsIgnoreCase 'Tom'"));
		Assert.assertEquals(true,
				TinyParser.instance.doParse(user, null, "userName startWith ? and not(age*(10+2) -age>?)", "T", 100));
	}

	@Test
	public void testNull() {
		Assert.assertEquals(false, TinyParser.instance.doParse(null, null, "null = 'abc'"));
		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "null <> 'abc'"));
 		Assert.assertEquals(false, TinyParser.instance.doParse(null, null, "null >6"));
 		Assert.assertEquals(false, TinyParser.instance.doParse(null, null, "6 >=null"));
		
		Assert.assertEquals(true, TinyParser.instance.doParse(null, null, "null is null"));
		User user = new User();
		user.setUserName("Tom");
		Assert.assertEquals(false, TinyParser.instance.doParse(user, null, "not(id is null)"));

		Map<String, Object> preset = new HashMap<String, Object>();
		preset.put("FOO", null);
		Assert.assertEquals(true, TinyParser.instance.doParse(null, preset, "FOO is NULL"));
	}

	@Test
	public void testParams() {
		User user = new User();
		user.setId("001");
		user.setUserName("Tom");
		Assert.assertEquals(true,
				TinyParser.instance.doParse(user, null, "userName equals ? and id equals ?", "Tom", "001"));
		Assert.assertEquals(false,
				TinyParser.instance.doParse(user, null, "userName equals ? and id equals ?", "Tom", "002"));
	}

	@Test
	public void testPresetValues() {
		Map<String, Object> preset = new HashMap<String, Object>();
		preset.put("FOO", 123);
		preset.put("BAR", "456");
		Assert.assertEquals(true, TinyParser.instance.doParse(null, preset, "FOO = 123 and BAR='456'"));
		Assert.assertEquals(true, TinyParser.instance.doParse(null, preset, "foo = 123 and bar equals '456'"));
	}

	@Test
	public void testSpeed() {
		User user = new User();
		user.setId("001");
		user.setUserName("Tom");
		Map<String, Object> preset = new HashMap<String, Object>();
		preset.put("FOO", 123);
		preset.put("BAR", "456");
		long start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++)
			Assert.assertEquals(true, TinyParser.instance.doParse(user, preset,
					"(1+2)*3/4>0.1/(9+?) and (userName equals ?) and id equals ? and (200 = ? ) and (FOO = 123 or BAR equals '456')",
					100, "Tom", "001", 200));
		Double secondsUsed = (System.currentTimeMillis() - start) / 1000.0;
		System.out.println("TinyParser repeat 10000 times cost " + secondsUsed + "s");
		Assert.assertTrue(secondsUsed < 5);
	}

	 
}
