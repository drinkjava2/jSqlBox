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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BeanBox is a virtual model tell system how to build or lookup bean instance
 *
 * @author Yong Zhu
 * @since 2.4.7
 *
 */
public class BeanBox {

	// below fields for BeanBox has a target
	protected Object target; // inject can be constant, beanBox, beanBox class, class

	protected boolean pureValue = false; // if true means target is a pure value

	protected Class<?> type; // For field and parameter constant inject, need know what's the type

	protected boolean required = true;// For field and parameter, if not found throw exception

	protected Class<? extends Annotation> qualifierAnno; // the qualifier annotation class

	protected Object qualifierValue; // the only value of the qualifier annotation, jBeanBox only support 1 value

	// below fields for BeanBox has no target
	protected Class<?> beanClass; // bean class, usually is an annotated class

	protected Boolean singleton = null; // Default Beanbox is singleton

	protected Constructor<?> constructor; // if not null, use constructor to create

	protected BeanBox[] constructorParams; // store constructor parameters if have

	// ======================fill or call back stage====================

	protected Method postConstruct; // if not null, call these methods after built

	protected Method preDestroy; // if not null, call postConstruct after built

	protected Map<Field, BeanBox> fieldInjects;// if not null, inject Fields

	protected Map<Method, BeanBox[]> methodInjects;// if not null, inject Methods

	// ========== AOP About ===========
	protected Map<Method, List<Object>> methodAops;// if not null, need create proxy bean
	protected List<Object[]> aopRules;// if not null, need create proxy bean

	public BeanBox() { // Default constructor
	}

	public Object getSingletonId() {
		if (singleton == null || !singleton || pureValue || target != null)
			return null;
		return this;
	}

	/** Use default global BeanBoxContext to create bean */
	public <T> T getBean() {
		return BeanBoxContext.globalBeanBoxContext.getBean(this);
	}

	/** For debug purpose */
	public String getDebugInfo() {
		StringBuilder sb = new StringBuilder("\r\n BeanBox properties:\r\n");
		sb.append("target=" + this.target).append("\r\n");
		sb.append("pureValue=" + this.pureValue).append("\r\n");
		sb.append("type=" + this.type).append("\r\n");
		sb.append("required=" + this.required).append("\r\n");
		sb.append("beanClass=" + this.beanClass).append("\r\n");
		sb.append("singleton=" + this.singleton).append("\r\n");
		sb.append("methodAops=" + this.methodAops).append("\r\n");
		sb.append("methodAopRules=" + this.aopRules).append("\r\n");
		sb.append("constructor=" + this.constructor).append("\r\n");
		sb.append("constructorParams=" + this.constructorParams).append("\r\n");
		sb.append("postConstructs=" + this.postConstruct).append("\r\n");
		sb.append("preDestorys=" + this.preDestroy).append("\r\n");
		sb.append("fieldInjects=" + this.fieldInjects).append("\r\n");
		sb.append("methodInjects=" + this.methodInjects).append("\r\n");
		sb.append("qualifierAnno=" + this.qualifierAnno).append("\r\n");
		sb.append("qualifierValue=" + this.qualifierValue);
		return sb.toString();
	}

	protected void checkOrCreateFieldInjects() {// no need explain
		if (fieldInjects == null)
			fieldInjects = new HashMap<Field, BeanBox>();
	}

	protected void checkOrCreateMethodInjects() {// no need explain
		if (methodInjects == null)
			methodInjects = new HashMap<Method, BeanBox[]>();
	}

	protected void checkOrCreateMethodAops() {// no need explain
		if (methodAops == null)
			methodAops = new HashMap<Method, List<Object>>();
	}

	protected void checkOrCreateMethodAopRules() { // no need explain
		if (aopRules == null)
			aopRules = new ArrayList<Object[]>();
	}

	protected Class<?> checkBeanClassExist(){
		if(beanClass==null) {
			Method mtd=ReflectionUtils.findMethod(this.getClass(), "create");
			Class<?> returnType=mtd.getReturnType();
			if(returnType!=Object.class)
				beanClass=returnType; 
		}		
		return beanClass;
	}

	protected BeanBox newCopy() {
		BeanBox box = new BeanBox();
		box.target = this.target;
		box.pureValue = this.pureValue;
		box.type = this.type;
		box.required = this.required;
		box.qualifierAnno = this.qualifierAnno;
		box.qualifierValue = this.qualifierValue;
		box.beanClass = this.beanClass;
		box.singleton = this.singleton;
		box.constructor = this.constructor;
		box.constructorParams = this.constructorParams;
		box.postConstruct = this.postConstruct;
		box.preDestroy = this.preDestroy;
		box.fieldInjects = this.fieldInjects;
		box.methodInjects = this.methodInjects;
		box.methodAops = this.methodAops;
		box.aopRules = this.aopRules;
		return box;
	}

	protected void belowAreJavaConfigMethods_______________() {// NOSONAR
	}

	/** Set this box as a pure value */
	public BeanBox setAsValue(Object value) {
		this.pureValue = true;
		this.target = value;
		return this;
	}

