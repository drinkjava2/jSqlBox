package com.github.drinkjava2.jsqlbox.function.quote_column;

import static com.github.drinkjava2.jsqlbox.DB.IGNORE_EMPTY;
import static com.github.drinkjava2.jsqlbox.DB.IGNORE_NULL;
import static com.github.drinkjava2.jsqlbox.DB.TAIL;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jdia.UUID32;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jsqlbox.sqlitem.SampleItem;

/**
 * Test column name is database's keywords, quoted with ``, and for entity use
 * column with name =`xxx`
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class QuoteColumnMySqlTest extends TestBase {

	public static class CrudUser extends ActiveRecord<CrudUser> {
		@Id
		@Column(name = "`order`")
		String order;

		@Column(name = "`select`")
		String select;

		@UUID32
		String other;

		public CrudUser() {
		}

		public CrudUser(String order, String select) {
			this.order = order;
			this.select = select;
		}

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

		public String getOther() {
			return other;
		}

		public void setOther(String other) {
			this.other = other;
		}

	}

	@Test
	public void globalIgnoreNullTest() {
		if (!(dialect.isMySqlFamily() || dialect.isH2Family()))
			return;
		createAndRegTables(CrudUser.class);
		DbContext ctx1 = new DbContext(ctx.getDataSource());
		ctx1.setIgnoreNull(true);
		CrudUser u = new CrudUser("order_u", "select1");
		ctx1.eInsert(u);
		ctx1.setIgnoreEmpty(true);
		CrudUser u2 = new CrudUser("order_u2", "select2");
		ctx1.eInsert(u2);
		ctx1.setIgnoreNull(false);
		ctx1.setIgnoreEmpty(false);
		CrudUser u3 = new CrudUser("order_u3", "select3");
		ctx1.eInsert(u3);
		CrudUser u4 = new CrudUser("order_u4", "select4");
		ctx1.eInsert(u4, IGNORE_EMPTY);

		CrudUser u5 = ctx1.eLoadById(CrudUser.class, u4);
		Assert.assertEquals("order_u4", u5.order);
		Assert.assertEquals("select4", u5.select);
	}

	/**
	 * Test below CRUD methods:cc
	 * 
	 * insert,update,tryUpdate,delete,tryDelete,deleteById,tryDeleteById,load,
	 * tryLoad,loadById,tryLoadById,findAll,findAllByIds,countAll,exist,existById,
	 * findRelated
	 */
	@Test
	public void crudTest() {
		if (!(dialect.isMySqlFamily() || dialect.isH2Family()))
			return;
		createAndRegTables(CrudUser.class);
		// ======insert
		CrudUser u1 = new CrudUser("order1", "select1");
		CrudUser u2 = new CrudUser("order2", "");
		CrudUser u3 = new CrudUser("order3", "");
		CrudUser u4 = new CrudUser().putField("order", "order4", "select", "select4");
		ctx.eInsert(u1);
		u2.insert(IGNORE_NULL);
		u3.insert(IGNORE_EMPTY);
		u4.insert(IGNORE_NULL);

		// ======update
		u1.setSelect("Newselect1");
		u2.setSelect("Newselect2");
		u3.setSelect("Newselect3");
		u4.setSelect("Newselect4");
		ctx.eUpdate(u1);
		u2.update();
		Assert.assertEquals(1, ctx.eUpdateTry(u3));
		Assert.assertEquals(1, u4.updateTry());

		// =======load
		Assert.assertEquals("Newselect1", ctx.eLoad(u1).getSelect());
		Assert.assertEquals("Newselect2", u2.load().getSelect());
		Assert.assertEquals(1, ctx.eLoadTry(u3));
		Assert.assertEquals("Newselect3", u3.getSelect());
		Assert.assertEquals(1, u4.loadTry());
		Assert.assertEquals("Newselect4", u4.getSelect());

		// =======load by id (id is basic value)
		Assert.assertEquals("Newselect1", ctx.eLoadById(CrudUser.class, "order1").getSelect());
		Assert.assertEquals("Newselect2", u2.loadById("order2").getSelect());
		Assert.assertEquals("Newselect3", ctx.eLoadByIdTry(CrudUser.class, "order3").getSelect());
		Assert.assertEquals("Newselect4", u4.loadByIdTry("order4").getSelect());

		// =======load by id (id is map)
		Map<String, Object> mp = new HashMap<String, Object>();
		mp.put("order", "order1");
		Assert.assertEquals("Newselect1", ctx.eLoadById(CrudUser.class, mp).getSelect());
		mp.put("order", "order2");
		Assert.assertEquals("Newselect2", u2.loadById(mp).getSelect());
		mp.put("order", "order3");
		Assert.assertEquals("Newselect3", ctx.eLoadByIdTry(CrudUser.class, mp).getSelect());
		mp.put("order", "order4");
		Assert.assertEquals("Newselect4", u4.loadByIdTry(mp).getSelect());

		// =======load by id (id is Tail)
		CrudUser tail = new CrudUser().putTail("`order`", "order1");
		Assert.assertEquals("Newselect1", ctx.eLoadById(CrudUser.class, tail, TAIL).getSelect());
		tail.putTail("`order`", "order2");
		Assert.assertEquals("Newselect2", u2.loadById(tail, TAIL).getSelect());
		tail.putTail("`order`", "order3");
		Assert.assertEquals("Newselect3", ctx.eLoadByIdTry(CrudUser.class, tail, TAIL).getSelect());
		tail.putTail("`order`", "order4");
		Assert.assertEquals("Newselect4", u4.loadByIdTry(tail, TAIL).getSelect());

		// =======load by id (id is Entity bean)
		Assert.assertEquals("Newselect1", ctx.eLoadById(CrudUser.class, u1).getSelect());
		Assert.assertEquals("Newselect2", u1.loadById(u2).getSelect());
		Assert.assertEquals("Newselect3", ctx.eLoadByIdTry(CrudUser.class, u3).getSelect());
		Assert.assertEquals("Newselect4", u1.loadByIdTry(u4).getSelect());

		// =======findAll
		Assert.assertEquals(4, ctx.eFindAll(CrudUser.class).size());
		Assert.assertEquals(4, new CrudUser().findAll().size());

		// ========fidnBySql
		Assert.assertEquals(4, ctx.eFindBySQL(CrudUser.class, "select * from CrudUser").size());
		Assert.assertEquals(4, u1.findBySQL("select * from CrudUser").size());

		// ========findBySample
		Assert.assertEquals(1, ctx.eFindBySample(u1).size());
		Assert.assertEquals(1, ctx.eFindAll(CrudUser.class, new SampleItem(u2).sql(" where  ").notNullFields()).size());
		CrudUser sample = new CrudUser("ord", "select");
		Assert.assertEquals(4, ctx.eFindAll(CrudUser.class, new SampleItem(sample).sql(" where (").allFields()
				.sql(") or `order` like ?").param(":order%").sql(" order by `order`")).size());
		Assert.assertEquals(
				4, ctx
						.eFindBySQL(CrudUser.class, new SampleItem(sample).sql("select * from CrudUser where (")
								.nullFields().sql(") or `order` like ?").param(":order%").sql(" order by `order`"))
						.size());

		Assert.assertEquals(1, u2.findBySample().size());
		Assert.assertEquals(1, u1.eFindBySample(u2).size());
		Assert.assertEquals(1, u1.findAll(new SampleItem(u3).sql(" where  ").notNullFields()).size());
		Assert.assertEquals(4, u1.findAll(CrudUser.class, new SampleItem(sample).sql(" where (").allFields()
				.sql(") or `order` like ?").param("order%").sql(" order by `order`")).size());
		Assert.assertEquals(4,
				u1.findBySQL("select * from CrudUser where (",
						new SampleItem(sample).notNullFields().sql(") or `order` like ?").param(":order%"),
						" order by `order`").size());

		// =======countAll
		Assert.assertEquals(4, ctx.eCountAll(CrudUser.class));
		Assert.assertEquals(4, new CrudUser().countAll());

		// =======exist
		Assert.assertEquals(true, ctx.eExist(u1));
		Assert.assertEquals(true, u2.existId());

		// =======existStrict
		Assert.assertTrue(u1.existStrict());
		u1.setOther("othervalues");
		Assert.assertFalse(u1.existStrict());

		// =======existById (id is basic value)
		Assert.assertEquals(true, ctx.eExistById(CrudUser.class, "order1"));
		Assert.assertEquals(true, u1.existById("order2"));

		// =======existById (id is Map)
		Map<String, Object> m1 = new HashMap<String, Object>();
		m1.put("order", "order1");
		Map<String, Object> m2 = new HashMap<String, Object>();
		m2.put("order", "order2");
		Assert.assertEquals(true, ctx.eExistById(CrudUser.class, m1));
		Assert.assertEquals(true, u1.existById(m2));

		// =======existById (id is tail)
		CrudUser t1 = new CrudUser().putTail("`order`", "order1");
		CrudUser t2 = new CrudUser().putTail("`order`", "order2");
		Assert.assertEquals(true, ctx.eExistById(CrudUser.class, t1, TAIL));
		Assert.assertEquals(true, u1.existById(t2, TAIL));

		// =======existById (id is entity bean)
		Assert.assertEquals(true, ctx.eExistById(CrudUser.class, u1));
		Assert.assertEquals(true, u1.existById(u2));

		// =======delete
		ctx.eDelete(u1);
		u2.delete();
		Assert.assertEquals(1, ctx.eDeleteTry(u3));
		Assert.assertEquals(0, ctx.eDeleteTry(u3));
		Assert.assertEquals(1, u4.deleteTry());
		Assert.assertEquals(0, u4.deleteTry());
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is basic value)
		new CrudUser("order1", "select1").insert();
		new CrudUser("order2", "select2").insert();
		new CrudUser("order3", "select3").insert();
		new CrudUser("order4", "select4").insert();
		ctx.eDeleteById(CrudUser.class, "order1");
		u1.deleteById("order2");
		Assert.assertEquals(1, ctx.eDeleteByIdTry(CrudUser.class, "order3"));
		Assert.assertEquals(0, ctx.eDeleteByIdTry(CrudUser.class, "order3"));
		Assert.assertEquals(1, u1.deleteByIdTry("order4"));
		Assert.assertEquals(0, u1.deleteByIdTry("order4"));
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is Map)
		new CrudUser("order1", "select1").insert();
		new CrudUser("order2", "select2").insert();
		new CrudUser("order3", "select3").insert();
		new CrudUser("order4", "select4").insert();
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("order", "order1");
		ctx.eDeleteById(CrudUser.class, m);
		m.put("order", "order2");
		u1.deleteById(m);
		m.put("order", "order3");
		Assert.assertEquals(1, ctx.eDeleteByIdTry(CrudUser.class, m));
		Assert.assertEquals(0, ctx.eDeleteByIdTry(CrudUser.class, m));
		m.put("order", "order4");
		Assert.assertEquals(1, u1.deleteByIdTry(m));
		Assert.assertEquals(0, u1.deleteByIdTry(m));
		Assert.assertEquals(0, u1.countAll());

		// =======delete by id (id is Entity bean)
		u1 = new CrudUser("order1", "select1").insert();
		u2 = new CrudUser("order2", "select2").insert();
		u3 = new CrudUser("order3", "select3").insert();
		u4 = new CrudUser("order4", "select4").insert();
		ctx.eDeleteById(CrudUser.class, u1);
		u1.deleteById(u2);
		Assert.assertEquals(1, ctx.eDeleteByIdTry(CrudUser.class, u3));
		Assert.assertEquals(0, ctx.eDeleteByIdTry(CrudUser.class, u3));
		Assert.assertEquals(1, u1.deleteByIdTry(u4));
		Assert.assertEquals(0, u1.deleteByIdTry(u4));
		Assert.assertEquals(0, u1.countAll());
	}

}
