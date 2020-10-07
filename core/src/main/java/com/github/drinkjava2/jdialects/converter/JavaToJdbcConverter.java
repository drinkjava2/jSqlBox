/**
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.drinkjava2.jdialects.converter;

/**
 * JavaToJdbcConverter used to convert Java value to JDBC value
 * 
 * @author yongz
 * @since 5.0.0
 *
 */
public interface JavaToJdbcConverter {

	/** Convert Java value to JDBC value  */
	public Object convert(Object value);

}