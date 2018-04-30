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
 * IocTool have a method getBean(Class<?> configClass) to create a bean instance
 * from configClass class, note: result object type may different to configClass
 * itself, that's why here called "configClass", an example can see
 * testGuessAnnotationHasParam() unit test in HandlersTest.java
 * 
 * IocTool here is not used for transaction control, but usually can share use
 * the same transaction control IOC tool.
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
public interface IocTool {

	/**
	 * Create a bean instance from configClass class, note: result object type may
	 * different to configClass itself, that's why here called "configClass", an
	 * example can see testGuessAnnotationHasParam() unit test in HandlersTest.java
	 * 
	 * @param configClass
	 *            The configuration class
	 * @return The bean created from configuration class
	 */
	public <T> T getBean(Class<?> configClass);
}