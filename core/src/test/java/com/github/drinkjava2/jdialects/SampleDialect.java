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

import java.util.Arrays;

import com.github.drinkjava2.jdialects.entity.UserTB;

/**
 * SampleDialect is only a sample to show how to extend a existing dialect
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings("all")
public class SampleDialect extends Dialect {

    public static void copyDialect(Dialect from, Dialect to) {
        ClassCacheUtils.copyBean(from.ddlFeatures, to.ddlFeatures);//从已有方言复制DDL属性

        to.sqlTemplate = from.sqlTemplate; //从已有方言复制分页模板
        to.topLimitTemplate = from.topLimitTemplate;

        to.typeMappings.putAll(from.typeMappings); //从已有方言复制类型定义模板

        to.functions.putAll(from.functions); //从已有方言复制函数定义模板
    }

    public SampleDialect() {
        super();
        copyDialect(Dialect.MySQL55Dialect, this);
    }

    //可以覆盖父类方法实现自定义功能    
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

    public static void main(String[] args) {
        SampleDialect d = new SampleDialect();
        System.out.println(Arrays.deepToString(d.toCreateDDL(UserTB.class)));
    }
}
