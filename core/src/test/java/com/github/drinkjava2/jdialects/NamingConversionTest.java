/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Yong Z.
 * @since 5.0.10
 *
 */
public class NamingConversionTest {

    @Test
    public void doTest() {
        Assert.assertEquals("naming_conversion_test", NamingConversion.LOWER_CASE_UNDERSCORE.getTableName(NamingConversionTest.class));
        Assert.assertEquals("order_price", NamingConversion.LOWER_CASE_UNDERSCORE.getColumnName("OrderPrice"));
        Assert.assertEquals("NAMING_CONVERSION_TEST", NamingConversion.UPPER_CASE_UNDERSCORE.getTableName(NamingConversionTest.class));
        Assert.assertEquals("ORDER_PRICE", NamingConversion.UPPER_CASE_UNDERSCORE.getColumnName("OrderPrice"));
    }
}
