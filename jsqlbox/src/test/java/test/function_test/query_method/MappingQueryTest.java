package test.function_test.query_method;

import static com.github.drinkjava2.jsqlbox.MappingHelper.oneToMany;
import static com.github.drinkjava2.jsqlbox.MappingHelper.to;
import static com.github.drinkjava2.jsqlbox.SqlHelper.aliasBegin;
import static com.github.drinkjava2.jsqlbox.SqlHelper.aliasEnd;
import static com.github.drinkjava2.jsqlbox.SqlHelper.from;
import static com.github.drinkjava2.jsqlbox.SqlHelper.select;

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

public class MappingQueryTest {

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
		for (int i = 0; i < 3; i++) {
			o.setCustomerId(c.getId());
			o.setOrderDate(new Date());
			o.setOrderName("Order" + i);
			o.insert();
		}
		Assert.assertEquals(3, (int) Dao.queryForInteger("select count(*) from orders"));

		for (int i = 0; i < 3; i++) {
			OrderItem item = new OrderItem();
			item.setOrderId(o.getId());
			item.setItemName("OrderItem" + i);
			item.setItemQty(i);
			item.insert();
		}
		Assert.assertEquals(3, (int) Dao.queryForInteger("select count(*) from orderitem"));
		Dao.getDefaultContext().setShowSql(true).setFormatSql(true);
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
	public void leftJoinQueryWithAlias() {
		Customer c = new Customer().configAlias("c");
		Order o = new Order().configAlias("o");
		OrderItem i = new OrderItem().configAlias("i");
		Map<String, Object> map1 = Dao.queryForEntityMap(select(), c.all(), ",", o.all(), ",", i.all(), from(),
				c.table(), //
				" left outer join ", o.table(), " on ", oneToMany(), c.ID(), "=", o.CUSTOMERID(), to(), //
				" left outer join ", i.table(), " on ", oneToMany(), o.ID(), "=", i.ORDERID(), to(), //
				" order by ", o.ID(), ",", i.ID());
		// System.out.println("map1=" + map1);
		try {
			aliasBegin(); 
			for (Customer customer : (List<Customer>) map1.get(c.ID())) {
				System.out.println("Customer=" + customer.getId() + "," + customer.getCustomerName());
				Map<String, Object> map2 = customer.box().getChildEntityMap(); 
				List<Order> listOrder = (List<Order>) map2.get(o.ID()); 
				if (listOrder != null)
					for (Order order : listOrder) {
						System.out.println(
								"\tOrder=" + order.getId() + "," + order.getOrderName() + "," + order.getCustomerId());
						 Map<String, Object> map3 = order.box().getChildEntityMap(); 
						 System.out.println(map3);
					}
			}
		} finally {
			aliasEnd();
		}
	}

}