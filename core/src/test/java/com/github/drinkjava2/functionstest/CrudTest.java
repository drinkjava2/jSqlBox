package com.github.drinkjava2.functionstest;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdbpro.LinkStyleArrayList;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Usage of different SQL style and speed test
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class CrudTest {
	static long REPEAT_TIMES = 1;
	static boolean PRINT_TIMEUSED = false;

	protected HikariDataSource dataSource;

	public static class CrudUser extends ActiveRecord<CrudUser> {
		@Id
		@Column(name = "name")
		String name;
		String address;

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

	}

	@Before
	public void init() {
		dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setUsername("sa");// change to your user & password
		dataSource.setPassword("");
		// SqlBoxContext.setGlobalAllowShowSql(true);
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(null);
		for (String ddl : ctx.getDialect().toDropAndCreateDDL(CrudUser.class))
			try {
				ctx.nExecute(ddl);
			} catch (Exception e) {
			}
	}

	@After
	public void cleanUp() {
		dataSource.close();
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
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);

		// ======insert
		CrudUser u1 = new CrudUser("Name1", "Address1");
		CrudUser u2 = new CrudUser("Name2", "Address2");
		CrudUser u3 = new CrudUser("Name3", "Address3");
		CrudUser u4 = new CrudUser("Name4", "Address4");
		ctx.entityInsert(u1);
		ctx.entityInsert(u2);
		u3.insert();
		u4.insert();

		// ======update
		u1.setAddress("NewAddress1");
		u2.setAddress("NewAddress2");
		u3.setAddress("NewAddress3");
		u4.setAddress("NewAddress4");
		ctx.entityUpdate(u1);
		u2.update();
		Assert.assertEquals(1, ctx.entityTryUpdate(u3));
		Assert.assertEquals(1, u4.tryUpdate());

		// =======load
		Assert.assertEquals("NewAddress1", ctx.entityLoad(u1).getAddress());
		Assert.assertEquals("NewAddress2", u2.load().getAddress());
		Assert.assertEquals(1, ctx.entityTryLoad(u3));
		Assert.assertEquals("NewAddress3", u3.getAddress());
		Assert.assertEquals(1, u4.tryLoad());
		Assert.assertEquals("NewAddress4", u4.getAddress());

		// =======load by id (id is basic value)
		Assert.assertEquals("NewAddress1", ctx.entityLoadById(CrudUser.class, "Name1").getAddress());
		Assert.assertEquals("NewAddress2", u2.loadById("Name2").getAddress());
		Assert.assertEquals("NewAddress3", ctx.entityTryLoadById(CrudUser.class, "Name3").getAddress());
		Assert.assertEquals("NewAddress4", u4.tryLoadById("Name4").getAddress());

		// =======load by id (id is map)
		Map<String, Object> mp = new HashMap<String, Object>();
		mp.put("name", "Name1");
		Assert.assertEquals("NewAddress1", ctx.entityLoadById(CrudUser.class, mp).getAddress());
		mp.put("name", "Name2");
		Assert.assertEquals("NewAddress2", u2.loadById(mp).getAddress());
		mp.put("name", "Name3");
		Assert.assertEquals("NewAddress3", ctx.entityTryLoadById(CrudUser.class, mp).getAddress());
		mp.put("name", "Name4");
		Assert.assertEquals("NewAddress4", u4.tryLoadById(mp).getAddress());

		// =======load by id (id is Entity bean)
		Assert.assertEquals("NewAddress1", ctx.entityLoadById(CrudUser.class, u1).getAddress());
		Assert.assertEquals("NewAddress2", u1.loadById(u2).getAddress());
		Assert.assertEquals("NewAddress3", ctx.entityTryLoadById(CrudUser.class, u3).getAddress());
		Assert.assertEquals("NewAddress4", u1.tryLoadById(u4).getAddress());

		// =======loadByIds (id is basic value list)
		LinkStyleArrayList<String> ids = new LinkStyleArrayList<String>().append("Name1").append("Name2")
				.append("Name3");
		Assert.assertEquals(3, ctx.entityLoadByIds(CrudUser.class, ids).size());
		Assert.assertEquals(3, new CrudUser().loadByIds(ids).size());

		// =======loadByIds (id is mp)
		LinkStyleArrayList<Object> idMapList = new LinkStyleArrayList<Object>();
		Map<String, Object> mpId1 = new HashMap<String, Object>();
		mpId1.put("name", "Name1");
		Map<String, Object> mpId2 = new HashMap<String, Object>();
		mpId2.put("name", "Name2");
		Map<String, Object> mpId3 = new HashMap<String, Object>();
		mpId3.put("name", "Name3");
		idMapList.append(mpId1).append(mpId2).append(mpId3);
		Assert.assertEquals(3, ctx.entityLoadByIds(CrudUser.class, idMapList).size());
		Assert.assertEquals(3, new CrudUser().loadByIds(idMapList).size());

		// =======loadByIds (id is bean)
		LinkStyleArrayList<Object> idBeanList = new LinkStyleArrayList<Object>();
		idBeanList.append(u1).append(u2).append(u3);
		Assert.assertEquals(3, ctx.entityLoadByIds(CrudUser.class, idBeanList).size());
		Assert.assertEquals(3, new CrudUser().loadByIds(idBeanList).size());

		// =======loadAll
		Assert.assertEquals(4, ctx.entityLoadAll(CrudUser.class).size());
		Assert.assertEquals(4, new CrudUser().loadAll().size());

		// =======loadAllById
		Assert.assertEquals(4, ctx.entityLoadAll(CrudUser.class).size());
		Assert.assertEquals(4, new CrudUser().loadAll().size());

		// =======countAll
		Assert.assertEquals(4, ctx.entityCountAll(CrudUser.class));
		Assert.assertEquals(4, new CrudUser().countAll());

		// =======delete
		ctx.entityDelete(u1);
		u2.delete();
		ctx.entityTryDelete(u3);
		u4.tryDelete();
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is basic value)
		new CrudUser("Name1", "Address1").insert();
		new CrudUser("Name2", "Address2").insert();
		new CrudUser("Name3", "Address3").insert();
		new CrudUser("Name4", "Address4").insert();
		ctx.entityDeleteById(CrudUser.class, "Name1");
		u1.deleteById("Name2");
		ctx.entityTryDeleteById(CrudUser.class, "Name3");
		u1.tryDeleteById("Name4");
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
		ctx.entityTryDeleteById(CrudUser.class, m);
		m.put("name", "Name4");
		u1.tryDeleteById(m);
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is Entity bean)
		u1 = new CrudUser("Name1", "Address1").insert();
		u2 = new CrudUser("Name2", "Address2").insert();
		u3 = new CrudUser("Name3", "Address3").insert();
		u4 = new CrudUser("Name4", "Address4").insert();
		ctx.entityDeleteById(CrudUser.class, u1);
		u1.deleteById(u2);
		ctx.entityTryDeleteById(CrudUser.class, u3);
		u1.tryDeleteById(u4);
		Assert.assertEquals(0, u1.countAll());
	}

}
