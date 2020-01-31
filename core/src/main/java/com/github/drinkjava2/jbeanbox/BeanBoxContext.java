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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.drinkjava2.jbeanbox.ValueTranslator.DefaultValueTranslator;
import com.github.drinkjava2.jbeanbox.annotation.AOP;
import com.github.drinkjava2.jbeanbox.annotation.COMPONENT;
import com.github.drinkjava2.jbeanbox.annotation.INJECT;
import com.github.drinkjava2.jbeanbox.annotation.NAMED;
import com.github.drinkjava2.jbeanbox.annotation.POSTCONSTRUCT;
import com.github.drinkjava2.jbeanbox.annotation.PREDESTROY;
import com.github.drinkjava2.jbeanbox.annotation.PROTOTYPE;
import com.github.drinkjava2.jbeanbox.annotation.QUALIFILER;
import com.github.drinkjava2.jbeanbox.annotation.VALUE;

/**
 * BeanBoxContext is the Context (i.e. BeanFactory) to create beans
 * 
 * @author Yong Zhu
 * @since 2.4
 *
 */
public class BeanBoxContext {
	protected boolean allowAnnotation = true;
	protected boolean allowSpringJsrAnnotation = true;
	protected ValueTranslator valueTranslator = new DefaultValueTranslator();

	protected Map<Object, Object> bindCache = new ConcurrentHashMap<Object, Object>();// bind cache
	protected Map<Class<?>, BeanBox> beanBoxCache = new ConcurrentHashMap<Class<?>, BeanBox>(); // default BeanBox cache
	protected Map<Object, Object> singletonCache = new ConcurrentHashMap<Object, Object>(); // class or BeanBox as key
	protected Set<Class<?>> componentCache = new HashSet<Class<?>>(); // component cache
	protected Map<String, BeanBox> componentSearchCache = new ConcurrentHashMap<String, BeanBox>();// as title

	protected static BeanBoxContext globalBeanBoxContext = new BeanBoxContext();// Global Bean context

	// ==========AOP about=========
	protected List<Object[]> aopRules; // Store aop string match rules

	private static final BeanBox NO_THIS_COMPONENT = new BeanBox();// Mark a no-exist component

	public BeanBoxContext() {
		bindBasicTypes();
	}

	/** Reset context to initial status, clear binding & caches, */
	public void reset() {
		for (Entry<Object, Object> singletons : singletonCache.entrySet()) {
			Object key = singletons.getKey();
			Object obj = singletons.getValue();
			if (key instanceof BeanBox) {
				BeanBox box = (BeanBox) key;
				if (box.getPreDestroy() != null)
					try {
						box.getPreDestroy().invoke(obj);
					} catch (Exception e) {
						System.err.println(e.getMessage());// NOSONAR
					}
			}
		}
		bindCache.clear();
		beanBoxCache.clear();
		singletonCache.clear();
		componentCache.clear();
		componentSearchCache.clear();
		allowAnnotation = true;
		allowSpringJsrAnnotation = true;
		valueTranslator = new DefaultValueTranslator();
		bindBasicTypes();
	}

	public Object getObject(Object target) {
		return getBean(target, true, null);
	}

	public <T> T getBean(Object target) {
		return getBean(target, true, null);// first step of changzheng
	}

	public <T> T getInstance(Class<T> target) {
		return getBean(target, true, null);
	}

	public <T> T getBean(Object target, boolean required) {
		return getBean(target, required, null);
	}

	public <T> T getInstance(Class<T> target, boolean required) {
		return getBean(target, required, null);
	}

	/** Get a class BeanBox which sington property is ture */
	public BeanBox getSingletonBeanBox(Class<?> clazz) {
		return getBeanBox(clazz, true);
	}

	/** Get a class BeanBox which sington property is false */
	public BeanBox getPrototypeBeanBox(Class<?> clazz) {
		return getBeanBox(clazz, false);
	}

