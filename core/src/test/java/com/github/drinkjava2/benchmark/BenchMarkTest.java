package com.github.drinkjava2.benchmark;

import static com.github.drinkjava2.jdbpro.JDBPRO.notNull;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.LEFT_JOIN_SQL;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.alias;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.gctx;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.give;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.pagin;

import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;

public class BenchMarkTest extends TestBase {
	{
		regTables(DemoCustomer.class, DemoOrder.class, DemoUser.class);
		 
	}

	@Before
	public void init() {
		super.init();
		new DemoCustomer().putFields("id","code","name");
		new DemoCustomer().putValues(1,"a","Customer1").insert();
		new DemoCustomer().putValues(2,"b","Customer2").insert();
		new DemoCustomer().putValues(3,"c","Customer3").insert(); 
		new DemoOrder().putFields("id","name","custId");
		new DemoOrder().putValues(1,"a",1).insert(); 
		new DemoOrder().putValues(2,"b",1).insert(); 
		new DemoOrder().putValues(3,"c",2).insert(); 
		new DemoOrder().putValues(4,"d",3).insert(); 
	}

	DataSource ds = null;

	int index = 1;

	@Test
	public void doTest() {
		long repeat = 1;
		Timer.multiple = 1;
		Timer.lastMark = "Done testOrmQUery2";
		for (int j = 0; j < 1; j++) { // change loop count to test
			for (int i = 0; i < repeat; i++)
				testAdd();
			Timer.set("Done Add");
			for (int i = 0; i < repeat; i++)
				testPageQuery();
			Timer.set("Done PageQry");
			for (int i = 0; i < repeat; i++)
				testUnique();
			Timer.set("Done Unique");
			for (int i = 0; i < repeat; i++)
				testUpdateById();
			Timer.set("Done UpdateById");
			for (int i = 0; i < repeat; i++)
				testExampleQuery();
			Timer.set("Done ExampleQuery");
			for (int i = 0; i < repeat; i++)
				testOrmQUery();
			Timer.set("Done testOrmQUery");
			for (int i = 0; i < repeat; i++)
				testOrmQUery2();
			Timer.set("Done testOrmQUery2");
		}
		Timer.print();
	}

	public void testAdd() {
		getNewUser().insert();
	}

	public void testUnique() {
		new DemoUser().put("id", 1).load();// included unique check
		// or use gctx().entityExistById(DemoUser.class, 1);
		// or use new DemoUser().existById(1);
	}

	public void testUpdateById() {
		new DemoUser().put("id", 1, "code", "abc").update();
	}

	public void testPageQuery() {
		new DemoUser().pageQuery(pagin(1, 10), notNull(" and code=?", "abc"));
	}

	public void testExampleQuery() {
		List<DemoUser> result = gctx().entityFindBySample(new DemoUser().put("id", 1));
		DemoUser u = result.get(0);
	}

	public void testOrmQUery() {
		EntityNet net = gctx().iQuery(new EntityNetHandler(), DemoOrder.class, DemoCustomer.class, alias("o", "c"),
				give("c", "o"), LEFT_JOIN_SQL);
		List<DemoOrder> list = net.pickEntityList(DemoOrder.class);
		for (DemoOrder order : list) {
			DemoCustomer customer = order.getDemoCustomer();
			if (customer == null)
				throw new RuntimeException("orm error");
		}
	}

	public void testOrmQUery2() {
		gctx().setAllowShowSQL(true);
		List<DemoOrder> list = gctx().entityFindAll(DemoOrder.class);
		for (DemoOrder order : list) {
			DemoCustomer customer = order.findOneRelated(DemoCustomer.class);
			System.out.println(customer.getName());
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
