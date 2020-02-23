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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Similar like JPA's &#064;Column annotation, but enhenced with extra fields
 * like tail, comment, createTimestamp, UpdateTimestamp, createdBy,
 * LastModifiedBy
 * 
 * @author Yong Zhu
 * @since 4.0.2
 */
@Target({ FIELD })
@Retention(RUNTIME)
public @interface COLUMN {

	/**
	 * (Optional) The name of the column. Defaults to the property or field name.
	 */
	String name() default "";

	/**
	 * (Optional) Whether the column is a unique key. This is a shortcut for the
	 * <code>UniqueConstraint</code> annotation at the table level and is useful for
	 * when the unique key constraint corresponds to only a single column. This
	 * constraint applies in addition to any constraint entailed by primary key
	 * mapping and to constraints specified at the table level.
	 */
	boolean unique() default false;

	/**
	 * (Optional) Whether the database column is nullable.
	 */
	boolean nullable() default true;

	/**
	 * (Optional) Whether the column is included in SQL INSERT statements generated
	 * by the persistence provider.
	 */
	boolean insertable() default true;

	/**
	 * (Optional) Whether the column is included in SQL UPDATE statements generated
	 * by the persistence provider.
	 */
	boolean updatable() default true;

	/**
	 * (Optional) The SQL fragment that is used when generating the DDL for the
	 * column.
	 * <p>
	 * Defaults to the generated SQL to create a column of the inferred type.
	 */
	String columnDefinition() default "";

	/**
	 * (Optional) The name of the table that contains the column. If absent the
	 * column is assumed to be in the primary table.
	 */
	String table() default "";

	/**
	 * (Optional) The column length. (Applies only if a string-valued column is
	 * used.)
	 */
	int length() default 255;

	/**
	 * (Optional) The precision for a decimal (exact numeric) column. (Applies only
	 * if a decimal column is used.) Value must be set by developer if used when
	 * generating the DDL for the column.
	 */
	int precision() default 0;

	/**
	 * (Optional) The scale for a decimal (exact numeric) column. (Applies only if a
	 * decimal column is used.)
	 */
	int scale() default 0;

	/** (Optional)The comment String, usage: &#064;Column(comment="abc"); */
	String comment() default "";

	/** (Optional)The Tail String, usage: &#064;Column(tail="default now()"); */
	String tail() default "";

	/** (Optional)When insert bean will write current timestamp value */
	boolean createTimestamp() default false;

	/** (Optional)When update bean will update current timestamp value */
	boolean updateTimestamp() default false;

	/**
	 * (Optional)When insert bean will write current userId, usage:
	 * 
	 * <pre>
	 * 1. Write a class which have a getCurrentAuditor method
	 * 2. Call JBEANBOX.bind("AuditorAware", XxxClass.class) to bind it
	 * 
	 * For example: 
	 * public class XxxxUserAuditor{
	 * 	public Object getCurrentAuditor() { 
	 *     return XxxxSecurityTool.getUserXxxxId();
	 * 	}
	 * }
	 * 
	 * JBEANBOX.bind("AuditorAware", XxxxUserAuditor.class);
	 * </pre>
	 */
	boolean createdBy() default false;

	/** (Optional)When update bean will write current userId, usage see createdBy */
	boolean lastModifiedBy() default false;
}
