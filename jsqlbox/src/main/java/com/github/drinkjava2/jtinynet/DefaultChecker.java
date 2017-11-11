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

import java.util.Collection;

/**
 * DefaultChecker always allow a node to put into input list or output list
 *
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class DefaultChecker extends Checker {
	public static final Checker instance = new DefaultChecker();

	@Override
	public boolean check(TinyNet tinyNet, Node node, int level, Collection<Node> inputList,
			Collection<Node> outputList) {
		if (level > 500)
			throw new TinyNetException("Search level beyond 500, this may caused by a circular reference path chain.");
		if (inputList != null && inputList.size() > 100000)
			throw new TinyNetException("Input list size >100000, this may often caused by careless programming.");
		if (outputList != null && outputList.size() > 100000)
			throw new TinyNetException("Output list size >100000, this may often caused by careless programming.");
		return true;
	}

}