	/** Get a class BeanBox which sington property determined by annotation */
	public BeanBox getBeanBox(Class<?> clazz) {
		return getBeanBox(clazz, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T getBean(Object target, boolean required, Set<Object> history) {// NOSONAR
		if (target != null && singletonCache.containsKey(target))
			return (T) singletonCache.get(target);

		if (target == null || EMPTY.class == target)
			return (T) notfoundOrException(target, required, null);

		if (history != null && target instanceof BeanBox && history.contains(target))
			BeanBoxException.throwEX("Circular dependency found on :" + target);

		Object result = null;
		if (history == null)
			history = new HashSet<Object>();// NOSONAR
		history.add(target);
		if (bindCache.containsKey(target)) {
			result = getBean(bindCache.get(target), required, history);
		} else if (target instanceof BeanBox) { // is a BeanBox instance?
			result = getBeanFromBox((BeanBox) target, required, history);
		} else if (target instanceof Class) { // is a class?
			BeanBox box = getBeanBox((Class<?>) target);
			BeanBox bx = searchComponent(box);
			if (bx != null)
				result = getBean(bx, required, history);
			else
				result = getBean(box, required, history);
			if (EMPTY.class != result && box.isSingleton()) {
				singletonCache.put(target, result);
			}
		} else
			result = notfoundOrException(target, required, null);
		history.remove(target);
		return (T) result;
	}

	/**
	 * Scan classes with &#064;COMPONENT or &#064;Component annotation, for
	 * autowiring purpose
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void scanComponents(String... packages) {
		List<Class> classes = ClassScanner.scanPackages(packages);
		for (Class claz : classes)
			for (Annotation anno : claz.getAnnotations()) {
				Class<? extends Annotation> aType = anno.annotationType();
				if (BeanBoxUtils.ifSameOrChildAnno(aType, COMPONENT.class)
						|| (allowSpringJsrAnnotation && BeanBoxUtils.ifSameOrChildAnno(aType, Component.class))) {
					componentCache.add(claz);// add class as component
					BeanBox box = getBeanBox(claz);
					Map<String, Object> values = BeanBoxUtils.changeAnnotationValuesToMap(anno);
					if (!"".equals(values.get("value")))// use given bean name
						this.bind(values.get("value"), box);
					else {
						String s = claz.getSimpleName(); // else use first char lower case class name as bean name
						bind(s, box); // use class name as bind key
						if (!Character.isLowerCase(s.charAt(0))) {
							s = (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1))
									.toString();
							bind(s, box); // also bind the key start wtih lower case
						}
					}
				}
			}
	}

	/** Bind a targe on a bean id, if id already exist, override it */
	public BeanBoxContext bind(Object id, Object target) {
		BeanBoxException.assureNotNull(id, "bind id can not be empty");
		bindCache.put(id, target);
		return this;
	}

	public BeanBoxContext addContextAop(Object aop, String classNameRegex, String methodNameRegex) {
		if (aopRules == null)
			aopRules = new ArrayList<Object[]>();
		aopRules.add(new Object[] { BeanBoxUtils.checkAOP(aop), classNameRegex, methodNameRegex });
		return this;
	}

	public BeanBoxContext addContextAop(Object aop, Class<?> clazz, String methodNameRegex) {
		return addContextAop(aop, clazz.getName() + "*", methodNameRegex);
	}

	protected void internalUtilMethods____________() {// nosonar
	}

	/** Read Bean annotations to build a BeanBox instance */
	private BeanBox doCreateBeanBox(Class<?> clazz) {// NOSONAR
		BeanBox box = new BeanBox();
		box.setBeanClass(clazz);
		box.setSingleton(true); // for static class default set to singleton

		if (!this.isAllowAnnotation())
			return box;

		// ========= singleton or prototype
		if (BeanBoxUtils.checkAnnoExist(clazz, PROTOTYPE.class))
			box.setSingleton(false);
		else if (allowSpringJsrAnnotation) {
			Map<String, Object> m = BeanBoxUtils.getAnnoAsMap(clazz, "org.springframework.context.annotation.Scope");
			if (m != null)
				for (Entry<String, Object> entry : m.entrySet())
					if ("value".equals(entry.getKey())) {// NOSONAR
						if ("prototype".equalsIgnoreCase(String.valueOf(entry.getValue())))
							box.setSingleton(false);
						else if ("singleton".equalsIgnoreCase(String.valueOf(entry.getValue())))
							box.setSingleton(true);
						else
							BeanBoxException.throwEX("'prototype' or 'singleton' required in @Scope annotation");
					}
		}

		// ======== Class inject, if @INJECT, @Qualifiler put on class
		BeanBox v = getInjectBoxFromAnno(clazz);
		if (v != null)
			BeanBoxUtils.copyBoxValues(v, box);

		// ======== AOP annotated annotations on class
		Annotation[] annos = clazz.getAnnotations();
		for (Annotation anno : annos) {
			if (anno.annotationType().isAnnotationPresent(AOP.class)) {
				Map<String, Object> annoMap = BeanBoxUtils.changeAnnotationValuesToMap(anno);
				Object aop = annoMap.get("value");
				String methodNameRule = (String) annoMap.get("method");// name must be method
				if (methodNameRule != null && methodNameRule.length() > 0 && aop != null)
					box.addBeanAop(aop, methodNameRule);
			}
		}

		// ========== Constructor inject
		Constructor<?>[] constrs = clazz.getConstructors();
		for (Constructor<?> constr : constrs) {
			v = getInjectBoxFromAnno(constr);
			if (v != null) { // has constr inject
				box.setBeanClass(clazz);// anyway set beanClass first
				BeanBox[] paramInjects = getParameterInjectAsBeanBoxArray(constr);
				box.setConstructor(constr);
				box.setConstructorParams(paramInjects);
				if (paramInjects.length == 1) {
					if (v.target != null && v.target != EMPTY.class)
						paramInjects[0].setTarget(v.target);
					if (v.pureValue)
						paramInjects[0].setPureValue(true);
					if (v.qualifierAnno != null) {
						paramInjects[0].setQualifierAnno(v.qualifierAnno);
						paramInjects[0].setQualifierValue(v.qualifierValue);
					}
				}
			}
		}

		// =================Field inject=================
		// @INJECT annotations on fields include super class's
		for (Field f : ReflectionUtils.getSelfAndSuperClassFields(clazz)) {
			BeanBox inject = getInjectBoxFromAnno(f);
			if (inject != null) {
				box.checkOrCreateFieldInjects();
				inject.setType(f.getType());
				ReflectionUtils.makeAccessible(f);
				box.getFieldInjects().put(f, inject);
			}
		}

		// @INJECT annotations on methods include super class's
		Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
		for (Method m : methods) {
			// ========== @PostConstruct and @PreDestory
			if (m.getAnnotation(POSTCONSTRUCT.class) != null || m.getAnnotation(PostConstruct.class) != null) {
				if (m.getParameterTypes().length > 0)
					BeanBoxException.throwEX("In jBeanBox, PostConstruct should have no parameter.");
				ReflectionUtils.makeAccessible(m);
				box.setPostConstruct(m);
			}
			if (m.getAnnotation(PREDESTROY.class) != null || m.getAnnotation(PreDestroy.class) != null) {
				if (m.getParameterTypes().length > 0)
					BeanBoxException.throwEX("In jBeanBox, PostConstruct should have no parameter.");
				ReflectionUtils.makeAccessible(m);
				box.setPreDestroy(m);
			}

			// ========== AOP about annotation =========
			Annotation[] mtdAnnos = m.getAnnotations();
			for (Annotation anno : mtdAnnos)
				if (anno.annotationType().isAnnotationPresent(AOP.class)) {
					Map<String, Object> annoMap = BeanBoxUtils.changeAnnotationValuesToMap(anno);
					Object aop = annoMap.get("value");
					if (aop != null)
						box.addMethodAop(aop, m);
				}

			// =========== method inject annotation ==============
			v = getInjectBoxFromAnno(m);
			if (v != null) {
				ReflectionUtils.makeAccessible(m);
				BeanBox[] paramInjects = getParameterInjectAsBeanBoxArray(m);
				box.checkOrCreateMethodInjects();
				box.getMethodInjects().put(m, paramInjects);
				if (paramInjects.length == 1) {
					if (v.target != null && v.target != EMPTY.class)
						paramInjects[0].setTarget(v.target);
					if (v.pureValue)
						paramInjects[0].setPureValue(true);
					if (v.qualifierAnno != null) {
						paramInjects[0].setQualifierAnno(v.qualifierAnno);
						paramInjects[0].setQualifierValue(v.qualifierValue);
					}
				}
			}
		}
		return box;
	}

	private void bindBasicTypes() {
		bind(Object.class, EMPTY.class);
		bind(String.class, EMPTY.class);
		bind(Integer.class, EMPTY.class);
		bind(Boolean.class, EMPTY.class);
		bind(Byte.class, EMPTY.class);
		bind(Long.class, EMPTY.class);
		bind(Short.class, EMPTY.class);
		bind(Float.class, EMPTY.class);
		bind(Double.class, EMPTY.class);
		bind(Character.class, EMPTY.class);
		bind(List.class, EMPTY.class);
		bind(Map.class, EMPTY.class);
		bind(Set.class, EMPTY.class);

		bind(int.class, EMPTY.class);
		bind(boolean.class, EMPTY.class);
		bind(byte.class, EMPTY.class);
		bind(long.class, EMPTY.class);
		bind(short.class, EMPTY.class);
		bind(float.class, EMPTY.class);
		bind(double.class, EMPTY.class);
		bind(char.class, EMPTY.class);
	}

	/**
	 * Get BeanBox for class, prototype can be null/true/false represents
	 * default/prototype/sington 3 type beanbox
	 */
	private BeanBox getBeanBox(Class<?> clazz, Boolean singleton) {
		BeanBoxException.assureNotNull(clazz, "Target class can not be null");
		BeanBox box = this.beanBoxCache.get(clazz);
		if (box != null) {
			if (singleton == null)
				return box;
			if (singleton && box.isSingleton())
				return box;
			return box.newCopy().setSingleton(singleton);
		}
		if (BeanBox.class.isAssignableFrom(clazz)) // not found beanbox
			try {
				box = (BeanBox) clazz.newInstance();
				if (box.singleton == null)
					box.singleton = true;
			} catch (Exception e) {
				BeanBoxException.throwEX(e);
			}
		else
			box = this.doCreateBeanBox(clazz);
		if (box.beanClass != null && PrototypeBean.class.isAssignableFrom(box.beanClass))// NOSONAR
			box.setSingleton(false);
		this.beanBoxCache.put(clazz, box);
		return box;
	}

	/** Get Bean From BeanBox instance */
	private Object getBeanFromBox(BeanBox box, boolean required, Set<Object> history) {// NOSONAR
		BeanBoxException.assureNotNull(box, "Fail to build instance for a null beanBox");
		Object bean = null;
		if (box.isSingleton()) { // Check if singleton in cache
			bean = singletonCache.get(box);
			if (bean != null)
				return bean;
		}

		if (box.isPureValue()) // if constant?
			return box.getTarget();
		if (box.getTarget() != null) {// if target?
			if (EMPTY.class != box.getTarget())//
				return getBean(box.getTarget(), box.required, history);
			if (box.type != null) { // now is EMPTY, it means it's a @INJECT parameter
				BeanBox bx = searchComponent(box);
				if (bx == null && box.qualifierAnno != null)
					return notfoundOrException(box.type, box.required, box);
				if (bx != null)
					return getBean(bx, box.required, history);
				else
					return getBean(box.type, box.required, history);
			} else
				return notfoundOrException(box.getTarget(), box.required, box);
		}

		boolean aopFound = false;// is AOP?
		if (box.getAopRules() != null || box.getMethodAops() != null)
			aopFound = true;
		else if (this.getAopRules() != null && box.getBeanClass() != null)
			for (Object[] aops : this.getAopRules()) // global AOP
				if (NameMatchUtil.nameMatch((String) aops[1], box.getBeanClass().getName())) {
					aopFound = true;
					break;
				}
		if (aopFound)
			bean = AopUtils.createProxyBean(box.getBeanClass(), box, this);
		else {
			bean = box.create(); // use BeanBox's create methods to create bean
			if (bean == null)
				bean = box.create(this);
			if (bean == null)
				bean = box.create(this, history);
		}
		if (bean == null)
			if (box.getConstructor() != null) { // has constructor?
				if (box.getConstructorParams() != null && box.getConstructorParams().length > 0) {
					Object[] initargs = param2RealObjects(box.getConstructorParams(), history);
					try {
						bean = box.getConstructor().newInstance(initargs);
					} catch (Exception e) {
						return BeanBoxException.throwEX(e);
					}
				} else // 0 param constructor
					try {
						bean = box.getConstructor().newInstance();
					} catch (Exception e) {
						return BeanBoxException.throwEX(e);
					}
			} else if (box.getBeanClass() != null) { // is normal bean
				if (EMPTY.class == box.getBeanClass() || box.getBeanClass().isInterface())
					return notfoundOrException(box.getBeanClass(), required, box);
				try {
					bean = box.getBeanClass().newInstance();
				} catch (Exception e) {
					return notfoundOrException(box.getBeanClass(), required, box);
				}
			} else
				return notfoundOrException(null, box.required, box); // return null or throw EX

		// Now Bean is ready

		// Cache bean or proxy bean right now for circular dependency use
		if (box.isSingleton()) {
			Object id = box.getSingletonId();
			if (id != null)
				singletonCache.put(box, bean);
		} // NOW BEAN IS CREATED

		box.config(bean);// call config methods maybe overrided by user
		box.config(bean, this);
		box.config(bean, this, history);

		if (box.getPostConstruct() != null) // PostConstructor
			ReflectionUtils.invokeMethod(box.getPostConstruct(), bean);

		if (box.getFieldInjects() != null) // Fields inject
			for (Entry<Field, BeanBox> entry : box.getFieldInjects().entrySet()) {
				Field f = entry.getKey();
				BeanBox b = entry.getValue();
				Object fieldValue = this.getBeanFromBox(b, b.required, history);
				if (fieldValue != null && EMPTY.class != fieldValue) {
					if (fieldValue instanceof String)
						fieldValue = this.valueTranslator.translate((String) fieldValue, b.type);
					ReflectionUtils.setField(f, bean, fieldValue);
				}
			}

		if (box.getMethodInjects() != null) { // Methods inject
			for (Entry<Method, BeanBox[]> methods : box.getMethodInjects().entrySet()) {
				Method m = methods.getKey();
				BeanBox[] paramBoxs = methods.getValue();
				if (paramBoxs != null && paramBoxs.length > 0) {
					Object[] methodParams = param2RealObjects(paramBoxs, history);
					ReflectionUtils.invokeMethod(m, bean, methodParams);
				} else // method has no parameter
					ReflectionUtils.invokeMethod(m, bean);
			}
		}
		return bean;
	}

	private Object[] param2RealObjects(BeanBox[] boxes, Set<Object> history) {
		Object[] result = new Object[boxes.length];
		for (int i = 0; i < boxes.length; i++) {
			result[i] = getBeanFromBox(boxes[i], boxes[i].required, history);
			if (result[i] instanceof String)
				result[i] = valueTranslator.translate((String) result[i], boxes[i].type);
		}
		return result;
	}

	/** Check if class is a component and return its BeanBox */
	private BeanBox searchComponent(BeanBox box) {
		if (box.type == null || componentCache.isEmpty())
			return null;
		String key = new StringBuilder().append(box.type.getName()).append(":").append(box.qualifierAnno).append(":")
				.append(box.qualifierValue).toString();
		BeanBox result = componentSearchCache.get(key);
		if (result == NO_THIS_COMPONENT) // already know not found
			return null;
		if (result != null) // already in cache?
			return result;
		if (box.qualifierAnno != null && box.qualifierAnno.getSimpleName().equalsIgnoreCase("named"))
			result = (BeanBox) bindCache.get(box.getQualifierValue());
		if (result == null)
			for (Class<?> compClass : componentCache)
				if (box.type.isAssignableFrom(compClass)) {
					BeanBox compBox = getBeanBox(compClass);
					if (box.qualifierAnno == null || (box.qualifierAnno == compBox.qualifierAnno
							&& ((box.qualifierValue == null) || (box.qualifierValue.equals(compBox.qualifierValue))))) {
						if (result != null)
							BeanBoxException.throwEX("2 components " + compClass.getName() + " and "
									+ result.beanClass.getName() + " found for type: " + box.type);
						result = compBox;
					}
				}
		if (result != null) {
			componentSearchCache.put(key, result);
			return result;
		} else
			componentSearchCache.put(key, NO_THIS_COMPONENT);
		return null;
	}

	/** Get wanted Inject info from target annotation */
	private BeanBox getInjectBoxFromAnno(Object target) {
		Annotation[] anno = BeanBoxUtils.getAnnotations(target);
		return getInjectBoxFromAnnos(anno);
	}

	/** Get a BeanBox instance from annotation array */
	@SuppressWarnings("unchecked")
	private BeanBox getInjectBoxFromAnnos(Annotation[] anno) {// NOSONAR
		BeanBox box = null;
		for (Annotation a : anno) {
			Class<? extends Annotation> type = a.annotationType();
			if (INJECT.class.equals(type)) {
				INJECT i = (INJECT) a;
				box = new BeanBox().setTarget(i.value()).setRequired(i.required()).setPureValue(i.pureValue());
			} else if (VALUE.class.equals(type))
				box = new BeanBox().setTarget(((VALUE) a).value()).setRequired(true).setPureValue(true);
			else if (allowSpringJsrAnnotation) {
				if (Inject.class.equals(type))
					box = new BeanBox().setTarget(EMPTY.class).setPureValue(false);
				else if (Autowired.class.equals(type))
					box = new BeanBox().setTarget(EMPTY.class).setRequired(((Autowired) a).required());
				else if (Value.class.equals(type))
					box = new BeanBox().setTarget(((Value) a).value()).setRequired(true).setPureValue(true);
			}
		}
		for (Annotation a : anno) {
			Class<? extends Annotation> type = a.annotationType();
			if (BeanBoxUtils.ifSameOrChildAnno(type, NAMED.class, QUALIFILER.class) // if have qualifiler anno?
					|| (allowSpringJsrAnnotation && BeanBoxUtils.ifSameOrChildAnno(type, Named.class, Qualifier.class,
							org.springframework.beans.factory.annotation.Qualifier.class))) {
				Map<String, Object> v = BeanBoxUtils.changeAnnotationValuesToMap(a);
				if (v.size() > 1)
					BeanBoxException
							.throwEX("jBeanBox does not support multiple property in Qualifier annotation: " + type);
				if (box == null)
					box = new BeanBox();
				box.setQualifierAnno(type).setQualifierValue(v.isEmpty() ? null : v.values().iterator().next());
			}
		}
		return box;
	}

	/** Get Parameter Inject as BeanBox[] Array */
	private BeanBox[] getParameterInjectAsBeanBoxArray(Object o) {
		Annotation[][] annoss = null;
		Class<?>[] paramTypes = null;
		if (o instanceof Method) {
			annoss = ((Method) o).getParameterAnnotations();
			paramTypes = ((Method) o).getParameterTypes();
		} else if (o instanceof Constructor) {
			annoss = ((Constructor<?>) o).getParameterAnnotations();
			paramTypes = ((Constructor<?>) o).getParameterTypes();
		} else
			return BeanBoxException.throwEX("Only method or Constructor are allowed for:" + o);
		BeanBox[] beanBoxes = new BeanBox[annoss.length];
		for (int i = 0; i < annoss.length; i++) {
			Annotation[] annos = annoss[i];
			BeanBox v = getInjectBoxFromAnnos(annos);
			BeanBox inject = new BeanBox();
			if (v != null) { // if parameter has annotation
				BeanBoxUtils.copyBoxValues(v, inject);
				if (inject.target == null)
					inject.target = EMPTY.class;
			} else // if parameter no annotation
				inject.setTarget(EMPTY.class);
			inject.setType(paramTypes[i]);
			beanBoxes[i] = inject;
		}
		return beanBoxes;
	}

	private static Object notfoundOrException(Object target, boolean required, BeanBox box) {
		if (required)
			return BeanBoxException
					.throwEX("Can not create instance for: " + target + (box == null ? "" : box.getDebugInfo()));
		else
			return EMPTY.class;
	}

	protected void getterAndSetters____________() {// NOSONAR
	}

	public boolean isAllowAnnotation() {
		return allowAnnotation;
	}

	public BeanBoxContext setAllowAnnotation(boolean allowAnnotation) {
		this.allowAnnotation = allowAnnotation;
		return this;
	}

	public boolean isAllowSpringJsrAnnotation() {
		return allowSpringJsrAnnotation;
	}

	public BeanBoxContext setAllowSpringJsrAnnotation(boolean allowSpringJsrAnnotation) {
		this.allowSpringJsrAnnotation = allowSpringJsrAnnotation;
		return this;
	}

	public ValueTranslator getValueTranslator() {
		return valueTranslator;
	}

	public BeanBoxContext setValueTranslator(ValueTranslator valueTranslator) {
		this.valueTranslator = valueTranslator;
		return this;
	}

	public Map<Object, Object> getBindCache() {
		return bindCache;
	}

	public BeanBoxContext setBindCache(Map<Object, Object> bindCache) {
		this.bindCache = bindCache;
		return this;
	}

	public Map<Object, Object> getSingletonCache() {
		return singletonCache;
	}

	public BeanBoxContext setSingletonCache(Map<Object, Object> singletonCache) {
		this.singletonCache = singletonCache;
		return this;
	}

	public List<Object[]> getAopRules() {
		return aopRules;
	}

	public BeanBoxContext setAopRules(List<Object[]> aopRules) {
		this.aopRules = aopRules;
		return this;
	}
}
