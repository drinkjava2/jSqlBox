/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects.function;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.config.JdialectsTestBase;
import com.github.drinkjava2.jdialects.entity.RoleTB;
import com.github.drinkjava2.jdialects.entity.UserTB;
import com.github.drinkjava2.jdialects.function.AnnotationTest.EntityForAll;
import com.github.drinkjava2.jdialects.function.AnnotationTest.EntityForMySql;
import com.github.drinkjava2.jdialects.function.AnnotationTest.EntityForOracle;

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

		if (guessedDialect.ddlFeatures.supportBasicOrPooledSequence()) {
			testCreateAndDropDatabase(TableModelUtils.entity2Models(EntityForAll.class, EntityForOracle.class));
			//Systemout.println(DebugUtils.getTableModelDebugInfo(TableModelUtils.entity2Model(EntityForOracle.class)));
		}
		if (!guessedDialect.ddlFeatures.supportBasicOrPooledSequence()) {
			testCreateAndDropDatabase(TableModelUtils.entity2Models(EntityForAll.class, EntityForMySql.class));
			//Systemout.println(DebugUtils.getTableModelDebugInfo(TableModelUtils.entity2Model(EntityForMySql.class)));
		}
	}
}
