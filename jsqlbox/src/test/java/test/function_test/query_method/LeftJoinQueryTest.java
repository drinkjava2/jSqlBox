package test.function_test.query_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.from;
import static com.github.drinkjava2.jsqlbox.SqlHelper.select;
import static com.github.drinkjava2.jsqlbox.SqlMapping.mapping;
import static com.github.drinkjava2.jsqlbox.SqlMapping.oneToMany;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;

import test.config.PrepareTestContext;
import test.config.po.Customer;
import test.config.po.Order;
import test.config.po.OrderItem;

public class LeftJoinQueryTest {

	/**
	 * Prepare test data for object tree
	 */
	@Before
	public void setup() {
		System.out.println("===============================Testing JoinQueryTest===============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		Dao.executeQuiet("drop table orderitem");
		Dao.executeQuiet("drop table orders");
		Dao.executeQuiet("drop table customer");
		Dao.execute(Customer.CREATE_SQL);
		Dao.execute(Order.CREATE_SQL);
		Dao.execute(OrderItem.CREATE_SQL);
		Dao.refreshMetaData();

		Customer c = new Customer();
		c.setCustomerName("Sam");
		c.insert();
		c.setCustomerName("Tom");
		c.insert();
		Assert.assertEquals(2, (int) Dao.queryForInteger("select count(*) from customer"));

		Order o = new Order();
		o.setCustomerId(c.getId());
		o.setOrderDate(new Date());
		o.setOrderName("Order1");
		o.insert();
		o.setOrderName("Order2");
		o.insert();
		o.setOrderName("Order3");
		o.insert();
		Assert.assertEquals(3, (int) Dao.queryForInteger("select count(*) from orders"));

		for (int i = 0; i < 5; i++) {
			OrderItem item = new OrderItem();
			item.setOrderId(o.getId());
			item.setItemName("OrderItem" + i);
			item.setItemQty(i);
			item.insert();
		}
		Assert.assertEquals(5, (int) Dao.queryForInteger("select count(*) from orderitem"));
		Dao.getDefaultContext().setShowSql(true);
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	@Test
	public void leftJoinQueryNoAlias() {
		Customer c = new Customer();
		Order o = new Order();
		OrderItem i = new OrderItem();
		List<Map<String, Object>> result2 = Dao.queryForList(select(), c.all(), ",", o.all(), ",", i.all(), from(),
				c.table(), //
				" left outer join ", o.table(), " on ", c.ID(), "=", o.CUSTOMERID(), //
				" left outer join ", i.table(), " on ", o.ID(), "=", i.ORDERID(), //
				" order by ", o.ID(), ",", i.ID());
		for (Map<String, Object> map : result2) {
			System.out.println(map.get(o.ORDERNAME()));
		}
	}

	@Test
	public void leftJoinQueryWithAlias1() {
		Customer c = new Customer().configAlias("c");
		Order o = new Order().configAlias("o");
		OrderItem i = new OrderItem().configAlias("i");
		mapping(oneToMany(), c.ID(), o.CUSTOMERID());
		mapping(oneToMany(), o.ID(), i.ORDERID());

		List<Map<String, Object>> result2 = Dao.queryForList(select(), c.all(), ",", o.all(), ",", i.all(), from(),
				c.table(), //
				" left outer join ", o.table(), " on ", c.ID(), "=", o.CUSTOMERID(), //
				" left outer join ", i.table(), " on ", o.ID(), "=", i.ORDERID(), //
				" order by ", o.ID(), ",", i.ID());

		for (Map<String, Object> map : result2) {
			System.out.println(map.get(o.alias(o.ORDERNAME())));
		}
		// TODO work on it, need add a method on entity to transfer result list to object tree
	}

	@Test
	public void leftJoinQueryWithAlias2() {
		Customer c = new Customer().configAlias("c");
		Order o = new Order().configAlias("o");
		OrderItem i = new OrderItem().configAlias("i");
		List<Map<String, Object>> result2 = Dao.queryForList(select(), c.all(), ",", o.all(), ",", i.all(), from(),
				c.table(), //
				" left outer join ", o.table(), " on ", mapping(oneToMany(), c.ID(), o.CUSTOMERID()), //
				" left outer join ", i.table(), " on ", mapping(oneToMany(), o.ID(), i.ORDERID()), //
				" order by ", o.ID(), ",", i.ID());
		for (Map<String, Object> map : result2) {
			System.out.println(map.get(o.alias(o.ORDERNAME())));
		}
	}

	@Test
	public void leftJoinQueryAutomaticQuerySQL() {
		Customer c = new Customer();
		Order o = new Order();
		OrderItem i = new OrderItem();
		mapping(oneToMany(), c.ID(), o.CUSTOMERID());
		mapping(oneToMany(), o.ID(), i.ORDERID());
		List<Map<String, Object>> result2 = Dao.queryForList(c.automaticQuerySQL(), " order by ", c.ID());
		// TODO work on it, automaticQuerySQL should return the full left join SQL

		for (Map<String, Object> map : result2) {
			System.out.println(map);
			System.out.println(map.get(o.alias(o.ORDERNAME())));
		}
	}

}