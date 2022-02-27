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
package com.github.drinkjava2.jdialects;

/**
 * Set global NamingConversion for entity which have no &#064;table and &#064;column annotation
 * 
 * <pre>
 * For example: 
 *   //OrderDetail.class map to OrderDetail database table, entity field OrderPrice map to OrderPrice column 
 *   Dialect.setGlobalNamingRule(NamingRule.NONE);
 *   
 *   //OrderDetail.class map to order_detail database table, entity field OrderPrice map to order_price column
 *   Dialect.setGlobalNamingRule(NamingRule.LOWER_CASE_UNDERSCORE);  //
 *   
 *   //OrderDetail.class map to ORDER_DETAIL database table, entity field OrderPrice map to ORDER_PRICE column
 *   Dialect.setGlobalNamingRule(NamingRule.UPPER_CASE_UNDERSCORE);
 * </pre>
 * 
 * @author Yong
 * @since 5.0.10
 */
public interface NamingConversion {

    /** Get table name from entity class  */
    public String getTableName(Class<?> clazz);

    /**  Get column name from entity field    */
    public String getColumnName(String entityField);

    public static final NamingConversion NONE = null;
    public static final NamingConversion LOWER_CASE_UNDERSCORE = new LowerCaseUnderscoreConversion();
    public static final NamingConversion UPPER_CASE_UNDERSCORE = new UpperCaseUnderscoreConversion();
 

    public static class LowerCaseUnderscoreConversion implements NamingConversion {
        @Override
        public String getTableName(Class<?> clazz) {
            return StrUtils.camelToLowerCaseUnderScore(clazz.getSimpleName());
        }

        @Override
        public String getColumnName(String entityField) {
            return StrUtils.camelToLowerCaseUnderScore(entityField);
        }
    }

    public static class UpperCaseUnderscoreConversion implements NamingConversion {
        @Override
        public String getTableName(Class<?> clazz) {
            return StrUtils.camelToLowerCaseUnderScore(clazz.getSimpleName()).toUpperCase();
        }

        @Override
        public String getColumnName(String entityField) {
            return StrUtils.camelToLowerCaseUnderScore(entityField).toUpperCase();
        }
    }

}
