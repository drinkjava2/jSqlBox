/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.jbeanbox.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AOP used to mark a AOP annotation, for example: <br/>
 * 
 * <pre>
 * &#64;Retention(RetentionPolicy.RUNTIME)
 * &#64;Target({ ElementType.TYPE, ElementType.METHOD })
 * &#64;AOP
 * public static @interface MyAop {
 * 	public Class<?> value() default Interceptor1.class;
 * 
 * 	public String method() default "";
 * }
 * 
 * &#64;MyAop
 * public class User {
 * 
 *   &#64;MyAop
 *   public void setName(){
 *   ....
 *   }
 * }
 * 
 * 
 * </pre>
 * 
 * Note: for method, method field definition is not required
 * 
 * 
 * @author Yong Zhu
 * @since 2.4.7
 *
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AOP {
}
