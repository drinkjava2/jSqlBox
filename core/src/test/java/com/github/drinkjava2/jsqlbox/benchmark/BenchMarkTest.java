package com.github.drinkjava2.jsqlbox.benchmark;

import static com.github.drinkjava2.jsqlbox.DB.AUTO_SQL;
import static com.github.drinkjava2.jsqlbox.DB.alias;
import static com.github.drinkjava2.jsqlbox.DB.gctx;
import static com.github.drinkjava2.jsqlbox.DB.give;
import static com.github.drinkjava2.jsqlbox.DB.notNull;
import static com.github.drinkjava2.jsqlbox.DB.pagin;
import static com.github.drinkjava2.jsqlbox.DB.par;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;

public class BenchMarkTest extends TestBase implements TestServiceInterface {
	{
		regTables(DemoCustomer.class, DemoOrder.class, DemoUser.class);
	}

	@Override
	@Before
	public void init() {
		super.init();
		new DemoCustomer().forFields("id", "code", "name");
		new DemoCustomer().putValues(1, "a", "Customer1").insert();
		new DemoCustomer().putValues(2, "b", "Customer2").insert();
		new DemoCustomer().putValues(3, "c", "Customer3").insert();
		new DemoOrder().forFields("id", "name", "custId");
		new DemoOrder().putValues(1, "a", 1).insert();
		new DemoOrder().putValues(2, "b", 1).insert();
		new DemoOrder().putValues(3, "c", 2).insert();
		new DemoOrder().putValues(4, "d", 3).insert();
	}

	int index = 1;

	@Test
	public void doTest() {
		Debuger.lastMark = "testNetRelated";
		testAdd();
		Debuger.set("add");
		for (int j = 0; j < 100; j++) { // change repeat times to test
			testPageQuery();
			Debuger.set("pageQry");
			testUnique();
			Debuger.set("unique");
			testUpdateById();
			Debuger.set("updateById");
			testExampleQuery();
			Debuger.set("exampleQuery");
			testOrmQUery();
			Debuger.set("ormQUery");
			testOrmQUerySQ();
			Debuger.set("ormQUerySQ");
			testSqlRelated();
			Debuger.set("sqlRelated");
			testNetRelated();
			Debuger.set("NetRelated");
		}
		Debuger.print();
	}

	public void testTitledArrayResultHander() {
		EntityNet net = gctx().qry(new EntityNet(), DemoOrder.class, AUTO_SQL);
		gctx().qry(net, DemoCustomer.class, AUTO_SQL);
		List<DemoOrder> list = net.pickEntityList(DemoOrder.class);
		for (DemoOrder order : list) {
			DemoCustomer customer = order.findRelatedOne(net, DemoCustomer.class);
			if (customer == null)
				throw new RuntimeException("orm error");
		}
	}

	@Override
	public void testAdd() {
		getNewUser().insert();
	}

	@Override
	public void testUnique() {
		DemoUser d = new DemoUser().putField("id", 1).load();// included unique check
		if (d.getCode() == null)
			throw new RuntimeException("testUnique error");
		// or gctx().entityExistById(DemoUser.class, 1);
	}

	@Override
	public void testUpdateById() {
		new DemoUser().putField("id", 1, "code", "abc").update();
	}

	@Override
	public void testPageQuery() {
		new DemoUser().pageQuery(pagin(1, 10), notNull(" and code=?", "abc"));
	}

	@Override
	public void testExampleQuery() {
		List<DemoUser> result = gctx().entityFindBySample(new DemoUser().putField("id", 1, "code", "abc"), " or code=?",
				par("efg"));
		if (result.get(0) == null)
			throw new RuntimeException("Example query error");
	}

	@Override
	public void testOrmQUery() {
		List<DemoOrder> list = gctx().autoNet(DemoOrder.class, DemoCustomer.class).pickEntityList(DemoOrder.class);
		for (DemoOrder order : list) {
			DemoCustomer customer = order.getDemoCustomer();
			if (customer == null)
				throw new RuntimeException("Orm query error");
		}
	}

	public void testOrmQUerySQ() {
		EntityNet net = gctx().qry(new EntityNetHandler(), DemoOrder.class, DemoCustomer.class, alias("o", "c"),
				give("c", "o"),
				"select o.id as o_id, o.name as o_name, o.cust_id as o_cust_id, c.id as c_id, c.code as c_code, c.name as c_name from sys_order o left join sys_customer c on o.cust_id=c.id");
		List<DemoOrder> list = net.pickEntityList(DemoOrder.class);
		for (DemoOrder order : list) {
			DemoCustomer customer = order.getDemoCustomer();
			if (customer == null)
				throw new RuntimeException("Orm query error");
		}
	}

	public void testSqlRelated() {
		List<DemoOrder> list = gctx().entityFind(DemoOrder.class);
		for (DemoOrder order : list) {
			DemoCustomer customer = order.findRelatedOne(DemoCustomer.class);
			if (customer == null)
				throw new RuntimeException("orm error");
		}
	}

	public void testNetRelated() {
		EntityNet net = gctx().qry(new EntityNet(), DemoOrder.class, AUTO_SQL);
		gctx().qry(net, DemoCustomer.class, AUTO_SQL);
		List<DemoOrder> list = net.pickEntityList(DemoOrder.class);
		for (DemoOrder order : list) {
			DemoCustomer customer = order.findRelatedOne(net, DemoCustomer.class);
			if (customer == null)
				throw new RuntimeException("orm error");
		}
	}

	private DemoUser getNewUser() {
		DemoUser user = new DemoUser();
		user.setId(index++);
		user.setCode("abc");
		return user;
	}

}
