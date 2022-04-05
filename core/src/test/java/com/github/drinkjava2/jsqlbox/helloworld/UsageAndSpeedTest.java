package com.github.drinkjava2.jsqlbox.helloworld;

import static com.github.drinkjava2.jsqlbox.DB.TEMPLATE;
import static com.github.drinkjava2.jsqlbox.DB.bind;
import static com.github.drinkjava2.jsqlbox.DB.notNull;
import static com.github.drinkjava2.jsqlbox.DB.par;
import static com.github.drinkjava2.jsqlbox.DB.que;
import static com.github.drinkjava2.jsqlbox.DB.valuesQuestions;
import static com.github.drinkjava2.jsqlbox.DB.when;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.StatementConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdbpro.template.BasicSqlTemplate;
import com.github.drinkjava2.jdbpro.template.SqlTemplateEngine;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.springsrc.utils.ClassUtils;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Usage of different SQL style and speed test
 *
 * @author Yong Zhu
 * @since 1.7.0
 */
public class UsageAndSpeedTest {
    static long REPEAT_TIMES = 1;
    static boolean PRINT_TIMEUSED = false;

    protected HikariDataSource dataSource;

    @Before
    public void init() {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("sa");// change to your user & password
        dataSource.setPassword("");
        DbContext ctx = new DbContext(dataSource);
        DbContext.resetGlobalVariants();
        for (String ddl : ctx.getDialect().toDropAndCreateDDL(UserAR.class))
            try {
                ctx.exe(ddl);
            } catch (Exception e) {
            }
    }

    @After
    public void cleanUp() {
        dataSource.close();
    }

    @Test
    public void speedTest() throws Exception {
        try {
            PRINT_TIMEUSED = false;
            REPEAT_TIMES = 1;// warm up
            runTestMethods();
            PRINT_TIMEUSED = true;
            REPEAT_TIMES = 10;// Change to 10000 to do speed test
            Systemout.println("Speed test, compare method execute time for repeat " + REPEAT_TIMES + " times:");
            runTestMethods();
        } finally {
            PRINT_TIMEUSED = false;
            REPEAT_TIMES = 1;
        }
    }

    private void runTestMethods() throws Exception {
        runMethod("pureJdbc");
        runMethod("withConnection");
        runMethod("oldDbutilsMethods");
        runMethod("simpleMethods");
        runMethod("templateStyle");
        runMethod("dataMapperStyle");
        runMethod("activeRecordStyle");
        runMethod("activeRecordDefaultContext");
    }

    public void runMethod(String methodName) throws Exception {
        long start = System.currentTimeMillis();
        Method m = ClassUtils.getMethod(this.getClass(), methodName);
        m.invoke(this);
        long end = System.currentTimeMillis();
        String timeused = "" + (end - start) / 1000 + "." + (end - start) % 1000;
        if (PRINT_TIMEUSED)
            Systemout.println(String.format("%35s: %6s s", methodName, timeused));
    }

    @Table(name = "users")
    public static class UserPOJO {
        @Id
        String name;
        String address;

        public UserPOJO() {
        }

        public UserPOJO(String name, String address) {
            this.name = name;
            this.address = address;
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
    }

    @Table(name = UserAR.TABLE)
    public static class UserAR extends ActiveRecord<UserAR> {
        public static final String TABLE = "users";
        public static final String NAME = "name";
        public static final String ADDRESS = "address";

        @Id
        @Column(name = "name")
        String name;
        String address;
        Integer age;

        public UserAR() {
        }

        public UserAR(String name, String address) {
            this.name = name;
            this.address = address;
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

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

    }

    @Test
    public void pureJdbc() {
        for (int i = 0; i < REPEAT_TIMES; i++) {
            Connection conn = null;
            PreparedStatement pst = null;
            ResultSet rs = null;
            try {
                conn = dataSource.getConnection();
                pst = conn.prepareStatement("insert into users (name,address) values(?,?)");
                pst.setString(1, "Sam");
                pst.setString(2, "Canada");
                pst.execute();
                pst.close();

                pst = conn.prepareStatement("update users set name=?, address=?");
                pst.setString(1, "Tom");
                pst.setString(2, "China");
                pst.execute();
                pst.close();

                pst = conn.prepareStatement("select count(*) from users where name=? and address=?");
                pst.setString(1, "Tom");
                pst.setString(2, "China");
                rs = pst.executeQuery();
                rs.next();
                Assert.assertEquals(1L, rs.getLong(1));

                pst = conn.prepareStatement("delete from users where name=? or address=?");
                pst.setString(1, "Tom");
                pst.setString(2, "China");
                pst.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                if (rs != null)
                    try {
                        rs.close();
                    } catch (SQLException e) {
                    }
                if (pst != null)
                    try {
                        pst.close();
                    } catch (SQLException e) {
                    }
                if (conn != null)
                    try {
                        conn.close();
                    } catch (SQLException e) {
                    }
            }
        }
    }

