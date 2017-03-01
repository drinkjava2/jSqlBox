
package test.coverage_test;

import static com.github.drinkjava2.jsqlbox.MappingHelper.bind;
import static com.github.drinkjava2.jsqlbox.MappingHelper.oneToMany;
import static com.github.drinkjava2.jsqlbox.MappingHelper.oneToOne;
import static com.github.drinkjava2.jsqlbox.MappingHelper.tree;
import static com.github.drinkjava2.jsqlbox.SqlHelper.from;
import static com.github.drinkjava2.jsqlbox.SqlHelper.select;
import static com.github.drinkjava2.jsqlbox.SqlHelper.use;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.Mapping;
import com.github.drinkjava2.jsqlbox.SqlAndParameters;
import com.github.drinkjava2.jsqlbox.SqlBoxUtils;
import com.github.drinkjava2.jsqlbox.SqlHelper;

import test.config.PrepareTestContext;
import test.config.po.Customer;
import test.config.po.Order;
import test.config.po.OrderItem;

public class SqlMappingTest {
	@Before
	public void setup() {
		System.out.println("=============================Testing SqlBoxTest=============================");
		PrepareTestContext.prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		Dao.executeQuiet("drop table orderitem");
		Dao.executeQuiet("drop table orders");
		Dao.executeQuiet("drop table customer");
		Dao.execute(Customer.CREATE_SQL);
		Dao.execute(Order.CREATE_SQL);
		Dao.execute(OrderItem.CREATE_SQL);
		Dao.refreshMetaData();
	}

	@After
	public void cleanUp() {
		PrepareTestContext.closeDatasource_closeDefaultSqlBoxConetxt();
	}

	/**
	 * Coverage test of mapping methods only, this is a fake query
	 */
	@Test
	public void prepareSQLandParameters() {
		Customer c = new Customer();
		Order o = new Order();
		OrderItem i = new OrderItem();
		SqlAndParameters sp = SqlHelper.prepareSQLandParameters(select(), c.all(), ",", o.all(), ",", i.all(), from(),
				c.table(), //
				" left join ", o.table(), " on ", oneToOne(), c.ID(), "=", o.CUSTOMERID(), bind(), //
				" left join ", o.table(), " on ", oneToMany(), c.ID(), "=", o.CUSTOMERID(), bind(null, o.CUSTOMERID()), //
				" left join ", i.table(), " on ", oneToMany(), o.ID(), "=", i.ORDERID(), bind(c.ID(), null), //
				" left join ", i.table(), " on ", tree(), use(o.ID(), o.CUSTOMERID()), bind(o.ID(), o.CUSTOMERID()), //
				" order by ", o.ID(), ",", i.ID());
		System.out.println(SqlBoxUtils.formatSQL(sp.getSql()));
		List<Mapping> l = sp.getMappingList();
		Mapping m0 = l.get(1);

		Assert.assertEquals(c, m0.getThisEntity());
		Assert.assertEquals(o, m0.getOtherEntity());
		Assert.assertNull(m0.getThisPropertyName());
		Assert.assertEquals(o.fieldID(o.CUSTOMERID()), m0.getOtherPropertyName());

		for (Mapping mapping : l) {
			System.out.println(mapping.getDebugInfo());
		}

	}

}
