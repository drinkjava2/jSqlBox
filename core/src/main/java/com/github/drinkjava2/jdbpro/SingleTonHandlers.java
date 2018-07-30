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

import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.KeyedHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.github.drinkjava2.jdbpro.handler.TitleArrayListHandler;

/**
 * Here store some singleTon thread safe ResultSetHandlers
 * 
 * @author Yong Zhu
 * @since 1.7.0.3
 */
public abstract class SingleTonHandlers {// NOSONAR
	public static final KeyedHandler<?> keyedHandler = new KeyedHandler<Object>();
	public static final MapHandler mapHandler = new MapHandler();
	public static final MapListHandler mapListHandler = new MapListHandler(); 
	public static final ScalarHandler<?> scalarHandler = new ScalarHandler<Object>();
	public static final ArrayHandler arrayHandler = new ArrayHandler();
	public static final ArrayListHandler arrayListHandler = new ArrayListHandler(); 
	public static final TitleArrayListHandler titleArrayListHandler = new TitleArrayListHandler();
	public static final ColumnListHandler<?> columnListHandler = new ColumnListHandler<Object>(); 
}