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
public class QuoteColumnEntityNetTest extends TestBase {

	public static class Ademo extends ActiveRecord<Ademo> {
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

	public static class Bdemo extends ActiveRecord<Bdemo> {
		@Id
		@UUID32
		@Column(name = "`where`")
		String where;

		@SingleFKey(refs = { "Ademo", "`order`" })
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

	private static final Object[] targets = new Object[] { new EntityNetHandler(), Ademo.class, Bdemo.class,
			giveBoth("a", "b") };

	@Test
	public void doTest() {
		if (!(dialect.isMySqlFamily() || dialect.isH2Family()))
			return;
		createAndRegTables(Ademo.class, Bdemo.class);

		new Ademo().forFields("order", "select");
		new Ademo().putValues("o1", "s1").insert();
		new Ademo().putValues("o2", "s2").insert();

		new Bdemo().forFields("where", "order");
		new Bdemo().putValues("w1", "o1").insert();
		new Bdemo().putValues("w2", "o1").insert();
		new Bdemo().putValues("w3", "o2").insert();

		EntityNet net = ctx.iQuery(targets, AUTO_SQL);
		List<Ademo> a = net.pickEntityList("a");
		List<Bdemo> b = net.pickEntityList("b");
		Assert.assertEquals(2, (int) a.size());
		Assert.assertEquals(3, (int) b.size());
		for (Ademo ademo : a)
			Systemout.println(ademo.order + ", " + ademo.select);
		for (Bdemo bdemo : b)
			Systemout.println(bdemo.order + ", " + bdemo.where);
	}
}