	/** setPrototype(true) equal to setSintleton(false) */
	public BeanBox setPrototype(boolean isPrototype) {
		this.singleton = !isPrototype;
		return this;
	}

	/**
	 * This is Java configuration method equal to put @INJECT on a class's
	 * constructor, a usage example: injectConstruct(User.class, String.class,
	 * JBEANBOX.value("Sam"));
	 */
	public BeanBox injectConstruct(Class<?> clazz, Object... configs) {
		this.beanClass = clazz;
		if (configs.length == 0) {
			this.constructor = BeanBoxUtils.getConstructor(clazz);
		} else {
			Class<?>[] paramTypes = new Class<?>[configs.length / 2];
			BeanBox[] params = new BeanBox[configs.length / 2];
			int mid = configs.length / 2;
			for (int i = 0; i < mid; i++)
				paramTypes[i] = (Class<?>) configs[i];
			for (int i = mid; i < configs.length; i++) {
				params[i - mid] = BeanBoxUtils.wrapParamToBox(configs[i]);
				params[i - mid].setType(paramTypes[i - mid]);
			}
			this.constructor = BeanBoxUtils.getConstructor(clazz, paramTypes);
			this.constructorParams = params;
		}
		return this;
	}

	/**
	 * This is shortcut inject for method parameter type is same as parameter
	 * itself, usage example: injectMtd("setName", "Sam");
	 */
	public BeanBox injectConstr(Class<?> clazz, Object... configs) {
		Object[] newConfigs = new Object[configs.length * 2];
		for (int i = 0; i < configs.length; i++) {
			newConfigs[i * 2] = configs[i].getClass();
			newConfigs[i * 2 + 1] = configs[i];
		}
		return injectConstruct(clazz, newConfigs);
	}

	/**
	 * This is Java configuration method equal to put @INJECT on a class's method, a
	 * usage example: injectMethod("setName", String.class, JBEANBOX.value("Sam"));
	 */
	public BeanBox injectMethod(String methodName, Object... configs) {
		checkOrCreateMethodInjects();
		Class<?>[] paramTypes = new Class<?>[configs.length / 2];
		BeanBox[] params = new BeanBox[configs.length / 2];
		int mid = configs.length / 2;
		for (int i = 0; i < mid; i++)
			paramTypes[i] = (Class<?>) configs[i];
		for (int i = mid; i < configs.length; i++) {
			params[i - mid] = BeanBoxUtils.wrapParamToBox(configs[i]);
			params[i - mid].setType(paramTypes[i - mid]);
		}
		Method m = ReflectionUtils.findMethod(checkBeanClassExist(), methodName, paramTypes);
		if (m != null)
			ReflectionUtils.makeAccessible(m);
		this.getMethodInjects().put(m, params);
		return this;
	}

	/**
	 * This is shortcut inject for method parameter type is same as parameter
	 * itself, usage example: injectMtd("setName", "Sam");
	 */
	public BeanBox injectMtd(String methodName, Object... configs) {
		Object[] newConfigs = new Object[configs.length * 2];
		for (int i = 0; i < configs.length; i++) {
			newConfigs[i * 2] = configs[i].getClass();
			newConfigs[i * 2 + 1] = configs[i];
		}
		return injectMethod(methodName, newConfigs);
	}

	/**
	 * This is Java configuration method equal to put a AOP annotation on method. a
	 * AOP annotation is a kind of annotation be binded to an AOP alliance
	 * interceptor like ctx.bind(Tx.class, MyInterceptor.class); then you can put
	 * a @Tx annotation on method. But this method allow aop can be annotation class
	 * or interceptor class for both
	 */
	public synchronized BeanBox addMethodAop(Object aop, Method method) {
		checkOrCreateMethodAops();
		List<Object> aops = methodAops.get(method);
		if (aops == null) {
			aops = new ArrayList<Object>();
			methodAops.put(method, aops);
		}
		aops.add(BeanBoxUtils.checkAOP(aop));
		return this;
	}

	/**
	 * This is Java configuration method equal to put a AOP annotation on method. a
	 * AOP annotation is a kind of annotation be binded to an AOP alliance
	 * interceptor like ctx.bind(Tx.class, MyInterceptor.class); then you can put
	 * a @Tx annotation on method. But this method allow aop can be annotation class
	 * or interceptor class for both
	 */
	public synchronized BeanBox addMethodAop(Object aop, String methodName, Class<?>... paramTypes) {
		checkOrCreateMethodAops();
		Method m = ReflectionUtils.findMethod(checkBeanClassExist(), methodName, paramTypes);
		BeanBoxException.assureNotNull(m, "Not found method: '" + methodName + "'");
		addMethodAop(aop, m);
		return this;
	}

	/**
	 * Add an AOP to Bean
	 * 
	 * @param aop
	 *            An AOP alliance interceptor class or instance
	 * @param methodNameRegex
	 * @return
	 */
	public synchronized BeanBox addBeanAop(Object aop, String methodNameRegex) {
		checkOrCreateMethodAopRules();
		aopRules.add(new Object[] { BeanBoxUtils.checkAOP(aop), methodNameRegex });
		return this;
	}

