/*
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.annotation.jdia;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * A shortcut annotation of Unique, only for one column
 * 
 * @author Yong Zhu
 * @since 1.0.5
 */

@Target(FIELD)
@Retention(RUNTIME)
public @interface SingleUnique {

    /**
     * (Optional) The name of the index; defaults to a provider-generated name.
     */
    String name() default ""; 
}
