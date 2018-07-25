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

/**
 * CRUD test for entity have Compound Id
 * 
 * @since 1.7.0
 */
public class CrudCompoundIdTest extends TestBase {
	{
		regTables(CpdUser.class);
	}

	public static class CpdUser extends ActiveRecord<CpdUser> {
		@Id
		String name;

		@Id
		Boolean sex;

		String address;

		Integer age;

		public CpdUser() {
		}

		public CpdUser(String name, Boolean sex, String address) {
			this.name = name;
			this.sex = sex;
			this.address = address;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Boolean getSex() {
			return sex;
		}

		public void setSex(Boolean sex) {
			this.sex = sex;
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
		CpdUser u1 = new CpdUser("Name1", true, "Address1");
		CpdUser u2 = new CpdUser("Name2", true, "Address2");
		CpdUser u3 = new CpdUser("Name3", true, "Address3");
		CpdUser u4 = new CpdUser("Name4", true, "Address4");
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

		// =======load by id (id is map)
		Map<String, Object> mp = new HashMap<String, Object>();
		mp.put("sex", true);
		mp.put("name", "Name1");
		Assert.assertEquals("NewAddress1", ctx.entityLoadById(CpdUser.class, mp).getAddress());
		mp.put("name", "Name2");
		Assert.assertEquals("NewAddress2", u2.loadById(mp).getAddress());
		mp.put("name", "Name3");
		Assert.assertEquals("NewAddress3", ctx.entityLoadByIdTry(CpdUser.class, mp).getAddress());
		mp.put("name", "Name4");
		Assert.assertEquals("NewAddress4", u4.loadByIdTry(mp).getAddress());

		// =======load by id (id is Entity bean)
		Assert.assertEquals("NewAddress1", ctx.entityLoadById(CpdUser.class, u1).getAddress());
		Assert.assertEquals("NewAddress2", u1.loadById(u2).getAddress());
		Assert.assertEquals("NewAddress3", ctx.entityLoadByIdTry(CpdUser.class, u3).getAddress());
		Assert.assertEquals("NewAddress4", u1.loadByIdTry(u4).getAddress());

		// =======loadByIds (id is mp)
		LinkStyleArrayList<Object> idMapList = new LinkStyleArrayList<Object>();
		Map<String, Object> mpId1 = new HashMap<String, Object>();
		mpId1.put("sex", true);
		mpId1.put("name", "Name1");
		Map<String, Object> mpId2 = new HashMap<String, Object>();
		mpId1.put("sex", true);
		mpId2.put("name", "Name2");
		Map<String, Object> mpId3 = new HashMap<String, Object>();
		mpId1.put("sex", true);
		mpId3.put("name", "Name3");
		idMapList.append(mpId1).append(mpId2).append(mpId3);
		Assert.assertEquals(3, ctx.entityFindByIds(CpdUser.class, idMapList).size());
		Assert.assertEquals(3, new CpdUser().findByIds(idMapList).size());

		// =======findAll
		Assert.assertEquals(4, ctx.entityFindAll(CpdUser.class).size());
		Assert.assertEquals(4, new CpdUser().findAll().size());

		// =======findByIds (id is bean)
		LinkStyleArrayList<Object> idBeanList = new LinkStyleArrayList<Object>();
		idBeanList.append(u1).append(u2).append(u3);
		Assert.assertEquals(3, ctx.entityFindByIds(CpdUser.class, idBeanList).size());
		Assert.assertEquals(3, new CpdUser().findByIds(idBeanList).size());

		// ========findBySql
		Assert.assertEquals(4, ctx.iQueryForEntityList(CpdUser.class, "select * from CpdUser").size());
		Assert.assertEquals(4, u1.findBySQL("select * from CpdUser").size());

		// =======countAll
		Assert.assertEquals(4, ctx.entityCountAll(CpdUser.class));
		Assert.assertEquals(4, new CpdUser().countAll());

		// =======exist
		Assert.assertEquals(true, ctx.entityExist(u1));
		Assert.assertEquals(true, u2.exist());

		// =======existById (id is Map)
		Map<String, Object> m1 = new HashMap<String, Object>();
		m1.put("sex", true);
		m1.put("name", "Name1");
		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("sex", false);
		m2.put("name", "Name2");
		Assert.assertEquals(true, ctx.entityExistById(CpdUser.class, m1));
		Assert.assertEquals(false, u1.existById(m2));

		// =======existById (id is entity bean)
		Assert.assertEquals(true, ctx.entityExistById(CpdUser.class, u1));
		Assert.assertEquals(true, u1.existById(u2));

		// =======delete
		ctx.entityDelete(u1);
		u2.delete();
		Assert.assertEquals(1, ctx.entityDeleteTry(u3));
		Assert.assertEquals(0, ctx.entityDeleteTry(u3));
		Assert.assertEquals(1, u4.deleteTry());
		Assert.assertEquals(0, u4.deleteTry());
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is Map)
		new CpdUser("Name1", true, "Address1").insert();
		new CpdUser("Name2", true, "Address2").insert();
		new CpdUser("Name3", true, "Address3").insert();
		new CpdUser("Name4", true, "Address4").insert();
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("sex", true);
		m.put("name", "Name1");
		ctx.entityDeleteById(CpdUser.class, m);
		m.put("name", "Name2");
		u1.deleteById(m);
		m.put("name", "Name3");
		Assert.assertEquals(1, ctx.entityDeleteByIdTry(CpdUser.class, m));
		Assert.assertEquals(0, ctx.entityDeleteByIdTry(CpdUser.class, m));
		m.put("name", "Name4");
		Assert.assertEquals(1, u1.deleteByIdTry(m));
		Assert.assertEquals(0, u1.deleteByIdTry(m));
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is Entity bean)
		u1 = new CpdUser("Name1", true, "Address1").insert();
		u2 = new CpdUser("Name2", true, "Address2").insert();
		u3 = new CpdUser("Name3", true, "Address3").insert();
		u4 = new CpdUser("Name4", true, "Address4").insert();
		ctx.entityDeleteById(CpdUser.class, u1);
		u1.deleteById(u2);
		Assert.assertEquals(1, ctx.entityDeleteByIdTry(CpdUser.class, u3));
		Assert.assertEquals(0, ctx.entityDeleteByIdTry(CpdUser.class, u3));
		Assert.assertEquals(1, u1.deleteByIdTry(u4));
		Assert.assertEquals(0, u1.deleteByIdTry(u4));
		Assert.assertEquals(0, u1.countAll());
	}

}
