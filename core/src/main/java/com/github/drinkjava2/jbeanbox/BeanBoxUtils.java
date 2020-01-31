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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * BeanBoxUtils store public static methods used only for this project
 * 
 * @author Yong Zhu
 * @since 2.4.7
 */
public class BeanBoxUtils {// NOSONAR

	public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes) {// NOSONAR
		try {
			return clazz.getConstructor(paramTypes);
		} catch (SecurityException e) {
			throw new IllegalStateException("Security exception found for Constructor: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Constructor not found: " + e.getMessage());
		}
	}

	/** Copy target, pureValue, required, qualifier setting from from to to */
	public static void copyBoxValues(BeanBox from, BeanBox to) {
		to.setTarget(from.target);
		to.setPureValue(from.pureValue);
		to.setRequired(from.required);
		to.setQualifierAnno(from.qualifierAnno);
		to.setQualifierValue(from.qualifierValue);
		to.setType(from.type);
	}

	/** give a class or Field or Method, return annotations */
	public static Annotation[] getAnnotations(Object targetClass) {
		if (targetClass instanceof Field)
			return ((Field) targetClass).getAnnotations();
		else if (targetClass instanceof Method)
			return ((Method) targetClass).getAnnotations();
		else if (targetClass instanceof Constructor)
			return ((Constructor<?>) targetClass).getAnnotations();
		else if (targetClass instanceof Class)
			return ((Class<?>) targetClass).getAnnotations();
		else
			return BeanBoxException.throwEX("targetClass should be Field, Method, Constructor or Class");
	}

	/** Return all annotations for Class or Field */
	public static Map<String, Object> getAnnoAsMap(Object targetClass, String annoFullName) {
		Annotation[] anno = getAnnotations(targetClass);
		for (Annotation a : anno) {
			Class<? extends Annotation> type = a.annotationType();
			if (annoFullName.equals(type.getName()))
				return changeAnnotationValuesToMap(a);
		}
		return null;
	}

	public static boolean ifSameOrChildAnno(Class<? extends Annotation> annoType,
			@SuppressWarnings("unchecked") Class<? extends Annotation>... annoTypes) {
		for (Class<? extends Annotation> a : annoTypes)
			if (annoType.equals(a) || annoType.isAnnotationPresent(a))
				return true;
		return false;
	}

	/** Check if annotation exist in Class or Field */
	public static boolean checkAnnoExist(Object targetClass, Class<?> annoClass) {
		Annotation[] anno = getAnnotations(targetClass);
		for (Annotation annotation : anno) {
			Class<? extends Annotation> type = annotation.annotationType();
			if (annoClass.equals(type))
				return true;
		}
		return false;
	}

	/** This used for unknown Annotation, change values to a Map */
	public static Map<String, Object> changeAnnotationValuesToMap(Annotation annotation) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (Method method : annotation.annotationType().getDeclaredMethods())
			try {
				result.put(method.getName(), method.invoke(annotation, (Object[]) null));
			} catch (Exception e) {// NOSONAR
			}
		return result;
	}

	/**
	 * If aop is a instance of Aop alliance Interceptor, wrap it to a BeanBox and
	 * set as purevalue, otherwise direct return it (class or BeanBox)
	 */
	public static Object checkAOP(Object aop) {
		if (aop instanceof MethodInterceptor)
			return new BeanBox().setTarget(aop).setPureValue(true);
		else
			return aop;
	}

	/**
	 * If param is class, wrap it to BeanBox, if param is BeanBox instance, direct
	 * return it, otherwise wrap it as pure Value BeanBox
	 */
	public static BeanBox wrapParamToBox(Object param) {
		if (param != null) {
			if (param instanceof Class)
				return new BeanBox().setTarget(param);
			if (param instanceof BeanBox)
				return (BeanBox) param;
		}
		return new BeanBox().setAsValue(param);
	}

}
