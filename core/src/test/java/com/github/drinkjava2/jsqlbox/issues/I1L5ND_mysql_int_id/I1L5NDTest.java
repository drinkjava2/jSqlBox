package com.github.drinkjava2.jsqlbox.issues.I1L5ND_mysql_int_id;

import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jsqlbox.issues.IYDU7_orm_demo.Ademo;

/**
 * This is a demo to how to use ORM query
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class I1L5NDTest extends TestBase {

	public static class Entity1 extends ActiveRecord<Entity1> {
		@Id
		int id;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		} 
	}

	{
		regTables(Entity1.class);
	}

 
	@Test
	public void testEntity1() { 
		new Ademo().putValues("a1", "atext1", "b1").insert();
	}

	 

}