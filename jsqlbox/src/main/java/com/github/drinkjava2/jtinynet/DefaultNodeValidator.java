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
package com.github.drinkjava2.jtinynet;

import java.util.HashMap;
import java.util.Map;

import com.github.drinkjava2.jtinynet.parser.TinyParser;

/**
 * DefaultNodeValidator is the default implementation of NodeValidator to
 * validate if a entity can be selected
 *
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class DefaultNodeValidator implements NodeValidator {
	public static final NodeValidator instance = new DefaultNodeValidator();

	/**
	 * Validate if an entity can be selected, default always return true, subClass
	 * can override this method to do more judge
	 */
	public boolean validateBean(Object entity) {// NOSONAR
		return true;
	}

	/**
	 * Use default TinyParser to check the expression if return true, if true then
	 * means an entity can be selected, subClass can override this method to user
	 * other expression parser
	 * 
	 * @param entity The entity to be checked
	 * @param presetValues The preset values
	 * @param expression The expression String
	 * @param expressionParams The expression parameters
	 * @return True or false depends expression result
	 */
	public boolean validateExpression(Object entity, Map<String, Object> presetValues, String expression,
			Object... expressionParams) {
		Boolean result = (Boolean) TinyParser.instance.doParse(entity, presetValues, expression, expressionParams);
		return result != null ? result : true;
	}

	@Override
	public boolean validateNode(Node node, int selectLevel, int selectSize, Path path) {
		Map<String, Object> presetValues = new HashMap<String, Object>();
		presetValues.put("SELECTLEVEL", selectLevel);
		presetValues.put("SELECTSIZE", selectSize);
		return validateBean(node.getEntity())
				&& validateExpression(node.getEntity(), presetValues, path.getExpression(), path.getexpressionParams());
	}

}
