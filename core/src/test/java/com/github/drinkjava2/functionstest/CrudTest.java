package com.github.drinkjava2.functionstest;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.IGNORE_NULL;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.TAIL;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.sqlitem.SampleItem;

/**
 * Usage of different SQL style and speed test
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class CrudTest extends TestBase {
	{
		regTables(CrudUser.class);
	}

	public static class CrudUser extends ActiveRecord<CrudUser> {
		@Id
		String name;
		String address;
		Integer age;

		public CrudUser() {
		}

		public CrudUser(String name, String address) {
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

	/**
	 * Test below CRUD methods:cc
	 * 
	 * insert,update,tryUpdate,delete,tryDelete,deleteById,tryDeleteById,load,
	 * tryLoad,loadById,tryLoadById,findAll,findAllByIds,countAll,exist,existById,
	 * findRelated
	 */
	@Test
	public void crudTest() {
		// ======insert
		CrudUser u1 = new CrudUser("Name1", "Address1");
		CrudUser u2 = new CrudUser("Name2", "Address2");
		CrudUser u3 = new CrudUser("Name3", "Address3");
		CrudUser u4 = new CrudUser().putField("name", "Name4", "address", "Address4");
		ctx.eInsert(u1);
		ctx.eInsert(u2, IGNORE_NULL);
		u3.insert();
		u4.insert(IGNORE_NULL);

		// ======update
		u1.setAddress("NewAddress1");
		u2.setAddress("NewAddress2");
		u3.setAddress("NewAddress3");
		u4.setAddress("NewAddress4");
		ctx.eUpdate(u1);
		u2.update();
		Assert.assertEquals(1, ctx.eUpdateTry(u3));
		Assert.assertEquals(1, u4.updateTry());

		// =======load
		Assert.assertEquals("NewAddress1", ctx.eLoad(u1).getAddress());
		Assert.assertEquals("NewAddress2", u2.load().getAddress());
		Assert.assertEquals(1, ctx.eLoadTry(u3));
		Assert.assertEquals("NewAddress3", u3.getAddress());
		Assert.assertEquals(1, u4.loadTry());
		Assert.assertEquals("NewAddress4", u4.getAddress());

		// =======load by id (id is basic value)
		Assert.assertEquals("NewAddress1", ctx.eLoadById(CrudUser.class, "Name1").getAddress());
		Assert.assertEquals("NewAddress2", u2.loadById("Name2").getAddress());
		Assert.assertEquals("NewAddress3", ctx.eLoadByIdTry(CrudUser.class, "Name3").getAddress());
		Assert.assertEquals("NewAddress4", u4.loadByIdTry("Name4").getAddress());

		// =======load by id (id is map)
		Map<String, Object> mp = new HashMap<String, Object>();
		mp.put("name", "Name1");
		Assert.assertEquals("NewAddress1", ctx.eLoadById(CrudUser.class, mp).getAddress());
		mp.put("name", "Name2");
		Assert.assertEquals("NewAddress2", u2.loadById(mp).getAddress());
		mp.put("name", "Name3");
		Assert.assertEquals("NewAddress3", ctx.eLoadByIdTry(CrudUser.class, mp).getAddress());
		mp.put("name", "Name4");
		Assert.assertEquals("NewAddress4", u4.loadByIdTry(mp).getAddress());

		// =======load by id (id is Tail)
		CrudUser tail = new CrudUser().putTail("name", "Name1");
		Assert.assertEquals("NewAddress1", ctx.eLoadById(CrudUser.class, tail, TAIL).getAddress());
		tail.putTail("name", "Name2");
		Assert.assertEquals("NewAddress2", u2.loadById(tail, TAIL).getAddress());
		tail.putTail("name", "Name3");
		Assert.assertEquals("NewAddress3", ctx.eLoadByIdTry(CrudUser.class, tail, TAIL).getAddress());
		tail.putTail("name", "Name4");
		Assert.assertEquals("NewAddress4", u4.loadByIdTry(tail, TAIL).getAddress());

		// =======load by id (id is Entity bean)
		Assert.assertEquals("NewAddress1", ctx.eLoadById(CrudUser.class, u1).getAddress());
		Assert.assertEquals("NewAddress2", u1.loadById(u2).getAddress());
		Assert.assertEquals("NewAddress3", ctx.eLoadByIdTry(CrudUser.class, u3).getAddress());
		Assert.assertEquals("NewAddress4", u1.loadByIdTry(u4).getAddress());

		// =======findAll
		Assert.assertEquals(4, ctx.eFindAll(CrudUser.class).size());
		Assert.assertEquals(4, new CrudUser().findAll().size());

		// ========fidnBySql
		Assert.assertEquals(4, ctx.eFindBySQL(CrudUser.class, "select * from CrudUser").size());
		Assert.assertEquals(4, u1.findBySQL("select * from CrudUser").size());

		// ========findBySample
		Assert.assertEquals(1, ctx.eFindBySample(u1).size());
		Assert.assertEquals(1, ctx.eFindAll(CrudUser.class, new SampleItem(u2).sql(" where  ").notNullFields()).size());
		CrudUser sample = new CrudUser("Nam", "addr");
		Assert.assertEquals(4, ctx.eFindAll(CrudUser.class, new SampleItem(sample).sql(" where (").allFields()
				.sql(") or name like ?").param(":name%").sql(" order by name")).size());
		Assert.assertEquals(4,
				ctx.eFindBySQL(CrudUser.class, new SampleItem(sample).sql("select * from CrudUser where (").nullFields()
						.sql(") or name like ?").param(":name%").sql(" order by name")).size());

		Assert.assertEquals(1, u2.findBySample().size());
		Assert.assertEquals(1, u1.eFindBySample(u2).size());
		Assert.assertEquals(1, u1.findAll(new SampleItem(u3).sql(" where  ").notNullFields()).size());
		Assert.assertEquals(4, u1.findAll(CrudUser.class, new SampleItem(sample).sql(" where (").allFields()
				.sql(") or name like ?").param("%:name%").sql(" order by name")).size());
		Assert.assertEquals(4,
				u1.findBySQL("select * from CrudUser where (",
						new SampleItem(sample).notNullFields().sql(") or name like ?").param(":name%"),
						" order by name").size());

		// =======countAll
		Assert.assertEquals(4, ctx.eCountAll(CrudUser.class));
		Assert.assertEquals(4, new CrudUser().countAll());

		// =======exist
		Assert.assertEquals(true, ctx.eExist(u1));
		Assert.assertEquals(true, u2.exist());

		// =======existById (id is basic value)
		Assert.assertEquals(true, ctx.eExistById(CrudUser.class, "Name1"));
		Assert.assertEquals(true, u1.existById("Name2"));

		// =======existById (id is Map)
		Map<String, Object> m1 = new HashMap<String, Object>();
		m1.put("name", "Name1");
		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("name", "Name2");
		Assert.assertEquals(true, ctx.eExistById(CrudUser.class, m1));
		Assert.assertEquals(true, u1.existById(m2));

		// =======existById (id is tail)
		CrudUser t1 = new CrudUser().putTail("name", "Name1");
		CrudUser t2 = new CrudUser().putTail("name", "Name2");
		Assert.assertEquals(true, ctx.eExistById(CrudUser.class, t1, TAIL));
		Assert.assertEquals(true, u1.existById(t2, TAIL));

		// =======existById (id is entity bean)
		Assert.assertEquals(true, ctx.eExistById(CrudUser.class, u1));
		Assert.assertEquals(true, u1.existById(u2));

		// =======delete
		ctx.eDelete(u1);
		u2.delete();
		Assert.assertEquals(1, ctx.eDeleteTry(u3));
		Assert.assertEquals(0, ctx.eDeleteTry(u3));
		Assert.assertEquals(1, u4.deleteTry());
		Assert.assertEquals(0, u4.deleteTry());
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is basic value)
		new CrudUser("Name1", "Address1").insert();
		new CrudUser("Name2", "Address2").insert();
		new CrudUser("Name3", "Address3").insert();
		new CrudUser("Name4", "Address4").insert();
		ctx.eDeleteById(CrudUser.class, "Name1");
		u1.deleteById("Name2");
		Assert.assertEquals(1, ctx.eDeleteByIdTry(CrudUser.class, "Name3"));
		Assert.assertEquals(0, ctx.eDeleteByIdTry(CrudUser.class, "Name3"));
		Assert.assertEquals(1, u1.deleteByIdTry("Name4"));
		Assert.assertEquals(0, u1.deleteByIdTry("Name4"));
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is Map)
		new CrudUser("Name1", "Address1").insert();
		new CrudUser("Name2", "Address2").insert();
		new CrudUser("Name3", "Address3").insert();
		new CrudUser("Name4", "Address4").insert();
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", "Name1");
		ctx.eDeleteById(CrudUser.class, m);
		m.put("name", "Name2");
		u1.deleteById(m);
		m.put("name", "Name3");
		Assert.assertEquals(1, ctx.eDeleteByIdTry(CrudUser.class, m));
		Assert.assertEquals(0, ctx.eDeleteByIdTry(CrudUser.class, m));
		m.put("name", "Name4");
		Assert.assertEquals(1, u1.deleteByIdTry(m));
		Assert.assertEquals(0, u1.deleteByIdTry(m));
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is Entity bean)
		u1 = new CrudUser("Name1", "Address1").insert();
		u2 = new CrudUser("Name2", "Address2").insert();
		u3 = new CrudUser("Name3", "Address3").insert();
		u4 = new CrudUser("Name4", "Address4").insert();
		ctx.eDeleteById(CrudUser.class, u1);
		u1.deleteById(u2);
		Assert.assertEquals(1, ctx.eDeleteByIdTry(CrudUser.class, u3));
		Assert.assertEquals(0, ctx.eDeleteByIdTry(CrudUser.class, u3));
		Assert.assertEquals(1, u1.deleteByIdTry(u4));
		Assert.assertEquals(0, u1.deleteByIdTry(u4));
		Assert.assertEquals(0, u1.countAll());
	}

}
