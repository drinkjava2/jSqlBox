package com.github.drinkjava2.functionstest.jdialects;

import org.junit.Test;

import com.github.drinkjava2.jdialects.TableModelUtilsOfEntity;
import com.github.drinkjava2.jdialects.TableModelUtilsOfJavaSrc;
import com.github.drinkjava2.jdialects.annotation.jdia.FKey;
import com.github.drinkjava2.jdialects.annotation.jdia.FKey1;
import com.github.drinkjava2.jdialects.annotation.jdia.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

/**
 * Unit test for TableModelUtilsOfJavaSrc.java
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */

public class TableModelUtilsOfJavaSrcTest {

	@Table(name = "entity1")
	@FKey(name = "fkey1", ddl = false, columns = { "id", "name" }, refs = { "entity2", "field1", "field2" })
	@FKey1(columns = { "id", "name" }, refs = { "entity3", "field1", "field2" })
	public class Entity1 extends ActiveRecord<Entity1> {
		@Id
		private Integer id;

		@Id
		@Column(length = 10)
		private String name;

		@Column(name = "cust_id")
		@SingleFKey(refs = { "table2", "id" })
		private Integer custId;

		Entity1 demoEntity;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getCustId() {
			return custId;
		}

		public void setCustId(Integer custId) {
			this.custId = custId;
		}

		public Entity1 getDemoEntity() {
			return demoEntity;
		}

		public void setDemoEntity(Entity1 demoEntity) {
			this.demoEntity = demoEntity;
		}
	}

	@Test
	public void modelToJavaSrcTest() {
		TableModel model = TableModelUtilsOfEntity.entity2ReadOnlyModel(Entity1.class);
		System.out.println(TableModelUtilsOfJavaSrc.modelToJavaSourceCode(model, true, true, "somepackage"));
	}
}
