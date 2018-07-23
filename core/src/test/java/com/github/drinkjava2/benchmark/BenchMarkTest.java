package com.github.drinkjava2.benchmark;

import static com.github.drinkjava2.jdbpro.JDBPRO.param;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;

public class BenchMarkTest extends TestBase implements TestServiceInterface {
	{
		regTables(DemoCustomer.class, DemoOrder.class, DemoUser.class);
	}

	@Override
	@Before
	public void init() {
		super.init();
		new DemoCustomer().putFields("id", "code", "name");
		new DemoCustomer().putValues(1, "a", "Customer1").insert();
		new DemoCustomer().putValues(2, "b", "Customer2").insert();
		new DemoCustomer().putValues(3, "c", "Customer3").insert();
		new DemoOrder().putFields("id", "name", "custId");
		new DemoOrder().putValues(1, "a", 1).insert();
		new DemoOrder().putValues(2, "b", 1).insert();
		new DemoOrder().putValues(3, "c", 2).insert();
		new DemoOrder().putValues(4, "d", 3).insert();
	}

	int index = 1;

	@Test
	public void doTest() {
		Timer.lastMark = "Done testOrmQUery";
		for (int j = 0; j < 1000; j++) { // change repeat times to test
			testAdd();
			Timer.set("Done Add");
			testPageQuery();
			Timer.set("Done PageQry");
			testUnique();
			Timer.set("Done Unique");
			testUpdateById();
			Timer.set("Done UpdateById");
			testExampleQuery();
			Timer.set("Done ExampleQuery");
			testOrmQUery();
			Timer.set("Done testOrmQUery");
		}
		Timer.print();
	}

	@Override
	public void testAdd() {
		getNewUser().insert();
	}

	@Override
	public void testUnique() {
		new DemoUser().put("id", 1).load();// included unique check
		// or use gctx().entityExistById(DemoUser.class, 1);
		// or use new DemoUser().existById(1);
	}

	@Override
	public void testUpdateById() {
		new DemoUser().put("id", 1, "code", "abc").update();
	}

	@Override
	public void testPageQuery() {
		new DemoUser().pageQuery(pagin(1, 10), notNull(" and code=?", "abc"));
	}

	@Override
	public void testExampleQuery() {
		List<DemoUser> result = gctx().entityFindBySample(new DemoUser().put("id", 1, "code", "abc"), " or code=?",
				param("efg"));
		if (result.get(0) == null)
			throw new RuntimeException("Example query error");
	}

	@Override
	public void testOrmQUery() {
		List<DemoOrder> list = gctx().autoEntityNet(DemoOrder.class, DemoCustomer.class)
				.pickEntityList(DemoOrder.class);
		for (DemoOrder order : list) {
			DemoCustomer customer = order.getDemoCustomer();
			if (customer == null)
				throw new RuntimeException("Orm query error");
		}
	}

	private DemoUser getNewUser() {
		DemoUser user = new DemoUser();
		user.setId(index++);
		user.setCode("abc");
		return user;
	}

}
