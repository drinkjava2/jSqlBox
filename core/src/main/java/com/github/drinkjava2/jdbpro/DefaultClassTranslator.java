/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jdbpro;

/**
 * ClassItemTranslator translator a class item into real SqlItem. for example:
 * pExecute(SqlText.class, "2", "Bar"); if SqlText is a Text type class, the
 * DefaultClassItemTranslator will translate it to a String
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */
public class DefaultClassTranslator implements ClassTranslator {
	public static final DefaultClassTranslator instance = new DefaultClassTranslator();

	@Override
	public boolean translate(boolean inlineStyle, PreparedSQL predSQL, Class<?> classItem) {
		if (Text.class.isAssignableFrom(classItem)) {
			String text = Text.classToString(classItem);
			predSQL.addSqlOrParam(inlineStyle, text);
			return true;
		} else
			return false;
	}
}