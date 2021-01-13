package com.github.drinkjava2.demo.jsqlbox1;

import static com.github.drinkjava2.jsqlbox.DB.par;
import static com.github.drinkjava2.jsqlbox.DB.valuesQuestions;

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

        TableModelUtils.db2QClassSrcFiles(ctx.getDataSource(), ctx.getDialect(), "c:/temp", "com.github.drinkjava2.demo.jsqlbox1", "Q");

        QUserDemo u = QUserDemo.instance;
        for (int i = 1; i <= 10; i++) {
            DB.exe("insert into ", u, " (", //
                    u.id, ",", par(i), //
                    u.user_name, ",", par("Foo" + i), //
                    u.user_age, par(i), //
                    ")", valuesQuestions());
        }
        Assert.assertEquals(10, ctx.qryLongValue("select count(*) from ", u));
        ds.close();
    }
}
