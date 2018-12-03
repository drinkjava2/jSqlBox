/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects.function;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.DebugUtils;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.config.JdialectsTestBase;
import com.github.drinkjava2.jdialects.entity.RoleTB;
import com.github.drinkjava2.jdialects.entity.UserTB;
import com.github.drinkjava2.jdialects.function.AnnotationTest.Entity1;
import com.github.drinkjava2.jdialects.function.AnnotationTest.Entity2;

/**
 * Annotation Test
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class EntityAnnotationTest extends JdialectsTestBase {

	@Test
	public void ddlOutTest() {
		String[] dropAndCreateDDL = Dialect.H2Dialect
				.toCreateDDL(TableModelUtils.entity2Models(UserTB.class, RoleTB.class));
		for (String ddl : dropAndCreateDDL)
			Systemout.println(ddl);

		testCreateAndDropDatabase(TableModelUtils.entity2Models(Entity1.class, Entity2.class));
		Systemout.println(DebugUtils.getTableModelDebugInfo(TableModelUtils.entity2Model(Entity2.class)));
	}
}