    @Test
    public void withConnection() {
        DbContext ctx = new DbContext(dataSource);
        for (int i = 0; i < REPEAT_TIMES; i++) {
            Connection conn = null;
            try {
                conn = ctx.prepareConnection();
                ctx.execute(conn, "insert into users (name,address) values(?,?)", "Sam", "Canada");
                ctx.execute(conn, "update users set name=?, address=?", "Tom", "China");
                Assert.assertEquals(1L, ctx.queryForLongValue(conn,
                        "select count(*) from users where name=? and address=?", "Tom", "China"));
                ctx.execute(conn, "delete from users where name=? or address=?", "Tom", "China");
            } catch (SQLException e) {
                Systemout.println("Exception found: " + e.getMessage());
            } finally {
                try {
                    ctx.releaseConnection(conn);
                } catch (SQLException e) {
                    Systemout.println("Exception found: " + e.getMessage());
                }
            }
        }
    }

    @Test
    public void oldDbutilsMethods() {
        DbContext ctx = new DbContext(dataSource);
        for (int i = 0; i < REPEAT_TIMES; i++) {
            try {
                ctx.execute("insert into users (name,address) values(?,?)", "Sam", "Canada");
                ctx.execute("update users set name=?, address=?", "Tom", "China");
                Assert.assertEquals(1L,
                        ctx.queryForLongValue("select count(*) from users where name=? and address=?", "Tom", "China"));
                ctx.execute("delete from users where name=? or address=?", "Tom", "China");
            } catch (SQLException e) {
                Systemout.println("Exception found: " + e.getMessage());
            }
        }
    }

    @Test
    public void simpleMethods() {
        DbContext ctx = new DbContext(dataSource);
        for (int i = 0; i < REPEAT_TIMES; i++) {
            ctx.exe("insert into users (", //
                    notNull(" name ,", "Sam"), //
                    notNull(" someother ,", null), //
                    " address ", par("Canada"), //
                    ") ", valuesQuestions());
            ctx.exe("update users set name=?,address=?", par("Tom", "China"));
            Assert.assertEquals(1L, ctx.qryLongValue("select count(*) from users where name=? and address=?",
                    par("Tom", "China")));
            ctx.exe("delete from users where name=", que("Tom"), " or address=", que("China"));
        }
    }

    @Test
    public void templateStyle() {
        DbContext ctx2 = new DbContext(dataSource);
        Map<String, Object> paramMap = new HashMap<String, Object>();
        for (int i = 0; i < REPEAT_TIMES; i++) {
            UserAR sam = new UserAR("Sam", "Canada");
            UserAR tom = new UserAR("Tom", "China");
            paramMap.put("user", sam);
            ctx2.exe(TEMPLATE, "insert into users (name, address) values(#{user.name},:user.address)", paramMap);
            ctx2.exe(TEMPLATE, "update users set name=#{user.name}, address=:user.address", bind("user", tom));
            Assert.assertEquals(1L,
                    ctx2.qryLongValue(TEMPLATE, "select count(*) from users where name=#{name} and address=:addr",
                            bind("name", "Tom", "addr", "China")));
            ctx2.exe(TEMPLATE, "delete from users where "//
                    , " name=:name ", bind("name", "Tom")//
                    , " or address=#{address}", bind("address", "China")//
            );
        }
    }

    @Test
    public void dataMapperStyle() {
        DbContext ctx = new DbContext(dataSource);
        for (int i = 0; i < REPEAT_TIMES; i++) {
            UserPOJO user = new UserPOJO();
            user.setName("Sam");
            user.setAddress("Canada");
            ctx.entityInsert(user);
            user.setAddress("China");
            ctx.entityUpdateTry(user);
            UserPOJO sam2 = ctx.entityLoadById(UserPOJO.class, "Sam");
            ctx.entityDeleteTry(sam2);
        }
    }

