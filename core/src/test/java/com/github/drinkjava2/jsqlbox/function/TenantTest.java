package com.github.drinkjava2.jsqlbox.function;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdbpro.ImprovedQueryRunner;
import com.github.drinkjava2.jdbpro.TenantGetter;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.annotation.jdia.PKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID26;
import com.github.drinkjava2.jsqlbox.ActiveEntity;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * This is unit test for test @Version annotation
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public class TenantTest extends TestBase {
    {
        regTables(TentantDemo.class);
    }

    public static class CustomTenantGetter implements TenantGetter {

        @Override
        public ImprovedQueryRunner getTenant() {
            return DB.gctx();
        }

    }

    @Test
    public void doTest() {
        new TentantDemo().insert();
        ctx = new DbContext();
        ctx.setDialect(Dialect.MySQL57InnoDBDialect);
        ctx.setTenantGetter(new CustomTenantGetter());
        int i = ctx.qryIntValue("select count(*) from TentantDemo");
        Assert.assertEquals(1, i);
    }

    public static class TentantDemo implements ActiveEntity<TentantDemo> {
        @PKey
        @UUID26
        private String id;

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

}
