package com.github.drinkjava2.functionstest;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.eLoadBySQL;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.iExecute;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.model;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdialects.annotation.jdia.PKey;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.Tail;

/**
 * This is Batch operation function test<br/>
 * note: only test on MySql, not on H2
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class TailTest extends TestBase {
	{
		regTables(TailSample.class);
	}

	@Table(name = "tail")
	public static class TailSample extends ActiveRecord<TailSample> {
		@PKey
		String name;
		Integer age;

		public String getName() {
			return name;
		}

		public TailSample setName(String name) {
			this.name = name;
			return this;
		}

		public Integer getAge() {
			return age;
		}

		public TailSample setAge(Integer age) {
			this.age = age;
			return this;
		}

	}

	@Test
	public void mixTailTest() {
		new TailSample().setName("Tom").setAge(10).insert();
		TailSample tail = eLoadBySQL(TailSample.class, "select *, 'China' as address from tail");
		Assert.assertEquals("China", tail.get("address"));
		Assert.assertEquals("Tom", tail.getName());

		iExecute("alter table tail add address varchar(10)");
		tail.put("address", "Canada");
		TableModel m=model(tail);
		m.addColumn("address");
		tail.update(m);

		tail = eLoadBySQL(TailSample.class, "select * from tail");
		Assert.assertEquals("Canada", tail.get("address"));
	}

	@Test
	public void tailTest() {
		TableModel m=model(TailSample.class); 
		new Tail().put("name", "Tom", "age", 10).insert(m);
		Tail t = eLoadBySQL(Tail.class, "select *, 'China' as address from tail");
		Assert.assertEquals("China", t.get("address"));
		Assert.assertEquals("Tom", t.get("name"));

		iExecute("alter table tail add address varchar(10)");
		t.put("address", "Canada");
		m.addColumn("address");
		t.update(m);

		t = eLoadBySQL(Tail.class, "select * from tail");
		Assert.assertEquals("Canada", t.get("address"));
	}

}
