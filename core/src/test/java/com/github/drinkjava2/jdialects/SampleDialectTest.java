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

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.entity.UserTB;

/**
 * Here is a demo to show how to create a user customized dialect "SampleDialect" and "SampleDialect2" 
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings("all")
public class SampleDialectTest {

    public static class SampleDialect extends Dialect {
        public SampleDialect() {
            super();
            copyDialect(Dialect.MySQL55Dialect, this); //从现有的方言中选一个，拷贝它的属性，
            this.topLimitTemplate=this.topLimitTemplate+"   "; //然后改动所有模板都可以
        }

        //可覆盖父类方法实现自定义功能    
        @Override
        public String pagin(int pageNumber, int pageSize, String sql) {//分页
            return super.pagin(pageNumber, pageSize, sql);
        }

        @Override
        public String trans(String... sql) { //函数翻译
            return super.trans(sql);
        }

        @Override
        public String[] toCreateDDL(Class<?>... entityClasses) { //生成DDL
            return super.toCreateDDL(entityClasses);
        } 
    }
    
    public static class SampleDialect2 extends SampleDialect {
        public SampleDialect2() {
            super(); 
        }
 
        @Override
        public String[] toCreateDDL(Class<?>... entityClasses) { //生成DDL
            return super.toCreateDDL(entityClasses);
        } 
    }
 
    
    public static void copyDialect(Dialect from, Dialect to) {
        ClassCacheUtils.copyBean(from.ddlFeatures, to.ddlFeatures);//从已有方言复制DDL属性
        to.sqlTemplate = from.sqlTemplate; //从已有方言复制分页模板
        to.topLimitTemplate = from.topLimitTemplate;
        to.typeMappings.putAll(from.typeMappings); //从已有方言复制类型定义模板
        to.functions.putAll(from.functions); //从已有方言复制函数定义模板
    }

    @Test
    public void doTest() {
        SampleDialect2 dialect = new SampleDialect2();
        Systemout.println(dialect.getName());
        for (String ddl : dialect.toCreateDDL(UserTB.class))
            Systemout.println(ddl);
    }
}
