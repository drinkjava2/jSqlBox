package com.github.drinkjava2.jsqlbox.function.gtx;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.function.jtransactions.Usr;
import com.github.drinkjava2.jsqlbox.gtx.GtxConnectionManager;
import com.github.drinkjava2.jsqlbox.gtx.GtxInfo;
import com.github.drinkjava2.jsqlbox.gtx.GtxLock;
import com.github.drinkjava2.jsqlbox.gtx.GtxUtils;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Global Tx Test
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxTest {
	SqlBoxContext ctx1;
	SqlBoxContext ctx2;

	private static DataSource buildDataSource(String dsName) {
		HikariDataSource ds = new HikariDataSource();
		ds.setDriverClassName("org.h2.Driver");
		ds.setJdbcUrl("jdbc:h2:mem:" + dsName + ";MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setConnectionTimeout(2000);
		ds.setUsername("sa");
		ds.setPassword("");
		return ds;
	}

	@Before
	public void init() {
		SqlBoxContext.setGlobalNextAllowShowSql(true);

		SqlBoxContext lockCtx = new SqlBoxContext(buildDataSource("GtxTest_gtxServ"));
		lockCtx.setName("lockCtx");
		lockCtx.setConnectionManager(new TinyTxConnectionManager());
		GtxConnectionManager gtx = new GtxConnectionManager(lockCtx);

		ctx1 = new SqlBoxContext(buildDataSource("GtxTest_ds1"));
		ctx1.setName("ctx1");
		ctx1.setConnectionManager(gtx);

		ctx2 = new SqlBoxContext(buildDataSource("GtxTest_ds2"));
		ctx2.setName("ctx2");
		ctx2.setConnectionManager(gtx);

		lockCtx.executeDDL(lockCtx.toCreateDDL(GtxInfo.class));
		lockCtx.executeDDL(lockCtx.toCreateDDL(GtxLock.class));
		lockCtx.executeDDL(lockCtx.toCreateDDL(GtxUtils.entity2GtxLogModel(Usr.class)));
		ctx1.executeDDL(ctx1.toCreateDDL(Usr.class));
		ctx2.executeDDL(ctx2.toCreateDDL(Usr.class));
	}

	@Test
	public void commitTest() { // test group commit
		ctx1.startTransaction();
		try {
			new Usr().putField("id", "UserA").insert(ctx1);
			new Usr().putField("id", "UserB").insert(ctx2);
			new Usr().putField("id", "UserC").insert(ctx2);
			ctx1.commit();
		} catch (Exception e) {
			e.printStackTrace();
			ctx1.rollback();
		}
		Assert.assertEquals(1, ctx1.eCountAll(Usr.class));
		Assert.assertEquals(2, ctx2.eCountAll(Usr.class));
	}

	public void rollbackTest() {
		// TODO: to add
	}

}
