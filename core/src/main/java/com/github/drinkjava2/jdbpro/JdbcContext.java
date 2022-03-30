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
package com.github.drinkjava2.jdbpro;

import java.lang.reflect.Method;

import javax.sql.DataSource;

import org.apache.commons.dbutils.StatementConfiguration;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.converter.BasicJavaConverter;
import com.github.drinkjava2.jdialects.converter.JavaConverter;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jdialects.springsrc.utils.ClassUtils;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;

/**
 * DbContext is extended from DbPro, DbPro is extended from QueryRunner, by this
 * way DbContext have all JDBC methods of QueryRunner and DbPro. <br/>
 * 
 * As a ORM tool, DbContext focus on ORM methods like entity bean's CRUD methods
 * and EntityNet methods.
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
public class JdbcContext extends DbPro {// NOSONAR

    protected static Object globalNextAuditorGetter = null;

    protected static JdbcContext globalDbContext = new JdbcContext(); // this is a empty JdbcContext

    protected Object auditorGetter = globalNextAuditorGetter;

    public JdbcContext() {
        super();
    }

    public JdbcContext(DataSource ds) {
        super(ds);
    }

    public JdbcContext(DataSource ds, Dialect dialect) {
        super(ds, dialect);
    }

    public JdbcContext(DataSource ds, StatementConfiguration stmtConfig) {
        super(ds, stmtConfig);
    }

    public JdbcContext(DataSource ds, Dialect dialect, StatementConfiguration stmtConfig) {
        super(ds, dialect, stmtConfig);
    }

    protected void miscMethods______________________________() {// NOSONAR
    }

    /** Reset all global SqlBox variants to default values */
    public static void resetGlobalVariants() {
        setGlobalNextAllowShowSql(false);
        setGlobalNextMasterSlaveOption(SqlOption.USE_AUTO);
        setGlobalNextConnectionManager(TinyTxConnectionManager.instance());
        setGlobalNextSqlHandlers((SqlHandler[]) null);
        setGlobalNextBatchSize(300);
        setGlobalNextDialect(null);
        setGlobalNextAuditorGetter(null);
        globalDbContext = new JdbcContext();
        Dialect.setGlobalJdbcTypeConverter(new BasicJavaConverter());
    }

    /** Shortcut method equal to getGlobalDbContext() */
    public static JdbcContext gctx() {
        return JdbcContext.globalDbContext;
    }

    /** Get the global static DbContext instance */
    public static JdbcContext getGlobalDbContext() {
        return JdbcContext.globalDbContext;
    }

    private Method methodOfGetCurrentAuditor = null;

    /** For &#064;CreatedBy and &#064;LastModifiedBy, get current auditor */
    public Object getCurrentAuditor() {
        DbProException.assertNotNull(auditorGetter, "Can not call getCurrentAuditor() when auditorGetter is null.");
        Object result = null;
        if (methodOfGetCurrentAuditor == null) {
            methodOfGetCurrentAuditor = ClassUtils.getMethod(auditorGetter.getClass(), "getCurrentAuditor");
        }
        try {
            result = methodOfGetCurrentAuditor.invoke(auditorGetter);
        } catch (Exception e) {
            throw new DialectException("Fail to call auditorGetter's getCurrentAuditor method. ", e);
        }
        return result;
    }

    /**
     * Override DbPro's dealOneSqlItem method to deal DbContext's SqlItem
     */
    @Override
    protected boolean dealOneSqlItem(boolean iXxxStyle, PreparedSQL ps, Object item) {// NOSONAR
        if (super.dealOneSqlItem(iXxxStyle, ps, item)) {
            return true; // if super class DbPro can deal it, let it do
        }
        if (item instanceof SqlOption) {
            if (SqlOption.IGNORE_EMPTY.equals(item)) {
                ps.setIgnoreEmpty(true);
            } else if (SqlOption.IGNORE_NULL.equals(item)) {
                ps.setIgnoreNull(true);
            } else {
                return false;
            }
        } else if (item instanceof SqlItem) {
            SqlItem sqItem = (SqlItem) item;
            SqlOption sqlItemType = sqItem.getType();
            if (SqlOption.GIVE.equals(sqlItemType)) {
                Object[] o = ((SqlItem) item).getParameters();
                String[] s = new String[o.length];
                for (int i = 0; i < o.length; i++) {
                    s[i] = (String) o[i];
                }
                ps.addGives(s);
            } else if (SqlOption.GIVE_BOTH.equals(sqlItemType)) {
                Object[] a = ((SqlItem) item).getParameters();
                ps.addGives(new String[]{(String) a[0], (String) a[1]});
                ps.addGives(new String[]{(String) a[1], (String) a[0]});
            } else {
                return SqlOption.TAIL.equals(sqlItemType);
            }
        } else {
            return false;
        }
        return true;
    }

    protected void dialectShortcutMethods__________________________() {// NOSONAR
    }

    /** Shortcut call to dialect.pagin method */
    public String pagin(int pageNumber, int pageSize, String sql) {
        assertDialectNotNull();
        return dialect.pagin(pageNumber, pageSize, sql);
    }

    /** Shortcut call to dialect.trans method */
    public String trans(String sql) {
        assertDialectNotNull();
        return dialect.trans(sql);
    }

    /** Shortcut call to dialect.paginAndTrans method */
    public String paginAndTrans(int pageNumber, int pageSize, String sql) {
        assertDialectNotNull();
        return dialect.paginAndTrans(pageNumber, pageSize, sql);
    }

    /** Shortcut call to dialect.toCreateDDL method */
    public String[] toCreateDDL(Class<?>... entityClasses) {
        assertDialectNotNull();
        return dialect.toCreateDDL(entityClasses);
    }

    /** Shortcut call to dialect.toDropDDL method */
    public String[] toDropDDL(Class<?>... entityClasses) {
        assertDialectNotNull();
        return dialect.toDropDDL(entityClasses);
    }

    /** Shortcut call to dialect.toDropAndCreateDDL method */
    public String[] toDropAndCreateDDL(Class<?>... entityClasses) {
        assertDialectNotNull();
        return dialect.toDropAndCreateDDL(entityClasses);
    }

    /** Shortcut call to dialect.toCreateDDL method */
    public String[] toCreateDDL(TableModel... tables) {
        assertDialectNotNull();
        return dialect.toCreateDDL(tables);
    }

    /** Shortcut call to dialect.toDropDDL method */
    public String[] toDropDDL(TableModel... tables) {
        assertDialectNotNull();
        return dialect.toDropDDL(tables);
    }

    /** Shortcut call to dialect.toDropAndCreateDDL method */
    public String[] toDropAndCreateDDL(TableModel... tables) {
        assertDialectNotNull();
        return dialect.toDropAndCreateDDL(tables);
    }

    /** Execute DDL stored in a String array */
    public void executeDDL(String[] sqls) {
        for (String sql : sqls) {
            exe(sql);
        }
    }

    private void assertDialectNotNull() {
        if (dialect == null) {
            throw new DbProException("Try use a dialect method but dialect is null");
        }
    }

    // static global variants setting
    protected void staticGlobalSetMethods______________________() {// NOSONAR
    }

    public static void setGlobalNextAuditorGetter(Object globalNextAuditorGetter) {
        JdbcContext.globalNextAuditorGetter = globalNextAuditorGetter;
    }

    // ========= Global variants setting=======

    public static void setGlobalDbContext(JdbcContext globalDbContext) {
        JdbcContext.globalDbContext = globalDbContext;
    }

    public static void setGlobalJdbcTypeConverter(JavaConverter jdbcTypeConverter) {
        Dialect.setGlobalJdbcTypeConverter(jdbcTypeConverter);
    }

    // =========getter & setter ======= 

    public Object getAuditorGetter() {
        return auditorGetter;
    }

    public void setAuditorGetter(Object auditorGetter) {
        this.auditorGetter = auditorGetter;
    }
}