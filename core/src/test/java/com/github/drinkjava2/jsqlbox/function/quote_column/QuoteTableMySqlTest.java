package com.github.drinkjava2.jsqlbox.function.quote_column;

import static com.github.drinkjava2.jsqlbox.DB.IGNORE_EMPTY;
import static com.github.drinkjava2.jsqlbox.DB.IGNORE_NULL;
import static com.github.drinkjava2.jsqlbox.DB.TAIL;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jsqlbox.sqlitem.SampleItem;

/**
 * Test column name is database's keywords, quoted with ``, and for entity use
 * column with name =`xxx`
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class QuoteTableMySqlTest extends TestBase {
	{
		regTables(Order.class);
	}

	@Table(name = "`Order`")
	public static class Order extends ActiveRecord<Order> {
		@Id
		String name;
		String address;
		Integer age;

		public Order() {
		}

		public Order(String name, String address) {
			this.name = name;
			this.address = address;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}
	}

	@Test
	public void globalIgnoreNullTest() {
		if (!(dialect.isMySqlFamily() || dialect.isH2Family()))
			return;
		DbContext ctx1 = new DbContext(ctx.getDataSource());
		ctx1.setIgnoreNull(true);
		Order u = new Order("Name_u", "");
		ctx1.entityInsert(u);
		ctx1.setIgnoreEmpty(true);
		Order u2 = new Order("Name_u2", "");
		ctx1.entityInsert(u2);
		ctx1.setIgnoreNull(false);
		ctx1.setIgnoreEmpty(false);
		Order u3 = new Order("Name_u3", "");
		ctx1.entityInsert(u3);
		Order u4 = new Order("Name_u4", "");
		ctx1.entityInsert(u4, IGNORE_EMPTY);
	}

	/**
	 * Test below CRUD methods:cc
	 * 
	 * insert,update,tryUpdate,delete,tryDelete,deleteById,tryDeleteById,load,
	 * tryLoad,loadById,tryLoadById,findAll,findAllByIds,countAll,exist,existById,
	 * findRelated
	 */
	@Test
	public void crudTest() {
		if (!(dialect.isMySqlFamily() || dialect.isH2Family()))
			return;
		// ======insert
		Order u1 = new Order("Name1", "Address1");
		u1.setAge(null);
		Order u2 = new Order("Name2", "");
		Order u3 = new Order("Name3", "");
		Order u4 = new Order().putField("name", "Name4", "address", "Address4");
		ctx.entityInsert(u1);
		u2.insert(IGNORE_NULL);
		u3.insert(IGNORE_EMPTY);
		u4.insert(IGNORE_NULL);

		// ======update
		u1.setAddress("NewAddress1");
		u2.setAddress("NewAddress2");
		u3.setAddress("NewAddress3");
		u4.setAddress("NewAddress4");
		ctx.entityUpdate(u1);
		u2.update();
		Assert.assertEquals(1, ctx.entityUpdateTry(u3));
		Assert.assertEquals(1, u4.updateTry());

		// =======load
		Assert.assertEquals("NewAddress1", ctx.entityLoad(u1).getAddress());
		Assert.assertEquals("NewAddress2", u2.load().getAddress());
		Assert.assertEquals(1, ctx.entityLoadTry(u3));
		Assert.assertEquals("NewAddress3", u3.getAddress());
		Assert.assertEquals(1, u4.loadTry());
		Assert.assertEquals("NewAddress4", u4.getAddress());

		// =======load by id (id is basic value)
		Assert.assertEquals("NewAddress1", ctx.entityLoadById(Order.class, "Name1").getAddress());
		Assert.assertEquals("NewAddress2", u2.loadById("Name2").getAddress());
		Assert.assertEquals("NewAddress3", ctx.entityLoadByIdTry(Order.class, "Name3").getAddress());
		Assert.assertEquals("NewAddress4", u4.loadByIdTry("Name4").getAddress());

		// =======load by id (id is map)
		Map<String, Object> mp = new HashMap<String, Object>();
		mp.put("name", "Name1");
		Assert.assertEquals("NewAddress1", ctx.entityLoadById(Order.class, mp).getAddress());
		mp.put("name", "Name2");
		Assert.assertEquals("NewAddress2", u2.loadById(mp).getAddress());
		mp.put("name", "Name3");
		Assert.assertEquals("NewAddress3", ctx.entityLoadByIdTry(Order.class, mp).getAddress());
		mp.put("name", "Name4");
		Assert.assertEquals("NewAddress4", u4.loadByIdTry(mp).getAddress());

		// =======load by id (id is Tail)
		Order tail = new Order().putTail("name", "Name1");
		Assert.assertEquals("NewAddress1", ctx.entityLoadById(Order.class, tail, TAIL).getAddress());
		tail.putTail("name", "Name2");
		Assert.assertEquals("NewAddress2", u2.loadById(tail, TAIL).getAddress());
		tail.putTail("name", "Name3");
		Assert.assertEquals("NewAddress3", ctx.entityLoadByIdTry(Order.class, tail, TAIL).getAddress());
		tail.putTail("name", "Name4");
		Assert.assertEquals("NewAddress4", u4.loadByIdTry(tail, TAIL).getAddress());

		// =======load by id (id is Entity bean)
		Assert.assertEquals("NewAddress1", ctx.entityLoadById(Order.class, u1).getAddress());
		Assert.assertEquals("NewAddress2", u1.loadById(u2).getAddress());
		Assert.assertEquals("NewAddress3", ctx.entityLoadByIdTry(Order.class, u3).getAddress());
		Assert.assertEquals("NewAddress4", u1.loadByIdTry(u4).getAddress());

		// =======findAll
		Assert.assertEquals(4, ctx.entityFind(Order.class).size());
		Assert.assertEquals(4, new Order().findAll().size());

		// ========fidnBySql
		Assert.assertEquals(4, ctx.entityFindBySql(Order.class, "select * from `Order`").size());
		Assert.assertEquals(4, u1.findBySQL("select * from `Order`").size());

		// ========findBySample
		Assert.assertEquals(1, ctx.entityFindBySample(u1).size());
		Assert.assertEquals(1, ctx.entityFind(Order.class, new SampleItem(u2).sql(" where  ").notNullFields()).size());
		Order sample = new Order("Nam", "addr");
		Assert.assertEquals(4, ctx.entityFind(Order.class, new SampleItem(sample).sql(" where (").allFields()
				.sql(") or name like ?").param(":name%").sql(" order by name")).size());
		Assert.assertEquals(4, ctx.entityFindBySql(Order.class, new SampleItem(sample).sql("select * from `Order` where (")
				.nullFields().sql(") or name like ?").param(":name%").sql(" order by name")).size());

		Assert.assertEquals(1, u2.findBySample().size());
		Assert.assertEquals(1, u1.findAll(new SampleItem(u3).sql(" where  ").notNullFields()).size());
		Assert.assertEquals(4, u1.findAll(Order.class, new SampleItem(sample).sql(" where (").allFields()
				.sql(") or name like ?").param("%:name%").sql(" order by name")).size());
		Assert.assertEquals(4,
				u1.findBySQL("select * from `Order` where (",
						new SampleItem(sample).notNullFields().sql(") or name like ?").param(":name%"),
						" order by name").size());

		// =======countAll
		Assert.assertEquals(4, ctx.entityCount(Order.class));
		Assert.assertEquals(4, new Order().countAll());

		// =======exist
		Assert.assertEquals(true, ctx.entityExist(u1));
		Assert.assertEquals(true, u2.existId());

		// =======existStrict
		Assert.assertTrue(u1.existStrict());
		u1.setAge(1000);
		Assert.assertFalse(u1.existStrict());

		// =======existById (id is basic value)
		Assert.assertEquals(true, ctx.entityExistById(Order.class, "Name1"));
		Assert.assertEquals(true, u1.existById("Name2"));

		// =======existById (id is Map)
		Map<String, Object> m1 = new HashMap<String, Object>();
		m1.put("name", "Name1");
		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("name", "Name2");
		Assert.assertEquals(true, ctx.entityExistById(Order.class, m1));
		Assert.assertEquals(true, u1.existById(m2));

		// =======existById (id is tail)
		Order t1 = new Order().putTail("name", "Name1");
		Order t2 = new Order().putTail("name", "Name2");
		Assert.assertEquals(true, ctx.entityExistById(Order.class, t1, TAIL));
		Assert.assertEquals(true, u1.existById(t2, TAIL));

		// =======existById (id is entity bean)
		Assert.assertEquals(true, ctx.entityExistById(Order.class, u1));
		Assert.assertEquals(true, u1.existById(u2));

		// =======delete
		ctx.entityDelete(u1);
		u2.delete();
		Assert.assertEquals(1, ctx.entityDeleteTry(u3));
		Assert.assertEquals(0, ctx.entityDeleteTry(u3));
		Assert.assertEquals(1, u4.deleteTry());
		Assert.assertEquals(0, u4.deleteTry());
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is basic value)
		new Order("Name1", "Address1").insert();
		new Order("Name2", "Address2").insert();
		new Order("Name3", "Address3").insert();
		new Order("Name4", "Address4").insert();
		ctx.entityDeleteById(Order.class, "Name1");
		u1.deleteById("Name2");
		Assert.assertEquals(1, ctx.entityDeleteByIdTry(Order.class, "Name3"));
		Assert.assertEquals(0, ctx.entityDeleteByIdTry(Order.class, "Name3"));
		Assert.assertEquals(1, u1.deleteByIdTry("Name4"));
		Assert.assertEquals(0, u1.deleteByIdTry("Name4"));
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is Map)
		new Order("Name1", "Address1").insert();
		new Order("Name2", "Address2").insert();
		new Order("Name3", "Address3").insert();
		new Order("Name4", "Address4").insert();
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", "Name1");
		ctx.entityDeleteById(Order.class, m);
		m.put("name", "Name2");
		u1.deleteById(m);
		m.put("name", "Name3");
		Assert.assertEquals(1, ctx.entityDeleteByIdTry(Order.class, m));
		Assert.assertEquals(0, ctx.entityDeleteByIdTry(Order.class, m));
		m.put("name", "Name4");
		Assert.assertEquals(1, u1.deleteByIdTry(m));
		Assert.assertEquals(0, u1.deleteByIdTry(m));
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is Entity bean)
		u1 = new Order("Name1", "Address1").insert();
		u2 = new Order("Name2", "Address2").insert();
		u3 = new Order("Name3", "Address3").insert();
		u4 = new Order("Name4", "Address4").insert();
		ctx.entityDeleteById(Order.class, u1);
		u1.deleteById(u2);
		Assert.assertEquals(1, ctx.entityDeleteByIdTry(Order.class, u3));
		Assert.assertEquals(0, ctx.entityDeleteByIdTry(Order.class, u3));
		Assert.assertEquals(1, u1.deleteByIdTry(u4));
		Assert.assertEquals(0, u1.deleteByIdTry(u4));
		Assert.assertEquals(0, u1.countAll());
	}

}
