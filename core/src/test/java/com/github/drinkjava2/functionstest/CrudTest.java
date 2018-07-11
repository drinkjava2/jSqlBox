package com.github.drinkjava2.functionstest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
	 * Test below CRUD methods:
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
		u2.insert();
		Assert.assertEquals(1, ctx.entityTryInsert(u3));
		Assert.assertEquals(1, u4.tryInsert());

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
		u1.setAddress(null);
		u2.setAddress(null);
		u3.setAddress(null);
		u4.setAddress(null);
		Assert.assertEquals("NewAddress1", ctx.entityLoad(u1).getAddress());
		Assert.assertEquals("NewAddress2", u2.load().getAddress());
		Assert.assertEquals(1, ctx.entityTryLoad(u3));
		Assert.assertEquals("NewAddress3", u3.getAddress());
		Assert.assertEquals(1, u4.tryLoad());
		Assert.assertEquals("NewAddress4", u4.getAddress());

		// =======countAll
		Assert.assertEquals(4, ctx.entityCountAll(CrudUser.class));
		Assert.assertEquals(4, new CrudUser().countAll());

		// =======delete
		ctx.entityDelete(u1);
		u2.delete();
		ctx.entityTryDelete(u3);
		u4.tryDelete();
		Assert.assertEquals(0, u1.countAll());
	}

}
