/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
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
