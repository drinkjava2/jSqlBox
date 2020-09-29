package com.github.drinkjava2.jsqlbox.issues.IYDU7_orm_demo;

import static com.github.drinkjava2.jsqlbox.DB.give;

import java.util.List;

import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jsqlbox.entitynet.EntityNet;
import com.github.drinkjava2.jsqlbox.handler.EntityNetHandler;

/**
 * This is a demo to how to use ORM query
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class IYDU7Test extends TestBase {
	{
		regTables(Ademo.class, Bdemo.class, Cdemo.class);
	}

	protected void insertDemoData() {
		new Ademo().forFields("aid", "atext", "bid");
		new Ademo().putValues("a1", "atext1", "b1").insert();
		new Ademo().putValues("a2", "atext1", "b1").insert();
		new Ademo().putValues("a3", "atext1", "b2").insert();

		new Bdemo().forFields("bid", "btext");
		new Bdemo().putValues("b1", "btext1").insert();
		new Bdemo().putValues("b2", "btext2").insert();

		new Cdemo().forFields("cid", "ctext", "aid");
		new Cdemo().putValues("c1", "ctext1", "a1").insert();
		new Cdemo().putValues("c2", "ctext2", "a1").insert();
		new Cdemo().putValues("c3", "ctext2", "a2").insert();

	}

	@Test
	public void testQuery1() {
		Systemout.println("\ntest testQuery1");
		insertDemoData();
		EntityNet net = ctx.qry(new EntityNetHandler(), Ademo.class, Bdemo.class, give("b", "a"),
				"select a.**, b.** from Ademo a, Bdemo b where a.bid=b.bid order by a.aid");
		List<Ademo> aList = net.pickEntityList("a");
		for (Ademo a : aList) {
			Systemout.println("Aid:" + a.getAid());
			Systemout.println("bid:" + a.getBdemo().getBid());
		}

	}

	@Test
	public void testQuery2() {
		Systemout.println("\ntest testQuery2");
		insertDemoData();
		EntityNet net = ctx.qry(new EntityNetHandler(), Ademo.class, Bdemo.class, Cdemo.class, give("b", "a"),
				give("c", "a", "cdemoList"), "select a.**, b.**, c.** from Ademo a", //
				" left join Bdemo b on b.bid=a.bid ", //
				" left join Cdemo c on c.aid=a.aid ", //
				" order by a.aid, a.bid, c.cid");
		List<Ademo> aList = net.pickEntityList("a");
		for (Ademo a : aList) {
			Systemout.println("Aid:" + a.getAid());
			Systemout.println("bid:" + a.getBdemo().getBid());
			Systemout.println("cid:" + a.getCdemoList());
		}

	}

}