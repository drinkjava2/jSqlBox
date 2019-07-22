package com.github.drinkjava2.jsqlbox.function.gtx;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.function.jtransactions.Usr;
import com.github.drinkjava2.jsqlbox.gtx.GtxConnectionManager;
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

	@Before
	public void init() {
		HikariDataSource ds1 = new HikariDataSource();
		ds1.setDriverClassName("org.h2.Driver");
		ds1.setJdbcUrl("jdbc:h2:mem:GlobalTxTst_ds1;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds1.setConnectionTimeout(1000);
		ds1.setUsername("sa");
		ds1.setPassword("");

		HikariDataSource ds2 = new HikariDataSource();
		ds2.setDriverClassName("org.h2.Driver");
		ds2.setJdbcUrl("jdbc:h2:mem:GlobalTxTst_ds2;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds2.setConnectionTimeout(1000);
		ds2.setUsername("sa");
		ds2.setPassword("");

		SqlBoxContext.setGlobalNextAllowShowSql(true);
		ctx1 = new SqlBoxContext(ds1);
		ctx1.setName("ctx1");
		ctx1.setConnectionManager(GtxConnectionManager.instance());
		SqlBoxContext.setGlobalSqlBoxContext(ctx1);

		ctx2 = new SqlBoxContext(ds2);
		ctx2.setName("ctx2");
		ctx2.setConnectionManager(GtxConnectionManager.instance());

		ctx1.executeDDL(ctx1.toCreateDDL(Usr.class));
		ctx2.executeDDL(ctx2.toCreateDDL(Usr.class));

	}

	@Test
	public void commitTest() { // test group commit
		ctx1.startTransaction();
		try {
			new Usr().putField("id", "UserA").insert();
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
