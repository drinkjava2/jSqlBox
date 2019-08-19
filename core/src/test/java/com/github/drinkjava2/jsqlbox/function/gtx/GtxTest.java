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
import com.github.drinkjava2.jtransactions.TxResult;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Global TX Test
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxTest {
	SqlBoxContext ctx1;
	SqlBoxContext ctx2;
	SqlBoxContext ctx3;

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
		SqlBoxContext gtxCtx = new SqlBoxContext(newTestDataSource());
		gtxCtx.setName("gtxCtx");
		gtxCtx.setConnectionManager(new TinyTxConnectionManager());
		GtxConnectionManager gtx = new GtxConnectionManager(gtxCtx);
		gtxCtx.executeDDL(gtxCtx.toCreateDDL(GtxId.class));
		gtxCtx.executeDDL(gtxCtx.toCreateDDL(GtxLock.class));
		gtxCtx.executeDDL(gtxCtx.toCreateGtxLogDDL(Usr.class));

		ctx1 = new SqlBoxContext(newTestDataSource());
		ctx1.setName("ctx1");
		ctx1.setConnectionManager(gtx);
		ctx1.executeDDL(ctx1.toCreateDDL(GtxId.class));
		ctx1.executeDDL(ctx1.toCreateDDL(Usr.class));

		ctx2 = new SqlBoxContext(newTestDataSource());
		ctx2.setName("ctx2");
		ctx2.setConnectionManager(gtx);
		ctx2.executeDDL(ctx2.toCreateDDL(GtxId.class));
		ctx2.executeDDL(ctx2.toCreateDDL(Usr.class));

		ctx3 = new SqlBoxContext(newTestDataSource());
		ctx3.setName("ctx3");
		ctx3.setConnectionManager(gtx);
		ctx3.executeDDL(ctx3.toCreateDDL(GtxId.class));
		ctx3.executeDDL(ctx3.toCreateDDL(Usr.class));
	}

	@Test
	public void commitTest() { // test group commit
		try {
			ctx1.startTrans();
			new Usr().putField("id", "UserA").insert(ctx1);
			new Usr().putField("id", "UserB").insert(ctx2);
			new Usr().putField("id", "UserC").insert(ctx2);
			new Usr().putField("id", "UserD").insert(ctx3);
			Assert.assertEquals(1, ctx1.eCountAll(Usr.class));
			Assert.assertEquals(2, ctx2.eCountAll(Usr.class));
			Assert.assertEquals(1, ctx3.eCountAll(Usr.class));
			ctx1.commitTrans();
		} catch (Exception e) {
			e.printStackTrace();
			ctx1.rollbackTrans();
		}
		Assert.assertEquals(1, ctx1.eCountAll(Usr.class));
		Assert.assertEquals(2, ctx2.eCountAll(Usr.class));
		Assert.assertEquals(1, ctx3.eCountAll(Usr.class));
	}

	@Test
	public void rollbackCausedByDiv0Test() {
		ctx1.startTrans();
		try {
			new Usr().putField("id", "UserA").insert(ctx1);
			new Usr().putField("id", "UserB").insert(ctx2);
			new Usr().putField("id", "UserC").insert(ctx2);
			new Usr().putField("id", "UserD").insert(ctx3);
			Assert.assertEquals(1, ctx1.eCountAll(Usr.class));
			Assert.assertEquals(2, ctx2.eCountAll(Usr.class));
			Assert.assertEquals(1, ctx3.eCountAll(Usr.class));
			System.out.println(1 / 0);
			ctx1.commitTrans();
		} catch (Exception e) {
			ctx1.rollbackTrans();
		}
		Assert.assertEquals(0, ctx1.eCountAll(Usr.class));
		Assert.assertEquals(0, ctx2.eCountAll(Usr.class));
		Assert.assertEquals(0, ctx3.eCountAll(Usr.class));
	}

	@Test
	public void rollbackFailCausedByDsLostTest() {
		ctx1.startTrans();
		try {
			new Usr().putField("id", "UserA").insert(ctx1);
			new Usr().putField("id", "UserB").insert(ctx2);
			new Usr().putField("id", "UserC").insert(ctx2);
			new Usr().putField("id", "UserD").insert(ctx3);
			Assert.assertEquals(1, ctx1.eCountAll(Usr.class));
			Assert.assertEquals(2, ctx2.eCountAll(Usr.class));
			Assert.assertEquals(1, ctx3.eCountAll(Usr.class));
			((HikariDataSource) ctx2.getDataSource()).close();// Ds2 lost!
		} catch (Exception e) {
			ctx1.rollbackTrans();
		}
		Assert.assertEquals(0, ctx1.eCountAll(Usr.class));
		Assert.assertEquals(0, ctx3.eCountAll(Usr.class));
	}

	@Test
	public void rollbackCausedByPartialCommitTest() {
		ctx1.startTrans();
		TxResult result;
		try {
			new Usr().putField("id", "UserA").insert(ctx1);
			new Usr().putField("id", "UserB").insert(ctx2);
			new Usr().putField("id", "UserC").insert(ctx2);
			new Usr().putField("id", "UserD").insert(ctx3);
			Assert.assertEquals(1, ctx1.eCountAll(Usr.class));
			Assert.assertEquals(2, ctx2.eCountAll(Usr.class));
			Assert.assertEquals(1, ctx3.eCountAll(Usr.class));
			ctx2.setForceCommitFail(); // force ctx2 commit fail
			result = ctx1.commitTrans();
		} catch (Exception e) {
			result = ctx1.rollbackTrans();
		}
		System.out.println(result.getDetailedInfo());
		Assert.assertEquals(0, ctx1.eCountAll(Usr.class));
		Assert.assertEquals(0, ctx2.eCountAll(Usr.class));
		Assert.assertEquals(0, ctx3.eCountAll(Usr.class));
	}

	@Test
	public void rollbackCausedByLastCommitTest() {
		ctx1.startTrans();
		TxResult result;
		try {
			new Usr().putField("id", "UserA").insert(ctx1);
			new Usr().putField("id", "UserB").insert(ctx2);
			new Usr().putField("id", "UserC").insert(ctx2);
			new Usr().putField("id", "UserD").insert(ctx3);
			Assert.assertEquals(1, ctx1.eCountAll(Usr.class));
			Assert.assertEquals(2, ctx2.eCountAll(Usr.class));
			Assert.assertEquals(1, ctx3.eCountAll(Usr.class));
			ctx3.setForceCommitFail(); // force ctx2 commit fail
			result = ctx1.commitTrans();
		} catch (Exception e) {
			result = ctx1.rollbackTrans();
		}
		System.out.println(result.getDetailedInfo());
		Assert.assertEquals(0, ctx1.eCountAll(Usr.class));
		Assert.assertEquals(0, ctx2.eCountAll(Usr.class));
		Assert.assertEquals(0, ctx3.eCountAll(Usr.class));
	}

}