	public BeanBox setPostConstruct(String methodName) {// NOSONAR
		Method m = ReflectionUtils.findMethod(checkBeanClassExist(), methodName);
		this.setPostConstruct(m);
		return this;
	}

	public BeanBox setPreDestroy(String methodName) {// NOSONAR
		Method m = ReflectionUtils.findMethod(checkBeanClassExist(), methodName);
		this.setPreDestroy(m);
		return this;
	}

	/**
	 * Inject class, BeanBox class or instance,
	 */
	public BeanBox injectField(String fieldName, Object object) {
		BeanBox box = BeanBoxUtils.wrapParamToBox(object);
		checkOrCreateFieldInjects();
		Field f = ReflectionUtils.findField(checkBeanClassExist(), fieldName);
		box.setType(f.getType());
		ReflectionUtils.makeAccessible(f);
		this.getFieldInjects().put(f, box);
		return this;
	}

	/** Compatible for old jBeanBox version, Inject a pure value to Field */
	public BeanBox setProperty(String fieldName, Object object) {
		return injectField(fieldName, object);
	}

	/** Inject a pure value to Field */
	public BeanBox injectValue(String fieldName, Object constValue) {
		checkOrCreateFieldInjects(); 
		Field f = ReflectionUtils.findField(checkBeanClassExist(), fieldName);
		BeanBox inject = new BeanBox();
		inject.setTarget(constValue);
		inject.setType(f.getType());
		inject.setPureValue(true);
		ReflectionUtils.makeAccessible(f);
		this.getFieldInjects().put(f, inject);
		return this;
	}

	public Object create() {// for child class override
		return null;
	}

	public Object create(BeanBoxContext context) {//NOSONAR  for child class override
		return null;
	}

	public Object create(BeanBoxContext context, Set<Object> history) {//NOSONAR for child class override
		return null;
	}

	public void config(Object bean) {// for child class override
	}

	public void config(Object bean, BeanBoxContext context) {// for child class override
	}

	public void config(Object bean, BeanBoxContext context, Set<Object> history) {// for child class override
	}
	
	protected void getterAndSetters_____________________() {// NOSONAR
	}

	public Object getTarget() {
		return target;
	}

	public BeanBox setTarget(Object target) {
		this.target = target;
		return this;
	}

	public boolean isPureValue() {
		return pureValue;
	}

	public BeanBox setPureValue(boolean pureValue) {
		this.pureValue = pureValue;
		return this;
	}

	public Class<?> getType() {
		return type;
	}

	public BeanBox setType(Class<?> type) {
		this.type = type;
		return this;
	}

	public boolean isRequired() {
		return required;
	}

	public BeanBox setRequired(boolean required) {
		this.required = required;
		return this;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public BeanBox setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
		return this;
	}

	public boolean isSingleton() {
		return singleton != null && singleton;
	}

	public BeanBox setSingleton(boolean singleton) {
		this.singleton = singleton;
		return this;
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	public BeanBox setConstructor(Constructor<?> constructor) {
		this.constructor = constructor;
		return this;
	}

	public BeanBox[] getConstructorParams() {
		return constructorParams;
	}

	public BeanBox setConstructorParams(BeanBox[] constructorParams) {
		this.constructorParams = constructorParams;
		return this;
	}

	public Method getPostConstruct() {
		return postConstruct;
	}

	public BeanBox setPostConstruct(Method postConstruct) {
		this.postConstruct = postConstruct;
		return this;
	}

	public Method getPreDestroy() {
		return preDestroy;
	}

	public BeanBox setPreDestroy(Method preDestroy) {
		this.preDestroy = preDestroy;
		return this;
	}

	public Map<Field, BeanBox> getFieldInjects() {
		return fieldInjects;
	}

	public BeanBox setFieldInjects(Map<Field, BeanBox> fieldInjects) {
		this.fieldInjects = fieldInjects;
		return this;
	}

	public Map<Method, BeanBox[]> getMethodInjects() {
		return methodInjects;
	}

	public BeanBox setMethodInjects(Map<Method, BeanBox[]> methodInjects) {
		this.methodInjects = methodInjects;
		return this;
	}

	public Map<Method, List<Object>> getMethodAops() {
		return methodAops;
	}

	public BeanBox setMethodAops(Map<Method, List<Object>> methodAops) {
		this.methodAops = methodAops;
		return this;
	}

	public List<Object[]> getAopRules() {
		return aopRules;
	}

	public BeanBox setAopRules(List<Object[]> aopRules) {
		this.aopRules = aopRules;
		return this;
	}

	public Class<? extends Annotation> getQualifierAnno() {
		return qualifierAnno;
	}

	public BeanBox setQualifierAnno(Class<? extends Annotation> qualifierAnno) {
		this.qualifierAnno = qualifierAnno;
		return this;
	}

	public Object getQualifierValue() {
		return qualifierValue;
	}

	public BeanBox setQualifierValue(Object qualifierValue) {
		this.qualifierValue = qualifierValue;
		return this;
	}

}
