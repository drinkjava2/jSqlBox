/**
 * Copyright 2016 the original author or authors.
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

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.TextUtils;

import activerecordtext.AbstractUser;

/**
 * TextUtils is base class for Java text support (multiple line Strings).
 * 
 * @author Yong Zhu 
 */
public class TextUtilsTest {
	@Test
	public void doTextTest() {
		String str = TextUtils.getJavaSourceCodeUTF8(AbstractUser.class);
		Assert.assertTrue(str.indexOf("AbstractUser extends TextedUser") > -1);
	}
}