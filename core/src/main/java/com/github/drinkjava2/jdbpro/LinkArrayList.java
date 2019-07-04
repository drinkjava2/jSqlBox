/*
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
package com.github.drinkjava2.jdbpro;

import java.util.ArrayList;

/**
 * LinkArrayList make ArrayList support link style
 * 
 * @author Yong Zhu
 * @since 2.0.0
 */

@SuppressWarnings("serial")
public class LinkArrayList<T> extends ArrayList<T> {

	public LinkArrayList<T> frontAdd(T element) {
		super.add(0, element);
		return this;
	}

	public LinkArrayList<T> append(T element) {
		super.add(element);
		return this;
	}

	public LinkArrayList<T> insert(int index, T element) {
		super.add(index, element);
		return this;
	}

	public Object[] toObjectArray() {
		return this.toArray(new Object[this.size()]);
	}

}