    @Test
    public void activeRecordStyle() {
        DbContext ctx = new DbContext(dataSource);
        UserAR user = new UserAR();
        for (int i = 0; i < REPEAT_TIMES; i++) {
            user.setName("Sam");
            user.setAddress("Canada");
            user.insert(ctx);
            user.setAddress("China");
            user.update(ctx);
            UserAR user2 = new UserAR().loadById("Sam", ctx);
            user2.delete(ctx);
        }
    }

    @Test
    public void activeRecordDefaultContext() {
        DbContext ctx = new DbContext(dataSource);
        DbContext.setGlobalDbContext(ctx);// use global default context
        UserAR user = new UserAR();
        for (int i = 0; i < REPEAT_TIMES; i++) {
            user.setName("Sam");
            user.setAddress("Canada");
            user.insert();
            user.setAddress("China");
            user.update();
            UserAR user2 = ctx.entityLoadById(UserAR.class, "Sam");
            user2.delete();
        }
    }

    protected void BelowNotForSpeedTest_JustDoSomeUnitTest__________________() {
        // below methods are for unit test only, not for speed test
    }

    @Test
    public void useAnotherSqlTemplateEngine() {
        DbContext ctx = new DbContext(dataSource);
        SqlTemplateEngine engine = new BasicSqlTemplate("[", "]", true, true);//default engine, change to use [] for param
        UserAR user = new UserAR("Sam", "Canada");
        UserAR tom = new UserAR("Tom", "China");
        ctx.exe(engine, "insert into users (name, address) values([user.name], [user.address])", bind("user", user));
        ctx.exe(engine, "update users set name=[user.name], address=[user.address]", bind("user", tom));
        Assert.assertEquals(1L,
                ctx.qryLongValue(engine, "select count(*) from users where ${col}= [name] and address=[addr]",
                        bind("name", "Tom"), bind("addr", "China"), bind("$col", "name")));
        ctx.exe(engine, "delete from users where ${nm}='${t.name}' or address=:u.address", bind("u", tom), bind("$t", tom),
                bind("$nm", "name"));
    }

    @Test
    public void changeTemplateEngine() {
        DbContext ctx = new DbContext(dataSource);
        SqlTemplateEngine customizedEngine = new BasicSqlTemplate("[", "]", true, true);//customized sql engine
        UserAR user = new UserAR("Sam", "Canada");
        UserAR tom = new UserAR("Tom", "China");
        //default template
        ctx.exe(DB.TEMPLATE, "insert into users (name, address) values(#{user.name}, #{user.address})", bind("user", user));
        List<UserPOJO> lst = ctx.qryEntityList(TEMPLATE, UserPOJO.class, "select t.* from users t where t.name=:name", bind("name", "Sam"));
        Assert.assertEquals(1, lst.size());

        //below are customized engine
        ctx.exe(customizedEngine, "update users set name=[user.name], address=[user.address]", bind("user", tom));
        Assert.assertEquals(1L,
                ctx.qryLongValue(customizedEngine, "select count(*) from users where ${col}= [name] and address=[addr]",
                        bind("name", "Tom"), bind("addr", "China"), bind("$col", "name")));
        ctx.exe("delete from users where ${nm}='${t.name}' or address=:u.address", bind("u", tom), bind("$t", tom),
                bind("$nm", "name"), customizedEngine);
    }

    /**
     * Use const String can make SQL support Java Bean field refactoring
     */
    @Test
    public void supportRefactor() {
        DbContext ctx = new DbContext(dataSource);
        ctx.exe("insert into ", UserAR.TABLE, " ( ", //
                UserAR.NAME, ",", par("Sam"), //
                UserAR.ADDRESS, " ", par("Canada"), //
                ") ", valuesQuestions());
        ctx.exe("delete from users where ", //
                UserAR.NAME, "=", que("Sam"), //
                " or ", UserAR.ADDRESS, "=", que("Canada")//
        );
    }

    @Test
    public void activeRecordLoadByIdMap() {
        DbContext ctx = new DbContext(dataSource);
        UserAR user = new UserAR();
        user.useContext(ctx); // Use ctx as DbContext
        user.setName("Sam");
        user.setAddress("Canada");
        user.insert();
        user.setAddress("China");
        user.update();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "Sam");
        UserAR user2 = new UserAR().useContext(ctx).loadById(map);
        user2.delete(ctx);
    }

