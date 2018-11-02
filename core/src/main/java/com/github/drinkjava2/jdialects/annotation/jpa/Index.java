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
package com.github.drinkjava2.jdialects.annotation.jpa;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used in schema generation to specify creation of an index.
 * <p>
 * Note that it is not necessary to specify an index for a primary key,
 * as the primary key index will be created automatically.
 *
 * <p> 
 * The syntax of the <code>columnList</code> element is a 
 * <code>column_list</code>, as follows:
 * 
 * <pre>
 *    column::= index_column [,index_column]*
 *    index_column::= column_name [ASC | DESC]
 * </pre>
 * 
 * <p> If <code>ASC</code> or <code>DESC</code> is not specified, 
 * <code>ASC</code> (ascending order) is assumed.
 *
 * @see Table
 * @see SecondaryTable
 * @see CollectionTable
 * @see JoinTable
 * @see TableIdGeneratorTest
 *
 * @since Java Persistence 2.1
 *
 */
@Target({})
@Retention(RUNTIME)
public @interface Index {

    /**
     * (Optional) The name of the index; defaults to a provider-generated name.
     */
    String name() default "";

    /**
     * (Required) The names of the columns to be included in the index, 
     * in order.
     */
    String columnList();

    /**
     * (Optional) Whether the index is unique.
     */
    boolean unique() default false;

}
