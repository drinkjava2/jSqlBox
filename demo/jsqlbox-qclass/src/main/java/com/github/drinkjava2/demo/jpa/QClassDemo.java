package com.github.drinkjava2.demo.jpa;

import static com.github.drinkjava2.jsqlbox.DB.par;
import static com.github.drinkjava2.jsqlbox.DB.valuesQuestions;

import org.junit.Assert;

import com.github.drinkjava2.QUser;
import com.github.drinkjava2.User;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jdbpro.SqlItemHandler;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.zaxxer.hikari.HikariDataSource;

/**
 * This demo shows use QClass with jSqlBox
 */
public class QClassDemo {

    @SuppressWarnings("rawtypes")
    public static class QClassSqlItemHandle implements SqlItemHandler {

        @Override
        public boolean handle(PreparedSQL ps, Object item) {
            if (item instanceof EntityPathBase) {
                Class c = (Class) ((EntityPathBase) item).getAnnotatedElement();
                TableModel t = TableModelUtils.entity2ReadOnlyModel(c);
                ps.addSql(t.getTableName());
                return true;
            }
            if (item instanceof Path) {
                Path p = (Path) item;
                Class c = (Class) p.getRoot().getAnnotatedElement();
                TableModel t = TableModelUtils.entity2ReadOnlyModel(c);
                String s = p.getAnnotatedElement().toString();
                s = StrUtils.substringAfterLast(s, ".");
                ColumnModel col = t.getColumnByFieldName(s);
                ps.addSql(col.getColumnName());
                return true;
            }
            return false;
        }

    }

    public static void main(String[] args) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.h2.Driver"); // H2 Memory database
        ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
        ds.setUsername("sa");
        ds.setPassword("");

        DbContext ctx = new DbContext(ds);
        ctx.setSqlItemHandler(new QClassSqlItemHandle());
        ctx.setAllowShowSQL(true);
        DbContext.setGlobalDbContext(ctx);
        ctx.quiteExecute(ctx.toCreateDDL(User.class));

        QUser u = QUser.user;
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
