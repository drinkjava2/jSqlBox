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

import java.util.Map.Entry;

import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
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
	private String type;

	/** The reference table name or entity class */
	private Object target;

	/** A String expression condition should return true or false */
	private String where;

	/**
	 * Checker class or Checker instance, used to check if a node can be selected
	 */
	private Object checker;

	/** The reference column names or entity field names */
	private String[] refs;

	/** Next Path, used for build a linked path chain */
	private Path nextPath;

	/** Not allow cache */
	private Boolean cacheable = true;

	// ==================inside used fields======================
	// Initialize some fields to improve speed
	private Boolean checkerInitialized = false;
	private BeanValidator checkerInstance;

	private Boolean uniqueStringInitialized = false;
	private String uniqueStringId;

	private Boolean joinColumnsInitialized = false;
	private String joinedColumns;

	private TableModel targetModel = null;
	private String refColumns;

	/** When path linked, this field store father Path */
	private Path fatherPath;

	public Path(String type, Object target, Object checker, String... refs) {
		this.type = type;
		this.target = target;
		this.checker = checker;
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
		checkInitialized();
		Path next = new Path(type, target, checker, refs);
		next.fatherPath = this;
		this.nextPath = next;
		return next;
	}

	public Path nextPath(String type, Object target, String... refs) {
		checkInitialized();
		Path next = new Path(type, target, refs);
		next.fatherPath = this;
		this.nextPath = next;
		return next;
	}

	public Path nextPath(String type, Object target) {
		checkInitialized();
		Path next = new Path(type, target);
		next.fatherPath = this;
		this.nextPath = next;
		return next;
	}

	/**
	 * In query method will call this method to determine the real target and
	 * columns
	 */
	public void initializeTargetAndColumns(TinyNet net) {
		if (target == null || StrUtils.isEmpty(target))
			throw new TinyNetException("In path, target can not be null or empty.");
		TableModel model = null;
		// if target is a String represented Class name?
		if (this.getTarget() instanceof String) {
			String tbName = (String) this.getTarget();
			for (Entry<Class<?>, TableModel> entry : net.getConfigModels().entrySet()) {
				TableModel mod = entry.getValue();
				if (mod != null && tbName.equalsIgnoreCase(mod.getTableName())) {
					model = mod;
					break;
				}
			}
		} else {// target is Class type
			if (!(this.getTarget() instanceof Class))
				throw new TinyNetException("In path, target can only be table name string or entity class.");
			model = net.getConfigModels().get((Class<?>) this.getTarget());
		}
		if (model == null)
			throw new TinyNetException("Can not find target model for target '" + this.getTarget() + "'");
		this.targetModel = model;

		if (refs == null)
			this.refColumns = null;
		else { // compare tableModel to determine refs are field names or column names
			StringBuilder sb = new StringBuilder();
			for (String ref : refs) {
				boolean found = false;
				for (ColumnModel col : model.getColumns()) {
					if (ref.equalsIgnoreCase(col.getEntityField())) {
						if (found)
							throw new TinyNetException("Can't judge '" + ref + "' is a field or a column name.");
						found = true;
						if (sb.length() > 0)
							sb.append(TinyNet.COMPOUND_COLUMNNAME_SEPARATOR);
						sb.append(col.getColumnName());
					} else if (ref.equalsIgnoreCase(col.getColumnName())) {
						if (ref.equalsIgnoreCase(col.getEntityField())) {
							if (found)
								throw new TinyNetException("Can't judge '" + ref + "' is a field or a column name.");
							found = true;
							if (sb.length() > 0)
								sb.append(TinyNet.COMPOUND_COLUMNNAME_SEPARATOR);
							sb.append(ref);
						}
					}
				}
				if (!found)
					throw new TinyNetException("Can't find reference column name for '" + ref + "'  ");
			}
			this.refColumns = sb.toString();
		}
		if (this.getNextPath() != null)
			this.getNextPath().initializeTargetAndColumns(net);
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

	/**
	 * Get a unique Id String represent this path if it will never change, otherwise
	 * return null, this id will be used as query cache key
	 */
	public String getUniqueIdString() {
		if (uniqueStringInitialized)
			return uniqueStringId;
		uniqueStringId = null;
		uniqueStringInitialized = true;
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
		if (refs != null) {
			sb.append(",columns:");
			for (String colName : refs)
				sb.append(colName);
		}
		if (!StrUtils.isEmpty(next))
			sb.append(",Next:").append(next);
		uniqueStringId = sb.toString();
		return uniqueStringId;
	}

	/** Get Checker instance */
	public BeanValidator getCheckerInstance() {
		if (checkerInitialized)
			return checkerInstance;
		else {
			checkerInstance = TinyNetUtils.getOrBuildChecker(checker);
			checkerInitialized = true;
			return checkerInstance;
		}
	}

	public String getJoinedColumns() {
		if (joinColumnsInitialized)
			return joinedColumns;
		else {
			joinedColumns = TinyNetUtils.buildJoinedColumns(refs);
			joinColumnsInitialized = true;
			return joinedColumns;
		}
	}

	public Path where(String where) {
		checkInitialized();
		this.where = where;
		return this;
	}

	public Path getTopPath() {
		if (this.fatherPath == null)
			return this;
		else
			return fatherPath.getTopPath();
	}

	public void checkInitialized() {
		if (this.uniqueStringInitialized)
			throw new TinyNetException("Can change path setting after query");
	}

	// =====Getter & Setter ==============
	public String getType() {
		return type;
	}

	public Path setType(String type) {
		checkInitialized();
		this.type = type;
		validateType();
		return this;
	}

	public Path getNextPath() {
		return nextPath;
	}

	/** Set a Checker instance or Checker class before query */
	public Path setNextPath(Path nextPath) {
		checkInitialized();
		this.nextPath = nextPath;
		return this;
	}

	public String[] getRefs() {
		return refs;
	}

	public Path setRefs(String... refs) {
		checkInitialized();
		this.refs = refs;
		return this;
	}

	public Object getTarget() {
		return target;
	}

	public Path setTarget(Object target) {
		checkInitialized();
		this.target = target;
		return this;
	}

	public Object getChecker() {
		return checker;
	}

	/** Set a Checker instance or Checker class before query */
	public Path setChecker(Object checker) {
		checkInitialized();
		this.checker = checker;
		return this;
	}

	public Boolean getCacheable() {
		return cacheable;
	}

	public Path setCacheable(Boolean cacheable) {
		checkInitialized();
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
