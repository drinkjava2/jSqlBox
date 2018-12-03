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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The value of this Convert annotation point to a class, the implementation of
 * this class will used to convert field value to database column value, and
 * column value to database, for example:
 * 
 * <pre>
 * &#64;Convert(XxxDateConverter.class)
 * private Date date;// Tell ORM Tool use XxxDateConverter to do the convert
 * 
 * &#64;Convert(XxxEnumTranslator.class)
 * private Enum some1;// Tell ORM Tool use XxxEnumTranslator to do the convert
 * 
 * &#64;Convert()
 * private Enum some2; // Tell ORM Tool decide how to do the convert by ORM tool itself
 * 
 * </pre>
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Convert {
	Class<?> value() default void.class;

	/**
	 * Specifies the converter to be applied. A value for this element must be
	 * specified if multiple converters would otherwise apply.
	 */
	Class<?> converter() default void.class;// equal to value, for compatible to JPA only

	/**
	 * The <code>attributeName</code> element must be specified unless the
	 * <code>Convert</code> annotation is on an attribute of basic type or on an
	 * element collection of basic type. In these cases, the
	 * <code>attributeName</code> element must not be specified.
	 */
	String attributeName() default ""; // ignored, for compatible to JPA only

	/**
	 * Used to disable an auto-apply or inherited converter. If disableConversion is
	 * true, the <code>converter</code> element should not be specified.
	 */
	boolean disableConversion() default false; // for compatible to JPA only
}