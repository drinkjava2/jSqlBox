package com.github.drinkjava2.functionstest;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.IGNORE_NULL;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdbpro.LinkStyleArrayList;
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
		CrudUser u4 = new CrudUser("Name4", "Address4");
		ctx.entityInsert(u1);
		ctx.entityInsert(u2, IGNORE_NULL);
		u3.insert();
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
		Assert.assertEquals("NewAddress1", ctx.entityLoadById(CrudUser.class, "Name1").getAddress());
		Assert.assertEquals("NewAddress2", u2.loadById("Name2").getAddress());
		Assert.assertEquals("NewAddress3", ctx.entityLoadByIdTry(CrudUser.class, "Name3").getAddress());
		Assert.assertEquals("NewAddress4", u4.loadByIdTry("Name4").getAddress());

		// =======load by id (id is map)
		Map<String, Object> mp = new HashMap<String, Object>();
		mp.put("name", "Name1");
		Assert.assertEquals("NewAddress1", ctx.entityLoadById(CrudUser.class, mp).getAddress());
		mp.put("name", "Name2");
		Assert.assertEquals("NewAddress2", u2.loadById(mp).getAddress());
		mp.put("name", "Name3");
		Assert.assertEquals("NewAddress3", ctx.entityLoadByIdTry(CrudUser.class, mp).getAddress());
		mp.put("name", "Name4");
		Assert.assertEquals("NewAddress4", u4.loadByIdTry(mp).getAddress());

		// =======load by id (id is Entity bean)
		Assert.assertEquals("NewAddress1", ctx.entityLoadById(CrudUser.class, u1).getAddress());
		Assert.assertEquals("NewAddress2", u1.loadById(u2).getAddress());
		Assert.assertEquals("NewAddress3", ctx.entityLoadByIdTry(CrudUser.class, u3).getAddress());
		Assert.assertEquals("NewAddress4", u1.loadByIdTry(u4).getAddress());

		// =======loadByIds (id is basic value list)
		LinkStyleArrayList<String> ids = new LinkStyleArrayList<String>().append("Name1").append("Name2")
				.append("Name3");
		Assert.assertEquals(3, ctx.entityFindByIds(CrudUser.class, ids).size());
		Assert.assertEquals(3, new CrudUser().findByIds(ids).size());

		// =======loadByIds (id is mp)
		LinkStyleArrayList<Object> idMapList = new LinkStyleArrayList<Object>();
		Map<String, Object> mpId1 = new HashMap<String, Object>();
		mpId1.put("name", "Name1");
		Map<String, Object> mpId2 = new HashMap<String, Object>();
		mpId2.put("name", "Name2");
		Map<String, Object> mpId3 = new HashMap<String, Object>();
		mpId3.put("name", "Name3");
		idMapList.append(mpId1).append(mpId2).append(mpId3);
		Assert.assertEquals(3, ctx.entityFindByIds(CrudUser.class, idMapList).size());
		Assert.assertEquals(3, new CrudUser().findByIds(idMapList).size());

		// =======findAll
		Assert.assertEquals(4, ctx.entityFindAll(CrudUser.class).size());
		Assert.assertEquals(4, new CrudUser().findAll().size());

		// =======findByIds (id is bean)
		LinkStyleArrayList<Object> idBeanList = new LinkStyleArrayList<Object>();
		idBeanList.append(u1).append(u2).append(u3);
		Assert.assertEquals(3, ctx.entityFindByIds(CrudUser.class, idBeanList).size());
		Assert.assertEquals(3, new CrudUser().findByIds(idBeanList).size());

		// ========fidnBySql
		Assert.assertEquals(4, ctx.entityFindBySQL(CrudUser.class, "select * from CrudUser").size());
		Assert.assertEquals(4, u1.findBySQL("select * from CrudUser").size());

		// ========findBySample
		Assert.assertEquals(1, ctx.entityFindBySample(u1).size());
		Assert.assertEquals(1,
				ctx.entityFindAll(CrudUser.class, new SampleItem(u2).sql(" where  ").notNullFields()).size());
		CrudUser sample = new CrudUser("Nam", "addr");
		Assert.assertEquals(4, ctx.entityFindAll(CrudUser.class, new SampleItem(sample).sql(" where (").allFields()
				.sql(") or name like ?").param(":name%").sql(" order by name")).size());
		Assert.assertEquals(4,
				ctx.entityFindBySQL(CrudUser.class, new SampleItem(sample).sql("select * from CrudUser where (")
						.nullFields().sql(") or name like ?").param(":name%").sql(" order by name")).size());

		Assert.assertEquals(1, u1.findBySample(u2).size());
		Assert.assertEquals(1, u1.findAll(new SampleItem(u3).sql(" where  ").notNullFields()).size());
		Assert.assertEquals(4, u1.findAll(CrudUser.class, new SampleItem(sample).sql(" where (").allFields()
				.sql(") or name like ?").param("%:name%").sql(" order by name")).size());
		Assert.assertEquals(4,
				u1.findBySQL("select * from CrudUser where (",
						new SampleItem(sample).notNullFields().sql(") or name like ?").param(":name%"), " order by name")
						.size());

		// =======countAll
		Assert.assertEquals(4, ctx.entityCountAll(CrudUser.class));
		Assert.assertEquals(4, new CrudUser().countAll());

		// =======exist
		Assert.assertEquals(true, ctx.entityExist(u1));
		Assert.assertEquals(true, u2.exist());

		// =======existById (id is basic value)
		Assert.assertEquals(true, ctx.entityExistById(CrudUser.class, "Name1"));
		Assert.assertEquals(true, u1.existById("Name2"));

		// =======existById (id is Map)
		Map<String, Object> m1 = new HashMap<String, Object>();
		m1.put("name", "Name1");
		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("name", "Name2");
		Assert.assertEquals(true, ctx.entityExistById(CrudUser.class, m1));
		Assert.assertEquals(true, u1.existById(m2));

		// =======existById (id is entity bean)
		Assert.assertEquals(true, ctx.entityExistById(CrudUser.class, u1));
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
		new CrudUser("Name1", "Address1").insert();
		new CrudUser("Name2", "Address2").insert();
		new CrudUser("Name3", "Address3").insert();
		new CrudUser("Name4", "Address4").insert();
		ctx.entityDeleteById(CrudUser.class, "Name1");
		u1.deleteById("Name2");
		Assert.assertEquals(1, ctx.entityDeleteByIdTry(CrudUser.class, "Name3"));
		Assert.assertEquals(0, ctx.entityDeleteByIdTry(CrudUser.class, "Name3"));
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
		ctx.entityDeleteById(CrudUser.class, m);
		m.put("name", "Name2");
		u1.deleteById(m);
		m.put("name", "Name3");
		Assert.assertEquals(1, ctx.entityDeleteByIdTry(CrudUser.class, m));
		Assert.assertEquals(0, ctx.entityDeleteByIdTry(CrudUser.class, m));
		m.put("name", "Name4");
		Assert.assertEquals(1, u1.deleteByIdTry(m));
		Assert.assertEquals(0, u1.deleteByIdTry(m));
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is Entity bean)
		u1 = new CrudUser("Name1", "Address1").insert();
		u2 = new CrudUser("Name2", "Address2").insert();
		u3 = new CrudUser("Name3", "Address3").insert();
		u4 = new CrudUser("Name4", "Address4").insert();
		ctx.entityDeleteById(CrudUser.class, u1);
		u1.deleteById(u2);
		Assert.assertEquals(1, ctx.entityDeleteByIdTry(CrudUser.class, u3));
		Assert.assertEquals(0, ctx.entityDeleteByIdTry(CrudUser.class, u3));
		Assert.assertEquals(1, u1.deleteByIdTry(u4));
		Assert.assertEquals(0, u1.deleteByIdTry(u4));
		Assert.assertEquals(0, u1.countAll());
	}

}
