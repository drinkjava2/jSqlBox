/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects;

import java.sql.SQLException;

import org.junit.Test;

import com.github.drinkjava2.jdialects.config.JdialectsTestBase;
import com.github.drinkjava2.jdialects.entity.RoleTB;
import com.github.drinkjava2.jdialects.entity.UserTB;

public class ToExcelTest extends JdialectsTestBase {
    
    private static boolean isWindows() {
    String os = System.getProperty("os.name");  
    return os.toLowerCase().startsWith("win");
    }



    @Test
    public void doTestPackage2Excel() {
        if (isWindows())
            TableModelUtils.entityPackage2Excel("com.github.drinkjava2.jdialects.entity", "c:/tmp/jsqlbox_test_packageOutput.csv");
    }

    @Test
    public void doTestEntity2Excel() {
        if (isWindows())
        TableModelUtils.entity2Excel("c:/tmp/jsqlbox_test_entitiesOutput.csv", RoleTB.class, UserTB.class);
    }

    @Test
    public void doTestDb2Excel() throws SQLException {
        if (isWindows())
        TableModelUtils.db2Excel(ds.getConnection(), Dialect.MySQL57Dialect, "c:/tmp/jsqlbox_test_dbOutput.csv");
    }
}
