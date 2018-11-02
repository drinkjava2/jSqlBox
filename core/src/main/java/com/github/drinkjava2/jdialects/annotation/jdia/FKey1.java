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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

 
/**
 * Specifies the foreign key
  
 * <pre>
 *   Example:
 *
 *   &#064;FKey1(name="fk_1", columns="field1,field2", ref="OtherTable, field1, field2")
 *   public class SomeClass()
 * </pre> 
 *
 * @since jDialects 1.0.5
 */
@Target(TYPE) 
@Retention(RUNTIME)
public @interface FKey1 {
    /**
     * (Optional) The name of the foreign key. 
     */
    String name() default "";
 

    /**
     * Columns in this table
     */
    String[] columns() default {};

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
