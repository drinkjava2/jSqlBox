package com.github.drinkjava2.jsqlbox.function.jdialects.typemapping;

import java.util.Date;

import org.junit.Test;

import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Temporal;
import com.github.drinkjava2.jdialects.annotation.jpa.TemporalType;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.config.TestBase;

public class DatetimeTimestampTest extends TestBase {

    public static class TestEntity extends ActiveRecord<TestEntity> {

        @Temporal(TemporalType.TIMESTAMP)
        java.util.Date d1;

        @Column(columnDefinition = "datetime")
        java.util.Date d2;

        public java.util.Date getD1() {
            return d1;
        }

        public void setD1(java.util.Date d1) {
            this.d1 = d1;
        }

        public java.util.Date getD2() {
            return d2;
        }

        public void setD2(java.util.Date d2) {
            this.d2 = d2;
        }
    }

    @Test
    public void doTest() {
        TableModel m= TableModelUtils.entity2Model(TestEntity.class);
        System.out.println(m.getDebugInfo());
        
        createAndRegTables(TestEntity.class);

        TestEntity t = new TestEntity();
        t.setD1(new Date());
        t.setD2(new Date());
        t.insert();
        t.setD1(new Date());
        t.setD2(new Date());
        t.insert();
        System.out.println(DB.qryString("select d1 from TestEntity"));
        System.out.println(DB.qryString("select d2 from TestEntity"));
    }

}