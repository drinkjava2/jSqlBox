package com.github.drinkjava2.jsqlboxdemo;

import static com.github.drinkjava2.jsqlbox.DB.par;
import static com.github.drinkjava2.jsqlbox.DB.valuesQuestions;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.TableModelUtilsOfEntity;
import com.github.drinkjava2.jdialects.TableModelUtilsOfJavaSrc;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This demo shows use QClass with jSqlBox
 */
public class PClassDemo {

    public static class PUser {
        
        public static final PUser pUser =new PUser();
        
        public final String table = "user_table";

        public final String id = "id";

        public final String userAge = "user_age";

        public final String userName = "user_name";

        public String toString() {
            return "user_table";
        }
    }

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

        Map<String, Object> setting = new HashMap<String, Object>();
        setting.put(TableModelUtils.OPT_PACKAGE_NAME, "somepackage");// 包名
        setting.put(TableModelUtils.OPT_REMOVE_DEFAULT_IMPORTS, true); // 去除自带的imports
        setting.put(TableModelUtils.OPT_CLASS_ANNOTATION, false); // 类上的实体注解
        setting.put(TableModelUtils.OPT_CLASS_DEFINITION, "public static class P$1");// 类定义
        setting.put(TableModelUtils.OPT_FIELD_FLAGS, true); // 列名标记
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STATIC, false); // 列名标记为静态
        setting.put(TableModelUtils.OPT_FIELD_FLAGS_STYLE, "lower"); // 列名格式, lower小写，upper大写camel驼峰
        setting.put(TableModelUtils.OPT_FIELDS, false); // 属性
        TableModel model = TableModelUtilsOfEntity.entity2ReadOnlyModel(User.class);
        System.out.println(TableModelUtilsOfJavaSrc.modelToJavaSourceCode(model, setting));

        PUser u = PUser.pUser;;
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
