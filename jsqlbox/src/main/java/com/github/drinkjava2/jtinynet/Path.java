/*
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jtinynet;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * Path store search condition path, one path can link to another path to build
 * a path chain <br/>
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class Path {
	/**
	 * Can only be
	 * 
	 * <pre>
	 *    "S+": Start nodes,
	 *    "S-": Start nodes, but not put found nodes into result,
	 *    "C+":child nodes, 
	 *    "C-":child nodes, but not put found nodes into result,
	 *    "P+":parent 
	 *    "P-":parent nodes, but not put found nodes into result,
	 *    "P*":all parents
	 *	  "C*":all childs
	 * </pre>
	 */
	String type;

	/** The reference table name or entity class */
	Object target;

	/** A String expression condition should return true or false */
	String expression;

	Object[] expressionParams;

	/**
	 * BeanValidator class or instance, used to check if a node can be selected
	 */
	Object validator;

	/** The reference column names or entity field names */
	String[] refs;

	/** Next Path, used for build a linked path chain */
	Path nextPath;

	/** Not allow cache */
	Boolean cacheable = true;

	// ==================inside used fields======================
	// Initialize some fields to improve speed
	Boolean initialized = false;
	NodeValidator validatorInstance;

	String uniqueStringId;
	String joinedColumns;

	TableModel targetModel;
	String refColumns;

	Object autoPathTarget;

	/** When path linked, this field store father Path */
	Path fatherPath;

	public Path(String type, Object target, Object checker, String... refs) {
		this.type = type;
		this.target = target;
		this.validator = checker;
		this.refs = refs;
		validateType();
	}

	public Path(String type, Object target, String... refs) {
		this.type = type;
		this.target = target;
		this.refs = refs;
		validateType();
	}

	public Path(String type, Object target) {
		this.type = type;
		this.target = target;
		validateType();
	}

	public Path nextPath(String type, Object target, Object checker, String... refs) {
		checkIfModifyInitialized();
		Path next = new Path(type, target, checker, refs);
		next.fatherPath = this;
		this.nextPath = next;
		return next;
	}

	/** Set the next part */
	public Path nextPath(String type, Object target, String... refs) {
		checkIfModifyInitialized();
		Path next = new Path(type, target, refs);
		next.fatherPath = this;
		this.nextPath = next;
		return next;
	}

	/**
	 * Give target table or class, Let computer to automatically build the chain,
	 * note this method only works correct if there is only 1 way can reach to the
	 * target
	 */
	public Path autoPath(Object target) {
		checkIfModifyInitialized();
		this.autoPathTarget = target;
		return this;
	}

	public Path nextPath(String type, Object target) {
		checkIfModifyInitialized();
		Path next = new Path(type, target);
		next.fatherPath = this;
		this.nextPath = next;
		return next;
	}

	/**
	 * In query method will call this method to determine the real target and
	 * columns
	 */
	public void initializePath(TinyNet net) {
		if (initialized)
			return;
		PathUtils.calculateAutoPath(net, this);
		if (target == null || StrUtils.isEmpty(target))
			throw new TinyNetException("In path, target can not be null or empty.");
		Class<?> theTargetClass = PathUtils.findClassByTarget(net, this.target);
		this.targetModel = net.getConfigModels().get(theTargetClass);
		if (this.targetModel == null)
			throw new TinyNetException("Can not find target model for target '" + this.getTarget() + "'");

		if (refs == null)
			this.refColumns = null;
		else {
			this.refColumns = PathUtils.calculateRefColumns(this);
		}
		validatorInstance = TinyNetUtils.getOrBuildChecker(validator);
		if (refs != null)
			joinedColumns = TinyNetUtils.buildJoinedColumns(refs);
		initialized = true;
		if (this.getNextPath() != null)
			this.getNextPath().initializePath(net);
		initializeUniqueIdString();
	}

	/** return target model, note different query may change tableModel */
	protected TableModel getTargetModel() {
		return targetModel;
	}

	/** return target model, note different query may change tableModel */
	protected String getRefColumns() {
		return refColumns;
	}

	/* Check if type setting right */
	void validateType() {
		if (type == null || type.length() != 2)
			throw new TinyNetException("Illegal type charactors: '" + type + "'");
		String s0 = type.substring(0, 1);
		if (!("S".equalsIgnoreCase(s0) || "C".equalsIgnoreCase(s0) || "P".equalsIgnoreCase(s0)
				|| "C*".equalsIgnoreCase(s0) || "P*".equalsIgnoreCase(s0)))
			throw new TinyNetException("Illegal type charactor: '" + s0 + "'");

		String s1 = type.substring(1, 2);
		if (!("+".equals(s1) || "-".equals(s1) || "*".equals(s1)))
			throw new TinyNetException("Illegal type charactor: '" + s1 + "'");
	}

	/**
	 * Get a unique Id String represent this path if it's a "FIXED" path, the id
	 * will be used as query cache key Return null if the path is not a "FIXED"
	 * type, for example it has query parameters.
	 */
	public void initializeUniqueIdString() {
		uniqueStringId = null;
		String next = null;
		if (!cacheable)
			return;
		if (validator != null && validator instanceof NodeValidator)
			return;
		if (nextPath != null) {
			next = nextPath.getUniqueIdString();
			if (StrUtils.isEmpty(next))
				return;
		}
		if (expressionParams != null && expressionParams.length > 0)
			return;
		StringBuilder sb = new StringBuilder()//
				.append("type:").append(type)//
				.append(",target:").append(target)//
				.append(",exp:").append(expression)//
				.append(",validator:").append(validator);
		if (refs != null) {
			sb.append(",columns:");
			for (String colName : refs)
				sb.append(colName);
		}
		if (!StrUtils.isEmpty(next))
			sb.append(",Next:").append(next);
		uniqueStringId = sb.toString();
	}

	/**
	 * Set a SQL style like search condition expression
	 * 
	 * @param expression
	 * @param expressionParams
	 * @return current Path
	 */
	public Path where(String expression, Object... expressionParams) {
		checkIfModifyInitialized();
		this.expression = expression;
		this.expressionParams = expressionParams;
		return this;
	}

	public Path getTopPath() {
		if (this.fatherPath == null)
			return this;
		else
			return fatherPath.getTopPath();
	}

	public void checkIfModifyInitialized() {
		if (initialized)
			throw new TinyNetException("It's not allowed to change path setting after it be initialized");
	}

	public String getJoinedColumns() {
		if (initialized)
			return joinedColumns;
		else
			throw new TinyNetException("Try to get JoinedColumns on a Path not initialized");
	}

	public NodeValidator getNodeValidator() {
		if (initialized)
			return validatorInstance;
		else
			throw new TinyNetException("Try to get NodeValidator on a Path not initialized");
	}

	public String getUniqueIdString() {
		if (initialized)
			return uniqueStringId;
		else
			throw new TinyNetException("Try to get UniqueIdString on a Path not initialized");
	}

	// =====Getter & Setter ==============
	public String getType() {
		return type;
	}

	public Path setType(String type) {
		checkIfModifyInitialized();
		this.type = type;
		validateType();
		return this;
	}

	public Path getNextPath() {
		return nextPath;
	}

	/** Set a Checker instance or Checker class before query */
	public Path setNextPath(Path nextPath) {
		checkIfModifyInitialized();
		this.nextPath = nextPath;
		return this;
	}

	public String[] getRefs() {
		return refs;
	}

	public Path setRefs(String... refs) {
		checkIfModifyInitialized();
		this.refs = refs;
		return this;
	}

	public Object getTarget() {
		return target;
	}

	public Path setTarget(Object target) {
		checkIfModifyInitialized();
		this.target = target;
		return this;
	}

	public Object getValidator() {
		return validator;
	}

	/** Set a BeanValidator instance or class before query */
	public Path setValidator(Object validator) {
		checkIfModifyInitialized();
		this.validator = validator;
		return this;
	}

	public Boolean getCacheable() {
		return cacheable;
	}

	public Path setCacheable(Boolean cacheable) {
		checkIfModifyInitialized();
		this.cacheable = cacheable;
		return this;
	}

	public String getExpression() {
		return expression;
	}

	public Path setExpression(String expression) {
		checkIfModifyInitialized();
		this.expression = expression;
		return this;
	}

	public Object[] getexpressionParams() {
		return expressionParams;
	}

	public Path setExpressionParams(Object[] expressionParams) {
		checkIfModifyInitialized();
		this.expressionParams = expressionParams;
		return this;
	}

}
