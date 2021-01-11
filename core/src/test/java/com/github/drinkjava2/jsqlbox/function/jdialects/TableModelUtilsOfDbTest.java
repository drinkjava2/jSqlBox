package com.github.drinkjava2.jsqlbox.function.jdialects;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Test;

import com.github.drinkjava2.common.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.FKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * Test Date time type
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class TableModelUtilsOfDbTest extends TestBase {

    @Table(name = "student_sample")
    public static class studentSample {
        @Id
        String stAddr;

        @Id
        String stName;

        public String getStName() {
            return stName;
        }

        public void setStName(String stName) {
            this.stName = stName;
        }

        public String getStAddr() {
            return stAddr;
        }

        public void setStAddr(String stAddr) {
            this.stAddr = stAddr;
        }

    }

    @FKey(name = "fkey1", ddl = true, columns = {"address", "name"}, refs = {"student_sample", "stAddr", "stName"})
    public static class DbSample {
        @Id
        @UUID25
        String id;

        @Id
        String name;

        String address;

        String email;

        // @SingleFKey(name = "singlefkey1", refs = { "student_sample", "stAddr" })
        String address2;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAddress2() {
            return address2;
        }

        public void setAddress2(String address2) {
            this.address2 = address2;
        }

    }

    public static class DbSample2 {
        @Id
        String id;

    }

    @Test
    public void doDbToModelTest() throws Exception {
        Map<String, Object> setting = new HashMap<String, Object>();
        setting.put(TableModelUtils.OPT_EXCLUDE_TABLES, Arrays.asList("Dbsample")); // 排除表名
        setting.put(TableModelUtils.OPT_PACKAGE_NAME, "somepackage");// 包名
        setting.put(TableModelUtils.OPT_IMPORTS, "import java.util.Map;\n"); // 追加新的imports
        setting.put(TableModelUtils.OPT_REMOVE_DEFAULT_IMPORTS, false); // 不去除自带的imports
        setting.put(TableModelUtils.OPT_CLASS_DEFINITION, "public class $ClassName extends ActiveRecord<$ClassName> {");// 类定义
        setting.put(TableModelUtils.OPT_FIELD_FLAGS, true); // 全局静态属性字段标记
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STATIC, true); // 全局静态属性字段标记
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STYLE, "upper"); // 大写
        setting.put(TableModelUtils.OPT_FIELDS, true); // 属性
        setting.put(TableModelUtils.OPT_GETTER_SETTERS, true); // getter setter
        setting.put(TableModelUtils.OPT_PUBLIC_FIELD, false); // 属性定义成public
        setting.put(TableModelUtils.OPT_LINK_STYLE, true); // 链式getter/setter风格

        quietDropTables(DbSample.class, studentSample.class);
        createTables(studentSample.class, DbSample.class);
        DataSource ds = JBEANBOX.getBean(DataSourceBox.class);
        Connection conn = null;
        conn = ds.getConnection();
        Dialect dialect = Dialect.guessDialect(conn);
        TableModel[] models = TableModelUtils.db2Models(conn, dialect);
        for (TableModel model : models) {
            Systemout.println("\n\n\n\n");
            Systemout.println(TableModelUtils.model2JavaSrc(model, setting));
        }
        conn.close();
        dropTables(DbSample.class, studentSample.class);
    }

    @Test
    public void testDbToJavaSrcFiles() {
        Map<String, Object> setting = new HashMap<String, Object>();
        setting.put(TableModelUtils.OPT_EXCLUDE_TABLES, Arrays.asList("Dbsample")); // 排除表名
        setting.put(TableModelUtils.OPT_PACKAGE_NAME, "somepackage");// 包名
        setting.put(TableModelUtils.OPT_IMPORTS, "import java.util.Map;\n"); // 追加新的imports
        setting.put(TableModelUtils.OPT_REMOVE_DEFAULT_IMPORTS, false); // 不去除自带的imports
        setting.put(TableModelUtils.OPT_CLASS_DEFINITION, "public class $ClassName extends ActiveRecord<$ClassName> {");// 类定义
        setting.put(TableModelUtils.OPT_FIELD_FLAGS, true); // 全局静态属性字段标记
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STATIC, true); // 全局静态属性字段标记
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STYLE, "upper"); // 大写
        setting.put(TableModelUtils.OPT_FIELDS, true); // 属性
        setting.put(TableModelUtils.OPT_GETTER_SETTERS, true); // getter setter
        setting.put(TableModelUtils.OPT_PUBLIC_FIELD, false); // 属性定义成public
        setting.put(TableModelUtils.OPT_LINK_STYLE, true); // 链式getter/setter风格

        quietDropTables(DbSample.class, studentSample.class);
        createTables(studentSample.class, DbSample.class);
        TableModelUtils.db2JavaSrcFiles(ctx.getDataSource(), ctx.getDialect(), "c:/temp", setting);
        dropTables(DbSample.class, studentSample.class);
    }

    @Test
    public void testDb2QClassSrcFiles1() {
        quietDropTables(DbSample.class, studentSample.class);
        createTables(studentSample.class, DbSample.class);
        String packageName="com.some";
        String prefix="Q";
        Map<String, Object> setting = new HashMap<String, Object>();
        setting.put(TableModelUtils.OPT_PACKAGE_NAME, packageName);// 包名
        setting.put(TableModelUtils.OPT_REMOVE_DEFAULT_IMPORTS, true); // 去除自带的imports
        setting.put(TableModelUtils.OPT_CLASS_ANNOTATION, false); // 类上的实体注解
        setting.put(TableModelUtils.OPT_CLASS_DEFINITION, "" + // 
                "public static class " + prefix + "$Class {\n" + //
                "\tpublic static final " + prefix + "$Class $class = new " + prefix + "$Class();\n\n" + //
                "\tpublic String toString(){\n" + //
                "\t\treturn \"$table\";\n" + //
                "\t}\n");// 类定义
        setting.put(TableModelUtils.OPT_FIELD_FLAGS, true); // 列名标记
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STATIC, false); // 列名标记为静态
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STYLE, "camel"); // 列名标记大写
        setting.put(TableModelUtils.OPT_FIELDS, false); // 属性
        TableModelUtils.db2JavaSrcFiles(ctx.getDataSource(), ctx.getDialect(), "c:/temp", setting);
        dropTables(DbSample.class, studentSample.class);
    }
    
    @Test
    public void testDb2PClassSrcFiles2() {
        quietDropTables(DbSample.class, studentSample.class);
        createTables(studentSample.class, DbSample.class);
        TableModelUtils.db2QClassSrcFiles(ctx.getDataSource(), ctx.getDialect(), "c:/temp", "com.some", "P");
        dropTables(DbSample.class, studentSample.class);
    }

}