    @Test
    public void activeRecordLoadByQuery() {
        DbContext ctx = new DbContext(dataSource);
        UserAR user = new UserAR();
        user.useContext(ctx); // Use ctx as DbContext
        user.setName("Sam");
        user.setAddress("Canada");
        user.insert();
        UserAR user2 = new UserAR().useContext(ctx).loadById(user.getName());
        Assert.assertEquals("Canada", user2.getAddress());
    }

    @Test
    public void dataMapperCrudTest() {
        DbContext ctx = new DbContext(dataSource);
        // ctx.setAllowShowSQL(true);
        UserAR user = new UserAR();
        for (int i = 1; i <= 10; i++) {
            user.setName("Tom" + i);
            user.setAddress("China" + i);
            ctx.entityInsert(user);
        }
        user = new UserAR();
        user.setName("Tom8");
        ctx.entityLoad(user);
        Assert.assertEquals("China8", user.getAddress());

        user = ctx.entityLoadById(UserAR.class, "Tom7");
        Assert.assertEquals("China7", user.getAddress());

        user.setAddress("Canada");
        ctx.entityUpdateTry(user);
        Assert.assertEquals("Canada", ctx.entityLoadById(UserAR.class, "Tom7").getAddress());

        ctx.entityDeleteTry(user);
        ctx.entityDeleteTry(user, " or name=?", par("Tom2"));

        Assert.assertEquals(7, ctx.entityFind(UserAR.class, " where name>?", par("Tom1")).size());
    }

    @Test
    public void conditionsQuery() {
        DbContext ctx = new DbContext(dataSource);
        ctx.setAllowShowSQL(true);
        final String name = "Tom";
        final String age = null;
        final String address = "China";
        ctx.exe("insert into users (", //
                notNull(" name", name), //
                notNull(" ,age ", age), //
                " ,address ", par(address), //
                ") ", valuesQuestions());
        ctx.exe("update users set ", //
                notNull(" name", "=", "?, ", name), //
                notNull(" age=?,", age), //
                " address=? ", par(address), //
                " where name is not null"//
        );
        Assert.assertEquals(1L, ctx.qryLongValue(//
                "select count(*) from users where 1=1 ", //
                notNull(" and name=? ", name), //
                "Tom".equals(name) ? ctx.prepare(" and name=?  ", par(name)) : "", //
                "China".equals(address) ? new Object[]{" and address=  ", que(address)} : "",//
                " order by name"
        ));
        ctx.exe("delete from users");
    }

    @Test
    public void conditionsQuery2() { //use "when" method to test
        DbContext ctx = new DbContext(dataSource);
        ctx.setAllowShowSQL(true);
        final String name = "Tom";
        final String age = null;
        final String address = "China";
        ctx.exe("insert into users (", //
                " name", par(name), //
                when(age != null, " ,age ", par(age)), //
                " ,address ", par(address), //
                ") ", valuesQuestions());
        ctx.exe("update users set ", //
                " name=", que(name), //
                when(age != null, ", age=", que(age)), //
                when(address != null, ", address=", que(address)), //
                " where name is not null"
        );
        Assert.assertEquals(1L, ctx.qryLongValue(//
                "select count(*) from users where 1=1 ", //
                when(name != null, " and name=", que(name)),//
                when("Tom".equals(name), " and name=", que(name)),//
                when("China".equals(address), " and address=", que(address)),//
                " order by name"
        ));
        ctx.exe("delete from users");
    }

    @Test
    public void timeoutQuery() {//timeout是在初始化DbContext(或它的父类QueryRunner)时设定，不可以作为参数传递
        DbContext ctx = new DbContext(dataSource, new StatementConfiguration.Builder().queryTimeout(1).build());
        ctx.setAllowShowSQL(true);
        final String name = "Tom";
        final String age = null;
        final String address = "China";
        ctx.exe("insert into users (", //
                " name", par(name), //
                when(age != null, " ,age ", par(age)), //
                " ,address ", par(address), //
                ") ", valuesQuestions());
        try {
            ctx.qryMapList("select * from users");
            ctx.qryMap("select * from users");
            ctx.qryString("select name from users where name=?", par(name));
            Systemout.println();
        } catch (Exception e) {
            if (e instanceof SQLException) {
                //test on PGSQL
                if (e.getMessage().indexOf("cancelled") != -1 || e.getMessage().indexOf("timeout") != -1) {
                    Systemout.println("execute sql timeout...");
                }
            }

        }
    }
}
