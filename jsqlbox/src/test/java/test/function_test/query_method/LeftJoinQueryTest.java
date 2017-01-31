package test.function_test.query_method;

import static com.github.drinkjava2.jsqlbox.SqlHelper.many;
import static com.github.drinkjava2.jsqlbox.SqlHelper.one;
import static com.github.drinkjava2.jsqlbox.SqlHelper.selectAlias;
import static com.github.drinkjava2.jsqlbox.SqlHelper.endAlias;

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
		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from customer"));
		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from orders"));
		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from orderitem"));

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
	public void doLeftJoinQuery() {
		Customer c = new Customer().configAlias("c");
		Order o = new Order().configAlias("o");
		OrderItem i = new OrderItem().configAlias("i");
		//bind(onoToMany(), c.ID(), o.CUSTOMERID());
		//bind(onoToOne(), c.ID(), o.CUSTOMERID());
		//bind(manyToMany(), c.ID(), o.CUSTOMERID());
		List<Map<String, Object>> result2 = Dao.queryForList(selectAlias(), c.all(), ",", o.all(), ",", i.all(),
				endAlias(), " from ", c.table(), //
				" left outer join ", o.table(), " on ", c.ID(), "=", o.CUSTOMERID(), //
				"  left outer join ", i.table(), " on ", o.ID(), "=", i.ORDERID());
		for (Map<String, Object> map : result2) {
			System.out.println(map);
			System.out.println(map.get(o.ORDERNAME()));
		}
		// TODO: work on it

	}

}