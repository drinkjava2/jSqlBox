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
	private String type;

	/** The reference table name or entity class */
	private Object target;

	/** A String expression condition should return true or false */
	private String where;

	/**
	 * Checker class or Checker instance, used to check if a node can be selected
	 */
	private Object checker;

	/** The fkey column names */
	private String[] columns;

	/** Next Path, used for build a linked path chain */
	private Path nextPath;

	/** Not allow cache */
	private Boolean cacheable = true;

	// ==================inside used fields======================
	private Boolean checkerObjBuilt = false;
	private BeanValidator checkerObj;

	private Boolean uniqueStringBuilt = false;
	private String uniqueStringCached;

	private void validateType() {
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

	public Path(String type, Object target, Object checker, String... columns) {
		this.type = type;
		this.target = target;
		this.checker = checker;
		this.columns = columns;
		validateType();
	}

	public Path(String type, Object target, String... columns) {
		this.type = type;
		this.target = target;
		this.columns = columns;
		validateType();
	}

	public Path(String type, Object target) {
		this.type = type;
		this.target = target;
		validateType();
	}

	public Path(Object target) {
		this.type = "S";
		this.target = target;
		validateType();
	}

	public String getUniqueIdString() {
		if (uniqueStringBuilt)
			return uniqueStringCached;
		uniqueStringCached = null;
		uniqueStringBuilt = true;
		String next = null;
		if (!cacheable)
			return null;
		if (checker != null && checker instanceof BeanValidator)
			return null;
		if (nextPath != null) {
			next = nextPath.getUniqueIdString();
			if (StrUtils.isEmpty(next))
				return null;
		}
		StringBuilder sb = new StringBuilder()//
				.append("type:").append(type)//
				.append(",target:").append(target)//
				.append(",where:").append(where)//
				.append(",checker:").append(checker);
		if (columns != null) {
			sb.append(",columns:");
			for (String colName : columns)
				sb.append(colName);
		}
		if (!StrUtils.isEmpty(next))
			sb.append(",Next:").append(next);
		uniqueStringCached = sb.toString();
		return uniqueStringCached;
	}

	public Path nextPath(String type, Object target, Object checker, String... columns) {
		Path next = new Path(type, target, checker, columns);
		this.setNextPath(next);
		return next;
	}

	public Path nextPath(String type, Object target, String... columns) {
		Path next = new Path(type, target, columns);
		this.setNextPath(next);
		return next;
	}

	public Path nextPath(String type, Object target) {
		Path next = new Path(type, target);
		this.setNextPath(next);
		return next;
	}

	public Path nextPath(Object target) {
		Path next = new Path(target);
		this.setNextPath(next);
		return next;
	}

	/** Get Checker instance */
	public BeanValidator getCheckerInstance() {
		if (checkerObjBuilt)
			return checkerObj;
		else {
			checkerObj = TinyNetUtils.getOrBuildChecker(checker);
			checkerObjBuilt = true;
			return checkerObj;
		}
	}

	public Path where(String where) {
		this.where = where;
		return this;
	}

	// =====Getter & Setter ==============
	public String getType() {
		return type;
	}

	public Path setType(String type) {
		this.type = type;
		this.validateType();
		return this;
	}

	public Path getNextPath() {
		return nextPath;
	}

	public Path setNextPath(Path nextPath) {
		this.nextPath = nextPath;
		return this;
	}

	public String[] getColumns() {
		return columns;
	}

	public Path setColumns(String... columns) {
		this.columns = columns;
		return this;
	}

	public Object getTarget() {
		return target;
	}

	public Path setTarget(Object target) {
		this.target = target;
		return this;
	}

	public Object getChecker() {
		return checker;
	}

	public Path setChecker(Object checker) {
		this.checker = checker;
		return this;
	}

	public Boolean getCacheable() {
		return cacheable;
	}

	public Path setCacheable(Boolean cacheable) {
		this.cacheable = cacheable;
		return this;
	}

	public String getWhere() {
		return where;
	}

	public Path setWhere(String where) {
		this.where = where;
		return this;
	}

}
