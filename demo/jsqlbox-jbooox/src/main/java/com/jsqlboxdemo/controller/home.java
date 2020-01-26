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

/**
 * This is a Demo to show use jWebBox as controller, WebBox is not thread safe,
 * to use WebBox act as controller, always use a IOC/AOP tool to get a new
 * instance or new proxy instance (for transaction) for each thread
 * 
 * @author Yong Zhu
 */
@SuppressWarnings("all")
public class home {

	public static class home_default extends BaseBox {
		{
			setPage("/WEB-INF/pages/home.jsp");
		}

	}

}
