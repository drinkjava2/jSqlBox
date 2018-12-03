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
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the version field or property of an entity class that serves as its
 * optimistic lock value. The version is used to ensure integrity when
 * performing the merge operation and for optimistic concurrency control.
 *
 * <p>
 * Only a single <code>Version</code> property or field should be used per
 * class; applications that use more than one <code>Version</code> property or
 * field will not be portable.
 * 
 * <p>
 * The <code>Version</code> property should be mapped to the primary table for
 * the entity class; applications that map the <code>Version</code> property to
 * a table other than the primary table will not be portable.
 * 
 * <p>
 * The following types are supported for version properties: <code>int</code>,
 * <code>Integer</code>, <code>short</code>, <code>Short</code>,
 * <code>long</code>, <code>Long</code>
 *
 * <pre>
 *    Example:
 *
 *    &#064;Version
 *    &#064;Column(name="OPTLOCK")
 *    protected int getVersionNum() { return versionNum; }
 * </pre>
 * 
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface Version {
}