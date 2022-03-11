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
 * SampleDialect is only a sample to show how to extend a existing dialect
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
@SuppressWarnings("all")
public class SampleDialect extends Dialect {

    public void copyExistDialect(Dialect d) {
        ddlFeatures = DDLFeatures.copyDDLFeatures(d);//复制已有的方言DDL属性
        sqlTemplate = d.sqlTemplate; //从已有方言复制分页模板
        topLimitTemplate = d.topLimitTemplate; //从已有方言复制Top分页模板
        typeMappings.putAll(d.typeMappings); //从已有方言复制类型定义
        functions.putAll(d.functions); //从已有方言复制函数模板
    }

    public SampleDialect(String name) {
        super(name);
        copyExistDialect(Dialect.MySQL55Dialect);
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
    public String[] toCreateDDL(Class<?>... entityClasses) { //根据实体类生成DDL
        return super.toCreateDDL(entityClasses);
    }

}
