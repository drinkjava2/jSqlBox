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
package com.jsqlboxdemo.controller;

import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jwebbox.WebBox;
import com.jsqlboxdemo.service.TeamService;

/**
 * This is a Demo to show use jWebBox as controller, WebBox is not thread safe,
 * to use WebBox act as controller, always use a IOC/AOP tool to get a new
 * instance or new proxy instance (for transaction) for each thread
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class BaseBox extends WebBox {

	TeamService teamService = JBEANBOX.getBean(TeamService.class);// singleton

	public void redirect(Object target) {
		if (target instanceof String) {
			String targetPage = (String) target;
			if (targetPage.indexOf("/WEB-INF/") == 0)
				setPage(targetPage);
			else
				setPage("/WEB-INF/pages/" + targetPage);
		} else if (target instanceof Class)
			setPage(JBEANBOX.getPrototypeBean((Class) target));// non-singleton
	}

	public Integer getObjectAsInt(String key) {
		return Integer.parseInt(getObject(key));
	}

	public String[] getPathParams() {
		return this.getObject("pathParams");
	}

	public Integer getPathParamAsInt(int index) {
		return Integer.parseInt(getPathParams()[index]);
	}
}
