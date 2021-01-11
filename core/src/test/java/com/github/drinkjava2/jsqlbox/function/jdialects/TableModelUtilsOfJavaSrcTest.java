package com.github.drinkjava2.jsqlbox.function.jdialects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.TableModelUtilsOfEntity;
import com.github.drinkjava2.jdialects.TableModelUtilsOfJavaSrc;
import com.github.drinkjava2.jdialects.annotation.jdia.FKey;
import com.github.drinkjava2.jdialects.annotation.jdia.FKey1;
import com.github.drinkjava2.jdialects.annotation.jdia.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

/**
 * Unit test for TableModelUtilsOfJavaSrc.java
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */

public class TableModelUtilsOfJavaSrcTest {

    @Table(name = "entity1_tb")
    @FKey(name = "fkey1", ddl = false, columns = {"id", "name"}, refs = {"entity2", "field1", "field2"})
    @FKey1(columns = {"id", "name"}, refs = {"entity3", "field1", "field2"})
    public class Entity1 extends ActiveRecord<Entity1> {
        @Id
        private Integer id;

        @Id
        @Column(length = 10)
        private String name;

        @Column(name = "cust_id")
        @SingleFKey(refs = {"table2", "id"})
        private Integer custId;

        Entity1 demoEntity;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getCustId() {
            return custId;
        }

        public void setCustId(Integer custId) {
            this.custId = custId;
        }

        public Entity1 getDemoEntity() {
            return demoEntity;
        }

        public void setDemoEntity(Entity1 demoEntity) {
            this.demoEntity = demoEntity;
        }
    }

    @Test
    public void modelToJavaSrcTest() {
        Map<String, Object> setting = new HashMap<String, Object>();
        setting.put(TableModelUtils.OPT_EXCLUDE_TABLES, Arrays.asList("Dbsample")); // 排除表名
        setting.put(TableModelUtils.OPT_PACKAGE_NAME, "somepackage");// 包名
        setting.put(TableModelUtils.OPT_IMPORTS, "import java.util.Map;\n"); // 追加新的imports
        setting.put(TableModelUtils.OPT_REMOVE_DEFAULT_IMPORTS, false); // 不去除自带的imports
        setting.put(TableModelUtils.OPT_CLASS_ANNOTATION, true); // 类上的实体注解
        setting.put(TableModelUtils.OPT_CLASS_DEFINITION, "public class $ClassName extends ActiveRecord<$ClassName> {");// 类定义
        setting.put(TableModelUtils.OPT_FIELD_FLAGS, true); // 全局静态属性字段标记
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STATIC, true); // 全局静态属性字段标记
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STYLE, "upper"); // 大写
        setting.put(TableModelUtils.OPT_FIELDS, true); // 属性
        setting.put(TableModelUtils.OPT_GETTER_SETTERS, true); // getter setter
        setting.put(TableModelUtils.OPT_PUBLIC_FIELD, false); // 属性定义成public
        setting.put(TableModelUtils.OPT_LINK_STYLE, true); // 链式getter/setter风格

        TableModel model = TableModelUtilsOfEntity.entity2ReadOnlyModel(Entity1.class);
        Systemout.allowPrint = true;
        Systemout.println(TableModelUtilsOfJavaSrc.modelToJavaSourceCode(model, setting));
    }

    @Test
    public void modelToJavaSrcTest2() {
        Map<String, Object> setting = new HashMap<String, Object>();
        setting.put(TableModelUtils.OPT_PACKAGE_NAME, "somepackage");// 包名
        setting.put(TableModelUtils.OPT_REMOVE_DEFAULT_IMPORTS, true); // 去除自带的imports
        setting.put(TableModelUtils.OPT_CLASS_ANNOTATION, false); // 类上的实体注解
        setting.put(TableModelUtils.OPT_CLASS_DEFINITION, "" + // 
                "public static class P$Class {\n" + //
                "\tpublic static final P$Class $class = new P$Class();\n\n" + //
                "\tpublic String toString(){\n" + //
                "\t\treturn \"$table\";\n" + //
                "\t}\n");// 类定义
        setting.put(TableModelUtils.OPT_FIELD_FLAGS, true); // 列名标记
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STATIC, false); // 列名标记为静态
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STYLE, "camel"); // 列名标记大写
        setting.put(TableModelUtils.OPT_FIELDS, false); // 属性

        TableModel model = TableModelUtilsOfEntity.entity2ReadOnlyModel(Entity1.class);
        Systemout.allowPrint = true;
        Systemout.println(TableModelUtilsOfJavaSrc.modelToJavaSourceCode(model, setting));
    }
 

}
