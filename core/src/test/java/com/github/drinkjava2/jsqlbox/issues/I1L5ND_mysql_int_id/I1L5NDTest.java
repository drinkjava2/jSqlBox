package com.github.drinkjava2.jsqlbox.issues.I1L5ND_mysql_int_id;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.annotation.jdia.IdentityId;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * This unit test for MySql5 int type id issue I1L5ND
 * 
 * @author Yong Zhu
 * @since 4.0.7
 */
/**
 * @author Yong Zhu
 * @since 1.7.0
 */
public class I1L5NDTest extends TestBase {
	{
		regTables(Entity1.class, Entity2.class, Entity3.class);
	}

	/** test Integer primary key */
	public static class Entity1 extends ActiveRecord<Entity1> {
		@Id
		Integer id;

		public Integer getId() {
			return id;
		}

		public Entity1 setId(Integer id) {
			this.id = id;
			return this;
		}
	}

	@Test
	public void testEntity1() {
		if (!ctx.getDialect().isMySqlFamily())
			return;
		Systemout.println("testEntity1");
		Entity1 e = new Entity1().putField("id", 1).insert();
		Systemout.println(e.id);
		Object o = ctx.iQueryForIntValue("select id from entity1");
		Assert.assertEquals(1, o);
		Assert.assertEquals(Integer.class, o.getClass());
	}

	/** test Integer identity primary key */
	public static class Entity2 extends ActiveRecord<Entity2> {
		@Id
		@IdentityId
		Integer id;

		String name;

		public Integer getId() {
			return id;
		}

		public Entity2 setId(Integer id) {
			this.id = id;
			return this;
		}

		public String getName() {
			return name;
		}

		public Entity2 setName(String name) {
			this.name = name;
			return this;
		}
	}

	@Test
	public void testEntity2() {
		if (!ctx.getDialect().isMySqlFamily())
			return;
		Systemout.println("testEntity2");
		Entity2 e = new Entity2().setName("name1").insert();
		Assert.assertEquals(1, (int) e.getId());
		Assert.assertEquals(Integer.class, e.getId().getClass());
		Object o = ctx.iQueryForIntValue("select id from entity2");
		Assert.assertEquals(1, o);
		Assert.assertEquals(Integer.class, o.getClass());

	}

	/** test int primary key */
	public static class Entity3 extends ActiveRecord<Entity3> {
		@Id
		int id;

		String name;

		public int getId() {
			return id;
		}

		public Entity3 setId(int id) {
			this.id = id;
			return this;
		}

		public String getName() {
			return name;
		}

		public Entity3 setName(String name) {
			this.name = name;
			return this;
		}

	}

	@Test
	public void testEntity3() {
		if (!ctx.getDialect().isMySqlFamily())
			return;
		Systemout.println("testEntity3");
		Entity3 e = new Entity3().setId(1).setName("name1").insert();
		Assert.assertEquals(1, e.getId());
		Object o = ctx.iQueryForIntValue("select id from entity3");
		Assert.assertEquals(1, o);
		Assert.assertEquals(Integer.class, o.getClass());

	}
}