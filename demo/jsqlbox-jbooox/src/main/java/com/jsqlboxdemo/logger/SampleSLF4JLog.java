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
package com.jsqlboxdemo.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.drinkjava2.jlogs.Log;

/**
 * This is the logger sample implemented com.github.drinkjava2.jlogs.Log
 * interface based on using SLF4J
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public class SampleSLF4JLog implements Log {
	Logger log;

	public SampleSLF4JLog(Class<?> targetClass) {
		log = LoggerFactory.getLogger(targetClass);
	}

	@Override
	public void debug(String arg0) {
		log.debug(arg0);
	}

	@Override
	public void error(String arg0) {
		log.error(arg0);
	}

	@Override
	public void error(String arg0, Throwable arg1) {
		log.error(arg0, arg1);
	}

	@Override
	public void info(String arg0) {
		log.info(arg0);
	}

	@Override
	public void warn(String arg0) {
		log.warn(arg0);
	}

	@Override
	public void warn(String arg0, Throwable arg1) {
		log.warn(arg0, arg1);
	}

}
