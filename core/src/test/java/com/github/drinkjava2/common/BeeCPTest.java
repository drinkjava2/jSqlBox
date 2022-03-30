package com.github.drinkjava2.common;

import static com.github.drinkjava2.jsqlbox.DB.que;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveEntity;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.zaxxer.hikari.HikariDataSource;

import cn.beecp.BeeDataSource;
import cn.beecp.BeeDataSourceConfig;

public class BeeCPTest implements ActiveEntity<BeeCPTest> {
    @Id
    @UUID25
    private String id;

    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public BeeCPTest setName(String name) {
        this.name = name;
        return this;
    }

    public static HikariDataSource createHikariPool() {
        HikariDataSource ds = new HikariDataSource();
        ds.addDataSourceProperty("cachePrepStmts", true);
        ds.addDataSourceProperty("prepStmtCacheSize", 250);
        ds.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        ds.addDataSourceProperty("useServerPrepStmts", true);
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/jsqlboxtest?rewriteBatchedStatements=true&useSSL=false&serverTimezone=UTC");
        ds.setUsername("root");
        ds.setPassword("root888");
        ds.setMaximumPoolSize(3);
        ds.setConnectionTimeout(5000);
        return ds;
    }

    public static BeeDataSource createBeeCP() {
        BeeDataSourceConfig config = new BeeDataSourceConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/jsqlboxtest?rewriteBatchedStatements=true&useSSL=false&serverTimezone=UTC");
        config.setUsername("root");
        config.setPassword("root888");
        config.setForceCloseUsingOnClear(true);
        config.setDelayTimeForNextClear(0);
        BeeDataSource ds = new BeeDataSource(config);
        return ds;
    }

    @Test
    public void test() throws SQLException {
        for (int i = 1; i <= 1; i++) { //change to 100 if want test pool leak 
            Systemout.println("==================== " + i + " ================");
            test1(createBeeCP());
        }
    }

    private static void test1(DataSource ds) throws SQLException {
        DbContext ctx = new DbContext(ds);
        ctx.setAllowShowSQL(true);
        DbContext.setGlobalDbContext(ctx);
        ctx.quiteExecute(ctx.toDropAndCreateDDL(BeeCPTest.class));
        for (int i = 0; i < 10; i++) {
            BeeCPTest h = new BeeCPTest().setName("Foo").insert().putField("name", "Hello jSqlBox").update();
            String s = DB.qryString("select name from BeeCPTest where name like", que("H%"), " or name=", que("1"), " or name =", que("2"));
            Systemout.println(s);
            h.delete();
        }
        ds.getConnection(); //这一行不关闭连接
        ctx.executeDDL(ctx.toDropDDL(BeeCPTest.class));
        if (ds instanceof HikariDataSource) {
            ((HikariDataSource) ds).close();
        } else if (ds instanceof BeeDataSource) {
            ((BeeDataSource) ds).close();//这一行会卡死
        }
    }

}