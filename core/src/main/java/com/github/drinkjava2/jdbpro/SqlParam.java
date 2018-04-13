/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.drinkjava2.jdbpro;

/**
 * SqlParam store SQL parameters
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */
public class SqlParam {
	
	/** Normal SQL param, is a Object[] type*/
	public static final String PARAM = "PARAM";// NOSONAR 
	
	/** Template param, is a Object[2] type */
	public static final String PUT = "PUT";  
	
	/**Template replace param, is a Object[2] type */
	public static final String REPLACE = "REPLACE"; 
	
	/** Param but also return question marks */
	public static final String QUESTION_PARAM = "QUESTION_PARAM";
	public static final String VALUES_QUESTIONS = "VALUES_QUESTIONS";

	private String type;
	private Object[] parameters;

	public SqlParam(String type, Object... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	/** return the parameter array, same as getParameters method */
	public Object[] value() {
		return parameters;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		throw new DbProRuntimeException("Param instance is not allowed to change to String directly");
	}

}
