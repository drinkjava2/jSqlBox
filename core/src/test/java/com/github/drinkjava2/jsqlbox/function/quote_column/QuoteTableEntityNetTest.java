package com.github.drinkjava2.jsqlbox.function.quote_column;

import static com.github.drinkjava2.jsqlbox.DB.AUTO_SQL;
import static com.github.drinkjava2.jsqlbox.DB.giveBoth;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.annotation.jdia.SingleFKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID32;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;

/**
 * Test column name is database's keyword, quoted with ``, and for entity use
 * column with name =`xxx`
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class QuoteTableEntityNetTest extends TestBase {

	@Table(name = "`From`")
	public static class From extends ActiveRecord<From> {
		@Id
		@Column(name = "`order`")
		String order;

		@Column(name = "`select`")
		String select;

		public String getOrder() {
			return order;
		}

		public void setOrder(String order) {
			this.order = order;
		}

		public String getSelect() {
			return select;
		}

		public void setSelect(String select) {
			this.select = select;
		}
	}

	@Table(name = "`Group`")
	public static class Group extends ActiveRecord<Group> {
		@Id
		@UUID32
		@Column(name = "`where`")
		String where;

		@SingleFKey(refs = { "`From`", "`order`" })
		@Column(name = "`order`")
		String order;

		public String getOrder() {
			return order;
		}

		public void setOrder(String order) {
			this.order = order;
		}

		public String getWhere() {
			return where;
		}

		public void setWhere(String where) {
			this.where = where;
		}
	}

	private static final Object[] targets = new Object[] { new EntityNetHandler(), From.class, Group.class,
			giveBoth("f", "g") };

	@Test
	public void doTest() {
		if (!(dialect.isMySqlFamily() || dialect.isH2Family()))
			return;
		createAndRegTables(From.class, Group.class);

		new From().forFields("order", "select");
		new From().putValues("o1", "s1").insert();
		new From().putValues("o2", "s2").insert();

		new Group().forFields("where", "order");
		new Group().putValues("w1", "o1").insert();
		new Group().putValues("w2", "o1").insert();
		new Group().putValues("w3", "o2").insert();

		EntityNet net = ctx.qry(targets, AUTO_SQL);
		List<From> a = net.pickEntityList("f");
		List<Group> b = net.pickEntityList("g");
		Assert.assertEquals(2, (int) a.size());
		Assert.assertEquals(3, (int) b.size());
		for (From from : a)
			Systemout.println(from.order + ", " + from.select);
		for (Group group : b)
			Systemout.println(group.order + ", " + group.where);
	}
}
