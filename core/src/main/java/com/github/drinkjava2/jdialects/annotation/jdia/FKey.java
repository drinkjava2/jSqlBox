/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.annotation.jdia;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies the foreign key
 * 
 * <pre>
 *   Example:
 *
 *   &#064;FKey(name="fk_1", columns="field1,field2", ref="OtherTable, field1, field2")
 *   public class SomeClass()
 * </pre>
 *
 * @since jDialects 1.0.5
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface FKey {
	/**
	 * (Optional) The name of the foreign key.
	 */
	String name() default "";

	/**
	 * Columns in this table
	 */
	String[] columns() default {};

	/**
	 * Referenced table name and columns, first is table name, followed by
	 * column names, like "table1, col1, col2..."
	 */
	String[] refs() default {};

	/**
	 * if ddl set to false, will not output DDL when call TableModelUtils's
	 * entity2Model() and oneEntity2Model methods
	 */
	boolean ddl() default true;
}
