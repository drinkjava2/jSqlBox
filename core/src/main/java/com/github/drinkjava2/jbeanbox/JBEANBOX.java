/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jbeanbox;

/**
 * JBEANBOX use default global BeanBoxContext, and have public static method to
 * access it, JBEANBOX is not a key class in project
 * 
 * @author Yong Zhu
 * @since 2.4
 *
 */
public class JBEANBOX {// NOSONAR

	public static BeanBoxContext ctx() {
		return BeanBoxContext.globalBeanBoxContext;
	}

	public static void scanComponents(String... packages) {
		BeanBoxContext.globalBeanBoxContext.scanComponents(packages);
	}

	public static void close() {
		BeanBoxContext.globalBeanBoxContext.reset();
	}
	
	public static void reset() {
		BeanBoxContext.globalBeanBoxContext.reset();
	}

	public static Object getObject(Object target) {
		return BeanBoxContext.globalBeanBoxContext.getObject(target);
	}

	public static <T> T getBean(Object target) {
		return BeanBoxContext.globalBeanBoxContext.getBean(target);
	}

	/** Use default global BeanBoxContext to create a prototype bean */
	public static <T> T getPrototypeBean(Class<?> beanClass) {
		return BeanBoxContext.globalBeanBoxContext.getPrototypeBeanBox(beanClass).getBean();
	}

	/** Use default global BeanBoxContext to create a singleton bean */
	public static <T> T getSingleBean(Class<?> beanClass) {
		return BeanBoxContext.globalBeanBoxContext.getSingletonBeanBox(beanClass).getBean();
	}

	public static <T> T getBean(Object target, boolean required) {
		return BeanBoxContext.globalBeanBoxContext.getBean(target, required);
	}

	public static <T> T getInstance(Class<T> clazz) {
		return BeanBoxContext.globalBeanBoxContext.getInstance(clazz);
	}

	public static <T> T getInstance(Class<T> clazz, boolean required) {
		return BeanBoxContext.globalBeanBoxContext.getInstance(clazz, required);
	}

	public static BeanBoxContext bind(Object shortcut, Object target) {
		return BeanBoxContext.globalBeanBoxContext.bind(shortcut, target);
	}

	public static BeanBox getBeanBox(Class<?> clazz) {
		return BeanBoxContext.globalBeanBoxContext.getBeanBox(clazz);
	}

	/** Equal to "@INJECT" annotation */
	public static BeanBox autowired() {
		return new BeanBox().setTarget(EMPTY.class);
	}

	/** Equal to "@INJECT" annotation */
	public static BeanBox inject() {
		return new BeanBox().setTarget(EMPTY.class);
	}

	/** Equal to "@INJECT" annotation */
	public static BeanBox inject(Object target) {
		return new BeanBox().setTarget(target);
	}

	/** Equal to "@VALUE" annotation */
	public static BeanBox value(Object value) {
		return new BeanBox().setTarget(value).setPureValue(true).setRequired(true);
	}

}
