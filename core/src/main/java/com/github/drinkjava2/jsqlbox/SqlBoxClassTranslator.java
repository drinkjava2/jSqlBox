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
package com.github.drinkjava2.jsqlbox;

import com.github.drinkjava2.jdbpro.DefaultClassTranslator;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdialects.TableModelUtils;

/**
 * SqlBoxClassTranslator translator a class item into real SqlItem. for example:
 * pExecute(someClass.class, "2", "Bar"); if someClass is a Text type class, the
 * SqlBoxClassTranslator will translate it to a String, if someClass is a
 * TableModel type class, will translate it to a TableModel instance
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */
public class SqlBoxClassTranslator extends DefaultClassTranslator {
	public static final SqlBoxClassTranslator instance = new SqlBoxClassTranslator();

	@Override
	public boolean translate(boolean inlineStyle, PreparedSQL predSQL, Class<?> clazz) {
		if (super.translate(inlineStyle, predSQL, clazz))
			return true;
		predSQL.addModel(TableModelUtils.entity2ReadOnlyModel(clazz));
		SqlBoxContextUtils.createLastAutoAliasName(predSQL);
		return true;
	}
}