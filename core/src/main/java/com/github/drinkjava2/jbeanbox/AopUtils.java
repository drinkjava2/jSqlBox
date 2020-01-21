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

import com.github.drinkjava2.cglib.proxy.Enhancer;

/**
 * AopUtils create AOP proxy bean
 * 
 * @author Yong Zhu
 * @since 2.4
 *
 */
public class AopUtils {// NOSONAR

	/**
	 * Create a ProxyBean
	 * 
	 * @param clazz
	 *            The target class
	 * @param box
	 *            The BeanBox of target class
	 * @param ctx
	 *            The BeanBoxContext
	 * @return A Proxy Bean with AOP support
	 */
	public static Object createProxyBean(Class<?> clazz, BeanBox box, BeanBoxContext ctx) {
		BeanBoxException.assureNotNull(clazz, "Try to create a proxy bean, but beanClass not found.");
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(clazz);
		if (box.getConstructorParams() != null && box.getConstructorParams().length > 0) {
			BeanBox[] boxes = box.getConstructorParams();
			Class<?>[] argsTypes = new Class<?>[boxes.length];
			Object[] realArgsValue = new Object[boxes.length];
			for (int i = 0; i < boxes.length; i++) {
				argsTypes[i] = boxes[i].getType();
				Object realValue = ctx.getBean(boxes[i]);
				if (realValue != null && realValue instanceof String)
					realValue = ctx.getValueTranslator().translate((String) realValue, boxes[i].getType());
				realArgsValue[i] = realValue;
			}
			enhancer.setCallback(new ProxyBean(box, ctx));
			return enhancer.create(argsTypes, realArgsValue);
		} else {
			enhancer.setCallback(new ProxyBean(box, ctx));
			return enhancer.create();
		}
	}

}
