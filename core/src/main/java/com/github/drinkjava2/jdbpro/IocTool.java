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
 * In DbPro or it's subClasses, if want us SqlMapper style and used handler
 * classes in annotation but it need inject constructor parameters, A IocTool is
 * needed, otherwise no need care about this property
 * 
 * 
 * IocTool should have a method getBean(Class<?> configClass) to create a bean
 * instance from configClass class, note: result object type may different to
 * configClass itself, that's why here called "configClass"
 * 
 * An example can see testGuessAnnotationHasParam() unit test in
 * HandlersTest.java
 * 
 * 
 * @author Yong Zhu
 * @since 1.0.8
 */
public interface IocTool {
	public <T> T getBean(Class<?> configClass);
}