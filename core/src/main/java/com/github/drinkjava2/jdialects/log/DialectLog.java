/*
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
package com.github.drinkjava2.jdialects.log;

/**
 * DialectLog used for jDialects project, if a "DialectLog.properties" file
 * found on class root folder (main/resources), will try load the designated
 * DialectLog implementation, otherwise use default DialectPrintLog as log
 * output. <br/>
 * 
 * An example of "DbProLog.properties": <br/>
 * log=com.github.drinkjava2.jdbpro.log.DbProSLF4JLog
 * 
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public interface DialectLog {

	public abstract void info(String msg);

	public abstract void warn(String msg);

	public abstract void error(String msg);

	public abstract void debug(String msg);

}
