package com.github.drinkjava2.demo.jsqlbox2;

import static com.github.drinkjava2.jsqlbox.DB.par;
import static com.github.drinkjava2.jsqlbox.DB.valuesQuestions;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import com.github.drinkjava2.User;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This demo shows use QClass with jSqlBox
 */
public class QClassDemo {

    public static void main(String[] args) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.h2.Driver"); // H2 Memory database
        ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
        ds.setUsername("sa");
        ds.setPassword("");

        DbContext ctx = new DbContext(ds);
        ctx.setAllowShowSQL(true);
        DbContext.setGlobalDbContext(ctx);
        ctx.quiteExecute(ctx.toCreateDDL(User.class));

        //以下是手工定制生成的静态类
        String packageName = "com.github.drinkjava2.demo.jsqlbox2";
        String prefix = "Q";
        Map<String, Object> setting = new HashMap<String, Object>();
        setting.put(TableModelUtils.OPT_PACKAGE_NAME, packageName);// 包名
        setting.put(TableModelUtils.OPT_REMOVE_DEFAULT_IMPORTS, true); // 去除自带的imports
        setting.put(TableModelUtils.OPT_CLASS_ANNOTATION, false); // 类上的实体注解
        setting.put(TableModelUtils.OPT_CLASS_DEFINITION, "" + // 
                "public class " + prefix + "$Class {\n" + //
                "\tpublic static final " + prefix + "$Class $class = new " + prefix + "$Class();\n\n" + //
                "\tpublic String toString(){\n" + //
                "\t\treturn \"$table\";\n" + //
                "\t}\n");// 类定义
        setting.put(TableModelUtils.OPT_FIELD_FLAGS, true); // 列名标记
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STATIC, false); // 列名标记为静态
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STYLE, "camel"); // 列名标记可以有camel, upper, lower, normal几种 
        setting.put(TableModelUtils.OPT_FIELDS, false); // JavaBean属性不需要生成
        TableModelUtils.db2JavaSrcFiles(ctx.getDataSource(), ctx.getDialect(), "c:/temp", setting);

        QUserDemo u = QUserDemo.userDemo;
        for (int i = 1; i <= 10; i++) {
            DB.exe("insert into ", u, " (", //
                    u.id, ",", par(i), //
                    u.userName, ",", par("Foo" + i), //
                    u.userAge, par(i), //
                    ")", valuesQuestions());
        }
        Assert.assertEquals(10, ctx.qryLongValue("select count(*) from ", u));
        ds.close();
    }
}
