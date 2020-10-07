package com.github.drinkjava2.jsqlbox.function;

import static com.github.drinkjava2.jsqlbox.DB.TAIL;
import static com.github.drinkjava2.jsqlbox.DB.entityLoadBySql;
import static com.github.drinkjava2.jsqlbox.DB.exe;
import static com.github.drinkjava2.jsqlbox.DB.gctx;
import static com.github.drinkjava2.jsqlbox.DB.tail;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jdia.PKey;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.Tail;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * This is Batch operation function test<br/>
 * note: only test on MySql, not on H2
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class TailTest extends TestBase {
	{
		regTables(TailDemo.class);
	}

	@Table(name = "tail_demo")
	public static class TailDemo extends ActiveRecord<TailDemo> {
		@PKey
		@Column(name = "user_name")
		String userName;

		Integer age;

		@Column(name = "birth_day")
		Date birthDay;
		
		public String getUserName() {
			return userName;
		}

		public TailDemo setUserName(String userName) {
			this.userName = userName;
			return this;
		}

		public Integer getAge() {
			return age;
		}

		public TailDemo setAge(Integer age) {
			this.age = age;
			return this;
		}

		public Date getBirthDay() {
			return birthDay;
		}

		public void setBirthDay(Date birthDay) {
			this.birthDay = birthDay;
		}

	}

	@Test
	public void mixTailTest() {
		new TailDemo().setUserName("Tom").putTail("age", 10).insert(TAIL);
		TailDemo t = entityLoadBySql(TailDemo.class, "select 'China' as address, tail_demo.* from tail_demo");
		Assert.assertEquals("China", t.getTail("address"));
		Assert.assertEquals("Tom", t.getUserName());
		t.putField("birthDay", new Date());
		t.putField("age", 10);
		t.update();

		exe("alter table tail_demo add address varchar(10)");
		gctx().reloadTailModels();
		t.putTail("address", "Canada");
		t.update(TAIL);

		t = entityLoadBySql(TailDemo.class, "select * from tail_demo");
		Assert.assertEquals("Canada", t.getTail("address"));
	}

	@Test
	public void tailTest() {
		new Tail().putTail("user_name", "Tom", "age", 10).insert(tail("tail_demo"));
		Tail t = entityLoadBySql(Tail.class, "select 'China' as address, tail_demo.* from tail_demo");
		Assert.assertEquals("China", t.getTail("address"));
		Assert.assertEquals("Tom", t.getTail("user_name"));
		t.update(tail("tail_demo"));

		exe("alter table tail_demo add address varchar(10)");
		gctx().reloadTailModels();
		t.putTail("address", "Canada");
		t.update(tail("tail_demo"));

		t = entityLoadBySql(Tail.class, "select * from tail_demo");
		Assert.assertEquals("Canada", t.getTail("address"));

		Assert.assertEquals(1, t.deleteTry(tail("tail_demo")));
	}

	@Test
	public void putValuesTest() {
		TailDemo t = new TailDemo();
		DB.exe("delete from tail_demo");
		t.forFields("userName", "age", "birthDay");
		t.putValues("Foo", 10, new Date()).insert();
		t.putValues("Bar", 20, new Date()).insert();
		Assert.assertEquals(2, t.countAll());

		DB.exe("delete from tail_demo");
		t.forTails("user_name", "age", "birth_Day");
		t.putValues("Foo", 30, new Date()).insert(TAIL);
		t.putValues("Bar", 40, new Date()).insert(TAIL);
		Assert.assertEquals(2, t.countAll());

		Tail tail = new Tail();
		DB.exe("delete from tail_demo");
		tail.forTails("user_name", "age", "birth_Day");
		tail.putValues("Foo", 30, new Date()).insert(tail("tail_demo"));
		tail.putValues("Bar", 40, new Date()).insert(tail("tail_demo"));
		Assert.assertEquals(2, t.countAll());

		Tail t2 = tail.loadById("Foo", tail("tail_demo")).putTail("age", 100).update(tail("tail_demo"))
				.load(tail("tail_demo"));
		Assert.assertEquals("100", ""+t2.getTail("age"));

	}

}
