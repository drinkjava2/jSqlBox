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

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies that a unique constraint is to be included in
 * the generated DDL for a primary or secondary table.
 *
 * <pre>
 *    Example:
 *    &#064;Entity
 *    &#064;Table(
 *        name="EMPLOYEE", 
 *        uniqueConstraints=
 *            &#064;UniqueConstraint(columnNames={"EMP_ID", "EMP_NAME"})
 *    )
 *    public class Employee { ... }
 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Target({}) 
@Retention(RUNTIME)
public @interface UniqueConstraint {

    /** (Optional) Constraint name.  A provider-chosen name will be chosen
     * if a name is not specified.
     *
     * @since Java Persistence 2.0
     */
    String name() default "";

    /** (Required) An array of the column names that make up the constraint. */
    String[] columnNames();
}
