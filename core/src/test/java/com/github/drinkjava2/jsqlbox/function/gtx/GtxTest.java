package com.github.drinkjava2.jsqlbox.function.gtx;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.id.UUID25Generator;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.function.jtransactions.Usr;
import com.github.drinkjava2.jsqlbox.gtx.GtxConnectionManager;
import com.github.drinkjava2.jsqlbox.gtx.GtxId;
import com.github.drinkjava2.jsqlbox.gtx.GtxLock;
import com.github.drinkjava2.jsqlbox.gtx.UnlockUtils;
import com.github.drinkjava2.jtransactions.TxResult;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Global TX Test
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxTest {
	SqlBoxContext[] ctx = new SqlBoxContext[3];

	private static DataSource newTestDataSource() {
		HikariDataSource ds = new HikariDataSource();
		ds.setDriverClassName("org.h2.Driver");
		ds.setJdbcUrl("jdbc:h2:mem:" + UUID25Generator.getUUID25()
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
		lock.executeDDL(lock.toCreateGtxLogDDL(Usr.class));

		GtxConnectionManager lockCM = new GtxConnectionManager(lock);
		for (int i = 0; i < 3; i++) {
			ctx[i] = new SqlBoxContext(newTestDataSource());
			ctx[i].setName("db");
			ctx[i].setDbCode(i);
			ctx[i].setConnectionManager(lockCM);
			ctx[i].setMasters(ctx);
			ctx[i].executeDDL(ctx[i].toCreateDDL(GtxId.class));
			ctx[i].executeDDL(ctx[i].toCreateDDL(Usr.class));
		}

	}

	@Test
	public void commitTest() { // test group commit
		ctx[0].startTrans();
		try {
			new Usr().putField("id", "UserA").insert(ctx[0]);
			new Usr().putField("id", "UserB").insert(ctx[1]);
			new Usr().putField("id", "UserC").insert(ctx[1]);
			new Usr().putField("id", "UserD").insert(ctx[2]);
			Assert.assertEquals(1, ctx[0].eCountAll(Usr.class));
			Assert.assertEquals(2, ctx[1].eCountAll(Usr.class));
			Assert.assertEquals(1, ctx[2].eCountAll(Usr.class));
			ctx[0].commitTrans();
		} catch (Exception e) {
			e.printStackTrace();
			ctx[0].rollbackTrans();
		}
		Assert.assertEquals(1, ctx[0].eCountAll(Usr.class));
		Assert.assertEquals(2, ctx[1].eCountAll(Usr.class));
		Assert.assertEquals(1, ctx[2].eCountAll(Usr.class));
	}

	@Test
	public void rollbackCausedByDiv0Test() {
		ctx[0].startTrans();
		try {
			new Usr().putField("id", "UserA").insert(ctx[0]);
			new Usr().putField("id", "UserB").insert(ctx[1]);
			new Usr().putField("id", "UserC").insert(ctx[1]);
			new Usr().putField("id", "UserD").insert(ctx[2]);
			Assert.assertEquals(1, ctx[0].eCountAll(Usr.class));
			Assert.assertEquals(2, ctx[1].eCountAll(Usr.class));
			Assert.assertEquals(1, ctx[2].eCountAll(Usr.class));
			System.out.println(1 / 0);
			ctx[0].commitTrans();
		} catch (Exception e) {
			ctx[0].rollbackTrans();
		}
		Assert.assertEquals(0, ctx[0].eCountAll(Usr.class));
		Assert.assertEquals(0, ctx[1].eCountAll(Usr.class));
		Assert.assertEquals(0, ctx[2].eCountAll(Usr.class));
	}

	@Test
	public void rollbackFailCausedByDsLostTest() {
		ctx[0].startTrans();
		try {
			new Usr().putField("id", "UserA").insert(ctx[0]);
			new Usr().putField("id", "UserB").insert(ctx[1]);
			new Usr().putField("id", "UserC").insert(ctx[1]);
			new Usr().putField("id", "UserD").insert(ctx[2]);
			Assert.assertEquals(1, ctx[0].eCountAll(Usr.class));
			Assert.assertEquals(2, ctx[1].eCountAll(Usr.class));
			Assert.assertEquals(1, ctx[2].eCountAll(Usr.class));
			((HikariDataSource) ctx[1].getDataSource()).close();// One DS lost!
			ctx[0].commitTrans();
		} catch (Exception e) {
			e.printStackTrace();
			ctx[0].rollbackTrans();
		}
		Assert.assertEquals(0, ctx[0].eCountAll(Usr.class));
		Assert.assertEquals(0, ctx[2].eCountAll(Usr.class));
	}

	@Test
	public void rollbackCausedByCommitTest() {
		try {
			ctx[0].startTrans();
			new Usr().putField("id", "UserA").insert(ctx[0]);
			new Usr().putField("id", "UserB").insert(ctx[1]);
			new Usr().putField("id", "UserC").insert(ctx[1]);
			new Usr().putField("id", "UserD").insert(ctx[2]);
			Assert.assertEquals(1, ctx[0].eCountAll(Usr.class));
			Assert.assertEquals(2, ctx[1].eCountAll(Usr.class));
			Assert.assertEquals(1, ctx[2].eCountAll(Usr.class));
			ctx[1].setForceCommitFail(); // force ctx[1] commit fail
			ctx[0].commitTrans(); // exception will throw
		} catch (Exception e) {
			TxResult result = ctx[0].rollbackTrans();
			UnlockUtils.forceUnlock(result.getTxId(), ctx[0]);
		}
		Assert.assertEquals(0, ctx[0].eCountAll(Usr.class));
		Assert.assertEquals(0, ctx[2].eCountAll(Usr.class));
	}

}
