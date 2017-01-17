package test.function_test.query_method;

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

public class JoinQueryTest {

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
		o.setOrderName("PO2017-001");
		o.insert();
		o.setOrderName("PO2017-002");
		o.insert();
		Assert.assertEquals(2, (int) Dao.queryForInteger("select count(*) from orders"));

		for (int i = 0; i < 5; i++) {
			OrderItem item = new OrderItem();
			item.setOrderId(o.getId());
			item.setItemName("Book" + i);
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
	public void doJoinQueryTest() {
		Customer customer = new Customer();
		Order order = new Order();
		customer.box().configAlias("c");
		order.box().configAlias("o");
		List<Map<String, Object>> result = Dao.queryForList(select(), customer.all(), ",", order.all(), from(),
				customer.table(), ",", order.table(), " where ", customer.ID(), "=", order.CUSTOMERID());
		for (Map<String, Object> map : result) {
			System.out.println(map);
		}
		List<Map<String, Object>> result2 = Dao.queryForList(
				"select c.*,o.*,i.*  from customer c left outer join orders o on c.id=o.customer_id  left outer join orderitem i on o.id=i.order_id");
		for (Map<String, Object> map : result2) {
			System.out.println(map);
		}

	}

}