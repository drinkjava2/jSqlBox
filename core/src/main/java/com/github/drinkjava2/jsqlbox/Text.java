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
package com.github.drinkjava2.jsqlbox;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.drinkjava2.jdialects.StrUtils;

/**
 * Child class extended from Text support multiple line String. the toString
 * method will return the comments between / * - and - * / , To use this
 * function need copy java file in resources folder, or move java file in
 * resources folder and set a plugin in pom.xml, detail see jSqlBox wiki
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */
public abstract class Text {
	private static final Map<Class<?>, String> textCache = new ConcurrentHashMap<Class<?>, String>();

	@Override
	public String toString() {
		if (textCache.containsKey(this.getClass()))
			return textCache.get(this.getClass());
		String thisPublicStaticClassName = this.getClass().getSimpleName();
		String javaSourceCode = TextUtils.getJavaSourceCode(this.getClass(), "UTF-8");
		String classText = StrUtils.substringBetween(javaSourceCode, "public static class " + thisPublicStaticClassName,
				"*/");
		if (StrUtils.isEmpty(classText))
			throw new RuntimeException("Can not find text between \"public static class " + thisPublicStaticClassName
					+ " and end tag \"*/\"");
		String s = StrUtils.substringAfter(classText, "/*-");
		textCache.put(this.getClass(), s);
		return s;
	}
}