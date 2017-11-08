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
 * Sub class override check method can do a Java Native check for a Node to
 * determine if node can keep in result list
 * 
 * @author Yong Zhu (Yong9981@gmail.com)
 * @since 1.0.0
 */
public class Path {
	/** Can only be "C":Child or "P".:Parent */
	private String type;

	/** The reference table name or entity class */
	private Object target;

	/** allowed keep in input list */
	private Boolean input = true;

	/** allowed keep in output list */
	private Boolean output = true;

	/** Checker class or Checker instance */
	private Object checker;

	/** The fkey column names */
	private String[] columns;

	/** Next Path, used for build a linked path chain */
	private Path nextPath;

	/** Not allow cache */
	private Boolean notCache;

	private void validParam() {
		if (!("C".equalsIgnoreCase(type) || "P".equalsIgnoreCase(type)))
			throw new TinyNetException("Type can only be 'C' or 'P', means child or parent path");
		if (target == null)
			throw new TinyNetException("Target can not be null");
		if (input == null || output == null)
			throw new TinyNetException("input or output can not be null, need be Boolean type");
	}

	public Path(String type, Object target, Boolean input, Boolean output, Object checker, String... columns) {
		validParam();
		this.type = type;
		this.target = target;
		this.input = input;
		this.output = output;
		this.checker = checker;
		this.columns = columns;
	}

	public Path(String type, Object target, String... columns) {
		this.type = type;
		this.target = target;
		this.columns = columns;
	}

	public Path(String type, Object target) {
		this.type = type;
		this.target = target;
	}

	public String getUniqueIdString() {
		String next = null;
		if (nextPath != null) {
			next = nextPath.getUniqueIdString();
			if (StrUtils.isEmpty(next))
				return null;
		}
		if (notCache)
			return null;
		if (checker != null && checker instanceof Checker)
			return null;
		StringBuilder sb = new StringBuilder()//
				.append("type:").append(type)//
				.append(",target:").append(target)//
				.append(",input:").append(input)//
				.append(",output:").append(output)//
				.append(",checker:").append(checker);
		if (columns != null) {
			sb.append(",columns:");
			for (String colName : columns)
				sb.append(colName);
		}
		if (!StrUtils.isEmpty(next))
			sb.append(",Next:").append(next);
		return sb.toString();
	}

	public Path nextPath(String type, Object target, Boolean input, Boolean output, Object checker, String... columns) {
		Path next = new Path(type, target, input, output, checker, columns);
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

	// =====Getter & Setter ==============
	public String getType() {
		return type;
	}

	public Path setType(String type) {
		this.type = type;
		return this;
	}

	public Boolean getInput() {
		return input;
	}

	public Path setInput(Boolean input) {
		this.input = input;
		return this;
	}

	public Boolean getOutput() {
		return output;
	}

	public Path setOutput(Boolean output) {
		this.output = output;
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

	public Boolean getNotCache() {
		return notCache;
	}

	public Path setNotCache(Boolean notCache) {
		this.notCache = notCache;
		return this;
	}

}
