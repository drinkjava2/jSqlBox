package com.github.drinkjava2.jsqlbox.function.gtx;

import java.util.Random;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.ShardDatabase;
import com.github.drinkjava2.jdialects.annotation.jdia.ShardTable;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.gtx.GtxConnectionManager;
import com.github.drinkjava2.jsqlbox.gtx.GtxId;
import com.github.drinkjava2.jsqlbox.gtx.GtxLock;
import com.github.drinkjava2.jsqlbox.gtx.GtxTag;
import com.github.drinkjava2.jsqlbox.gtx.GtxUnlockServ;
import com.github.drinkjava2.jtransactions.TxResult;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Global TX Test for shard DB, TB, and locker server
 * 
 * @author Yong Zhu
 * @since 2.0.7
 */
public class GtxShardDbTbLockDbTest {
	private static final int DB_SHARD_QTY = 5;

	DbContext[] ctxs = new DbContext[3];
	DbContext[] lockCtxs = new DbContext[3];

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
		DbContext.resetGlobalVariants(); 

		for (int i = 0; i < 3; i++) {
			lockCtxs[i] = new DbContext(newTestDataSource());
			lockCtxs[i].setName("lock");
			lockCtxs[i].setDbCode(i);
			lockCtxs[i].executeDDL(lockCtxs[i].toCreateDDL(GtxId.class));
			lockCtxs[i].executeDDL(lockCtxs[i].toCreateDDL(GtxLock.class));
			lockCtxs[i].executeDDL(lockCtxs[i].toCreateGtxLogDDL(DemoUsr.class));
			lockCtxs[i].setMasters(lockCtxs);
		}

		GtxConnectionManager lockCM = new GtxConnectionManager(lockCtxs[0]);// random choose 1
		for (int i = 0; i < 3; i++) {
			ctxs[i] = new DbContext(newTestDataSource());
			ctxs[i].setName("db");
			ctxs[i].setDbCode(i);
			ctxs[i].setConnectionManager(lockCM);
			ctxs[i].setMasters(ctxs);
			ctxs[i].executeDDL(ctxs[i].toCreateDDL(GtxTag.class));
			TableModel model = TableModelUtils.entity2Model(DemoUsr.class);
			for (int j = 0; j < DB_SHARD_QTY; j++) {
				model.setTableName("DemoUsr_" + j);
				ctxs[i].executeDDL(ctxs[i].toCreateDDL(model));
			}
		}
		DbContext.setGlobalDbContext(ctxs[0]);// the default ctx
	}

	@Test
	public void commitTest() {
		ctxs[0].startTransOnLockDb(1);
		try {
			new DemoUsr().setId(0).setAge(0).insert(); // db0, tb0
			new DemoUsr().setId(1).setAge(10).insert(); // db1, tb1
			new DemoUsr().setId(4).setAge(11).insert(); // db1, tb1
			new DemoUsr().setId(2).setAge(40).insert(); // db2, tb4
			ctxs[0].commitTrans();
		} catch (Exception e) {
			//e.printStackTrace();
			ctxs[0].rollbackTrans();
		}
		Assert.assertEquals(1, ctxs[0].qryIntValue("select count(1) from DemoUsr_0"));
		Assert.assertEquals(2, ctxs[1].qryIntValue("select count(1) from DemoUsr_1"));
		Assert.assertEquals(1, ctxs[2].qryIntValue("select count(1) from DemoUsr_4"));
	}

	@Test
	public void commitFailTest() {
		ctxs[0].startTransOnLockDb(1);
		try {
			new DemoUsr().setId(0).setAge(0).insert(); // locker1, db0, tb0
			new DemoUsr().setId(1).setAge(10).insert(); // locker1, db1, tb1
			new DemoUsr().setId(4).setAge(11).insert(); // locker1, db1, tb1
			new DemoUsr().setId(2).setAge(40).insert(); // locker1, db2, tb4
			Assert.assertEquals(1, ctxs[0].qryIntValue("select count(1) from DemoUsr_0"));
			Assert.assertEquals(2, ctxs[1].qryIntValue("select count(1) from DemoUsr_1"));
			Assert.assertEquals(1, ctxs[2].qryIntValue("select count(1) from DemoUsr_4"));
			ctxs[2].setForceCommitFail(); // force db2 commit fail
			ctxs[0].commitTrans(); // exception will throw
		} catch (Exception e) {
			//e.printStackTrace();
			TxResult result = ctxs[0].rollbackTrans();
			GtxUnlockServ.forceUnlock(1, ctxs[0], result);// Force unlock for unit test only
		}
		Assert.assertEquals(0, ctxs[0].qryIntValue("select count(1) from DemoUsr_0"));
		Assert.assertEquals(0, ctxs[1].qryIntValue("select count(1) from DemoUsr_1"));
		Assert.assertEquals(0, ctxs[2].qryIntValue("select count(1) from DemoUsr_4"));
	}

	public static class DemoUsr extends ActiveRecord<DemoUsr> {
		@Id
		@ShardDatabase({ "MOD", "3" })
		Integer id;

		@ShardTable({ "RANGE", "10" })
		Integer age;

		public Integer getId() {
			return id;
		}

		public DemoUsr setId(Integer id) {
			this.id = id;
			return this;
		}

		public Integer getAge() {
			return age;
		}

		public DemoUsr setAge(Integer age) {
			this.age = age;
			return this;
		}

	}
}
