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

import com.github.drinkjava2.jtinynet.parser.SimpleExpressionParser;

/**
 * BeanValidator is the default implementation of BeanValidatorSupport, it
 * always allow bean validated, it has a simple
 *
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class BeanValidator implements BeanValidatorSupport {
	public static final BeanValidatorSupport instance = new BeanValidator();

	@Override
	public boolean validateBean(Object entity) {
		return true;
	}

	@Override
	public boolean validateNode(Node node, int level, int selectedSize, Path path) {
		return validateBean(node.getEntity());
	}

	@Override
	public boolean validateExpression(Node node, int level, int selectedSize, Path path) {
		return SimpleExpressionParser.parse(node.getEntity(), level, selectedSize, path.getExpression(),
				path.getexpressionParams());
	}

}
