/*
 * Copyright 2016 the original author or authors. 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 */
package com.github.drinkjava2.jdialects.function;

import org.h2.engine.User;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.DebugUtils;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.TypeUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.FKey;
import com.github.drinkjava2.jdialects.annotation.jdia.FKey1;
import com.github.drinkjava2.jdialects.annotation.jdia.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.jdia.SingleIndex;
import com.github.drinkjava2.jdialects.annotation.jdia.SingleUnique;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID36;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Entity;
import com.github.drinkjava2.jdialects.annotation.jpa.GeneratedValue;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Index;
import com.github.drinkjava2.jdialects.annotation.jpa.SequenceGenerator;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.annotation.jpa.TableGenerator;
import com.github.drinkjava2.jdialects.annotation.jpa.Transient;
import com.github.drinkjava2.jdialects.annotation.jpa.UniqueConstraint;
import com.github.drinkjava2.jdialects.config.JdialectsTestBase;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * Annotation Test
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */
public class AnnotationTest extends JdialectsTestBase {

	public static class Entity1 {

		public String field1;
		public String field2;

		public String getField1() {
			return field1;
		}

		public void setField1(String field1) {
			this.field1 = field1;
		}

		public String getField2() {
			return field2;
		}

		public void setField2(String field2) {
			this.field2 = field2;
		}
	}

	@Entity
	@Table(name = "testpo", //
			uniqueConstraints = { @UniqueConstraint(columnNames = { "field1" }),
					@UniqueConstraint(name = "unique_cons2", columnNames = { "field1", "field2" }) }, //
			indexes = { @Index(columnList = "field1,field2", unique = true),
					@Index(name = "index_cons2", columnList = "field1,field2", unique = false) }//
	)
	@SequenceGenerator(name = "seqID1", sequenceName = "seqName1", initialValue = 1, allocationSize = 10)
	@TableGenerator(name = "tableID1", table = "table1", pkColumnName = "pkCol1", valueColumnName = "vcol1", pkColumnValue = "pkcolval1", initialValue = 2, allocationSize = 20)
	@FKey(name = "fkey1", ddl = true, columns = { "field1", "field2" }, refs = { "Entity1", "field1", "field2" })
	@FKey1(columns = { "field2", "field3" }, refs = { "Entity1", "field1", "field2" })
	public static class Entity2 {
		@SequenceGenerator(name = "seqID2", sequenceName = "seqName2", initialValue = 2, allocationSize = 20)
		@TableGenerator(name = "tableID2", table = "table2", pkColumnName = "pkCol1", valueColumnName = "vcol1", pkColumnValue = "pkcolval1", initialValue = 2, allocationSize = 20)
		@Id
		@Column(columnDefinition = TypeUtils.VARCHAR, length = 20)
		public String field1;

		@Column(name = "field2", nullable = false, columnDefinition = TypeUtils.BIGINT)
		public String field2;

		@GeneratedValue(strategy = GenerationType.TABLE, generator = "CUST_GEN")
		@Column(name = "field3", nullable = false, columnDefinition = TypeUtils.BIGINT)
		@SingleFKey(name = "singleFkey1", ddl = true, refs = { "Entity1", "field1" })
		@SingleIndex
		@SingleUnique
		public Integer field3;

		@Transient
		public Entity1 field4;
		
		@Transient
		public User user;

		@Column()
		@UUID36
		public String field5;// no columnDefinition

		public Float field6;

		public Double field7;

		public static void config(TableModel tableModel) {
			tableModel.getColumn("field7").setColumnName("changedfield7");
			tableModel.column("newField9").STRING(10);
		}

		public String getField1() {
			return field1;
		}

		public void setField1(String field1) {
			this.field1 = field1;
		}

		public String getField2() {
			return field2;
		}

		public void setField2(String field2) {
			this.field2 = field2;
		}

		public Integer getField3() {
			return field3;
		}

		public void setField3(Integer field3) {
			this.field3 = field3;
		}

		public Entity1 getField4() {
			return field4;
		}

		public void setField4(Entity1 field4) {
			this.field4 = field4;
		}

		public String getField5() {
			return field5;
		}

		public void setField5(String field5) {
			this.field5 = field5;
		}

		public Float getField6() {
			return field6;
		}

		public void setField6(Float field6) {
			this.field6 = field6;
		}

		public Double getField7() {
			return field7;
		}

		public void setField7(Double field7) {
			this.field7 = field7;
		}
	}

	@Test
	public void ddlOutTest() {
		String[] dropAndCreateDDL = Dialect.H2Dialect
				.toCreateDDL(TableModelUtils.entity2Models(Entity1.class, Entity2.class));
		for (String ddl : dropAndCreateDDL)
			Systemout.println(ddl);

		testCreateAndDropDatabase(TableModelUtils.entity2Models(Entity1.class, Entity2.class));
		
		Systemout.println(DebugUtils.getTableModelDebugInfo(TableModelUtils.entity2Model(Entity2.class)));
	}
}
