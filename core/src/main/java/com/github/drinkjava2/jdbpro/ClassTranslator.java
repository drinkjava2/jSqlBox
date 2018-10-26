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
 * ClassTranslator translator a class item into real SqlItem. for example:
 * pExecute(SqlText.class, "2", "Bar"); if SqlText is a Text type class, the
 * DefaultClassTranslator will translate it to a String
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */
public interface ClassTranslator {

	/**
	 * Translate a class item into real SqlItem. for example:
	 * pExecute(SqlText.class, "2", "Bar"); if SqlText is a Text type class, the
	 * DefaultClassItemTranslator will translate it to a String
	 * 
	 * @param inlineStyle
	 *            if is inside of a in-line style call?
	 * @param predSQL
	 *            the PreparedSQL instance
	 * @param clazz
	 *            the class item
	 * @return if can not translate by current implementation, return false,
	 *         otherwise return true
	 */
	public boolean translate(boolean inlineStyle, PreparedSQL predSQL, Class<?> clazz);

}