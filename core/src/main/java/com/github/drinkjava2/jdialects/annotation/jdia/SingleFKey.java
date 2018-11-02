/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jdialects.annotation.jdia;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

 
/**
 * A shortcut annotation of FKey, only for one column
 */
@Target(FIELD) 
@Retention(RUNTIME)
public @interface SingleFKey {
    /**
     * (Optional) The name of the foreign key. 
     */
    String name() default "";
 
	/**
	 * Referenced table name and columns, first is table name, followed by column
	 * names, like "table1, col1, col2..."
	 */
	String[] refs() default {};
	
	/**
	 * if ddl set to false, will not output DDL when call TableModelUtils's
	 * entity2Model() and oneEntity2Model methods
	 */
	boolean ddl() default true;
}
