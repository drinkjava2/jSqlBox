package com.github.drinkjava2.jsqlbox.function.gtx;

import java.util.Random;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jdia.ShardDatabase;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.gtx.GtxConnectionManager;
import com.github.drinkjava2.jsqlbox.gtx.GtxId;
import com.github.drinkjava2.jsqlbox.gtx.GtxLock;
import com.github.drinkjava2.jsqlbox.gtx.GtxTag;
import com.github.drinkjava2.jsqlbox.gtx.GtxUnlockServ;
import com.github.drinkjava2.jtransactions.TxResult;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Global TX Test for shard DB(database)
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxShardDbTest {
	SqlBoxContext[] ctx = new SqlBoxContext[3];

	private static DataSource newTestDataSource() {
		HikariDataSource ds = new HikariDataSource();
		ds.setDriverClassName("org.h2.Driver");
		ds.setJdbcUrl("jdbc:h2:mem:" + new Random().nextLong() // random h2 ds name
				+ ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setUsername("sa");
		ds.setPassword("");
		return ds;
	}

	@Before
	public void init() {
		SqlBoxContext.resetGlobalVariants();
		SqlBoxContext.setGlobalNextAllowShowSql(true);

		SqlBoxContext lock = new SqlBoxContext(newTestDataSource());
		lock.setName("lock");
		lock.executeDDL(lock.toCreateDDL(GtxId.class));
		lock.executeDDL(lock.toCreateDDL(GtxLock.class));
		lock.executeDDL(lock.toCreateGtxLogDDL(DemoUsr.class));

		GtxConnectionManager lockCM = new GtxConnectionManager(lock);
		for (int i = 0; i < 3; i++) {
			ctx[i] = new SqlBoxContext(newTestDataSource());
			ctx[i].setName("db");
			ctx[i].setDbCode(i);
			ctx[i].setConnectionManager(lockCM);
			ctx[i].setMasters(ctx);
			ctx[i].executeDDL(ctx[i].toCreateDDL(GtxTag.class));
			ctx[i].executeDDL(ctx[i].toCreateDDL(DemoUsr.class));
		}
		SqlBoxContext.setGlobalSqlBoxContext(ctx[0]);// the default ctx
	}

	@Test
	public void commitTest() {
		ctx[0].startTrans();
		try {
			new DemoUsr().setId(0).insert(); // db0
			new DemoUsr().setId(1).insert(); // db1
			new DemoUsr().setId(4).insert(); // db1
			new DemoUsr().setId(2).insert(); // db2
			Assert.assertEquals(1, ctx[0].eCountAll(DemoUsr.class));
			Assert.assertEquals(2, ctx[1].eCountAll(DemoUsr.class));
			Assert.assertEquals(1, ctx[2].eCountAll(DemoUsr.class));
			ctx[0].commitTrans();
		} catch (Exception e) {
			e.printStackTrace();
			ctx[0].rollbackTrans();
		}
		Assert.assertEquals(1, ctx[0].eCountAll(DemoUsr.class));
		Assert.assertEquals(2, ctx[1].eCountAll(DemoUsr.class));
		Assert.assertEquals(1, ctx[2].eCountAll(DemoUsr.class));
	}

	@Test
	public void commitFailTest() {
		ctx[0].startTrans();
		try {
			new DemoUsr().setId(0).insert(); // db0
			new DemoUsr().setId(1).insert(); // db1
			new DemoUsr().setId(4).insert(); // db1
			new DemoUsr().setId(2).insert(); // db2
			ctx[2].setForceCommitFail(); // force db2 commit fail
			ctx[0].commitTrans(); // exception will throw
		} catch (Exception e) {
			TxResult result = ctx[0].rollbackTrans();
			GtxUnlockServ.forceUnlock(ctx[0], result);// Force unlock for unit test only
		}
		Assert.assertEquals(0, ctx[0].eCountAll(DemoUsr.class));
		Assert.assertEquals(0, ctx[1].eCountAll(DemoUsr.class));
		Assert.assertEquals(0, ctx[2].eCountAll(DemoUsr.class));
	}

	public static class DemoUsr extends ActiveRecord<DemoUsr> {
		@Id
		@ShardDatabase({ "MOD", "3" })
		Integer id;
		String name;

		public Integer getId() {
			return id;
		}

		public DemoUsr setId(Integer id) {
			this.id = id;
			return this;
		}

		public String getName() {
			return name;
		}

		public DemoUsr setName(String name) {
			this.name = name;
			return this;
		}

	}
}
