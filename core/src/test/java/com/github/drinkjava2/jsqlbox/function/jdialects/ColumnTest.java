package com.github.drinkjava2.jsqlbox.function.jdialects;
/*- JAVA8_BEGIN */

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.COLUMN;
import com.github.drinkjava2.jdialects.annotation.jdia.CreateTimestamp;
import com.github.drinkjava2.jdialects.annotation.jdia.CreatedBy;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jdia.UpdateTimestamp;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Temporal;
import com.github.drinkjava2.jdialects.annotation.jpa.TemporalType;
import com.github.drinkjava2.jdialects.id.UUID25Generator;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveEntity;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * To test COLUMN annotation
 * 
 * @author Yong Zhu
 * @since 4.0.2
 */
public class ColumnTest extends TestBase implements ActiveEntity<ColumnTest> {

	private void useField(int x) {// Use jDialect's dynamic configuration to disable other fields
		TableModel model = TableModelUtils.entity2Model(ColumnTest.class);
		for (int i = 1; i <= 20; i++)
			try {
				model.getColumn("c" + i).setTransientable(true);
			} catch (Exception e) {
			}
		model.column("c" + x).setTransientable(false);
		TableModelUtils.bindGlobalModel(ColumnTest.class, model);
		quietCreateRegTables(ColumnTest.class);
	}

	java.util.Date c1;

	@Test
	public void testC1() {
		useField(1);
		ColumnTest in = new ColumnTest();
		in.insert();
		ColumnTest out = new ColumnTest().setId(in.getId()).load();
		Assert.assertNull(out.getC1());

		in.update();
		ColumnTest out2 = new ColumnTest().setId(in.getId()).load();
		Assert.assertNull(out2.getC1());
	}

	@Temporal(TemporalType.TIMESTAMP)
	@CreateTimestamp // or @COLUMN(createTimestamp = true)
	java.util.Date c2;

	@Test
	public void testC2() {
		useField(2);
		ColumnTest in = new ColumnTest();
		in.insert();
		ColumnTest out = new ColumnTest().setId(in.getId()).load();
		Assert.assertNotNull(in.getC2());
		Assert.assertNotNull(out.getC2());
		Date old = out.getC2();

		out.update();
		ColumnTest out2 = new ColumnTest().setId(in.getId()).load();
		Assert.assertEquals(old, out2.getC2());
	}

	@Temporal(TemporalType.TIMESTAMP)
	@UpdateTimestamp // or @COLUMN(updateTimestamp = true)
	java.util.Date c3;

	@Test
	public void testC3() {
		useField(3);
		ColumnTest in = new ColumnTest();
		in.insert();
		ColumnTest out = new ColumnTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getC3());
		Date old = out.getC3();
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}

		out.update();
		ColumnTest out2 = new ColumnTest().setId(in.getId()).load();
		Assert.assertNotEquals(old, out2.getC3());
	}

	public static class GetUserIdDemo {
		public Object getCurrentAuditor() {
			return UUID25Generator.getUUID25();// should get from Shiro
		}
	}

	@CreatedBy // or @COLUMN(createdBy = true)
	String c4;

	@Test
	public void testC4() {
		JBEANBOX.bind("AuditorAware", GetUserIdDemo.class);
		useField(4);
		ColumnTest in = new ColumnTest();
		in.insert();
		ColumnTest out = new ColumnTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getC4());
		String old = out.getC4();
		in.update();
		out = new ColumnTest().setId(in.getId()).load();
		Assert.assertEquals(old, out.getC4());
	}

	@COLUMN(lastModifiedBy = true) // or @LastModifiedBy
	String c5;

	@Test
	public void testC5() {
		JBEANBOX.bind("AuditorAware", GetUserIdDemo.class);
		useField(5);
		ColumnTest in = new ColumnTest();
		in.insert();
		ColumnTest out = new ColumnTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getC5());
		String old = out.getC5();
		in.update();
		out = new ColumnTest().setId(in.getId()).load();
		Assert.assertNotNull(out.getC5());
		Assert.assertNotEquals(old, out.getC5());
	}

	// getter & setter ======
	@Id
	@UUID25
	String id;

	public String getId() {
		return id;
	}

	public ColumnTest setId(String id) {
		this.id = id;
		return this;
	}

	public java.util.Date getC1() {
		return c1;
	}

	public void setC1(java.util.Date c1) {
		this.c1 = c1;
	}

	public java.util.Date getC2() {
		return c2;
	}

	public void setC2(java.util.Date c2) {
		this.c2 = c2;
	}

	public java.util.Date getC3() {
		return c3;
	}

	public void setC3(java.util.Date c3) {
		this.c3 = c3;
	}

	public String getC4() {
		return c4;
	}

	public void setC4(String c4) {
		this.c4 = c4;
	}

	public String getC5() {
		return c5;
	}

	public void setC5(String c5) {
		this.c5 = c5;
	}

}
/* JAVA8_END */