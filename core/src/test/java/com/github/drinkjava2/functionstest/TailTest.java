package com.github.drinkjava2.functionstest;

import static com.github.drinkjava2.jdbpro.SqlOption.WITH_TAIL;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.eFindBySQL;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.iExecute;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdialects.annotation.jdia.PKey;
import com.github.drinkjava2.jsqlbox.AR;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

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
	public void doTailTest() {
		new TailSample().setName("Tom").setAge(10).insert();
		List<TailSample> tailList = eFindBySQL(TailSample.class, "select *, 'China' as address from TailSample");
		TailSample tail = tailList.get(0);
		Assert.assertEquals("China", tail.get("address"));
		Assert.assertEquals("Tom", tail.get("name"));

		iExecute("alter table TailSample add address varchar(10)");
		tail.put("address", "Canada");
		tail.update(WITH_TAIL);

		tailList = eFindBySQL(TailSample.class, "select * from TailSample");
		tail = tailList.get(0);
		Assert.assertEquals("Canada", tail.get("address"));
	}

	@Test
	public void activeRecordTailTest() {
		new AR("TailSample").put("name", "Tom", "age", 10).insert(WITH_TAIL);
		List<AR> tailList = eFindBySQL(AR.class, "select *, 'China' as address from TailSample");
		AR ar = tailList.get(0);
		Assert.assertEquals("China", ar.get("address"));
		Assert.assertEquals("Tom", ar.get("name"));

		iExecute("alter table TailSample add address varchar(10)");
		ar.put("address", "Canada");
		ar.model().setTableName("TailSample");
		ar.model().column("name").id();
		ar.update(WITH_TAIL);

		tailList = eFindBySQL(AR.class, "select * from TailSample");
		ar = tailList.get(0);
		Assert.assertEquals("Canada", ar.get("address"));
	}

}
