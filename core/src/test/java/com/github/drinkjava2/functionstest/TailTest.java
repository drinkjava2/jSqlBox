package com.github.drinkjava2.functionstest;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.TAIL;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.eLoadBySQL;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.gctx;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.iExecute;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.tail;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdialects.annotation.jdia.PKey;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
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
		TailDemo t = eLoadBySQL(TailDemo.class, "select *, 'China' as address from tail_demo");
		Assert.assertEquals("China", t.getTail("address"));
		Assert.assertEquals("Tom", t.getUserName());
		t.putField("birthDay", new Date());
		t.update();

		iExecute("alter table tail_demo add address varchar(10)");
		gctx().reloadTailModels();
		t.putTail("address", "Canada");
		t.update(TAIL);

		t = eLoadBySQL(TailDemo.class, "select * from tail_demo");
		Assert.assertEquals("Canada", t.getTail("address"));
	}

	@Test
	public void tailTest() {
		new Tail().putTail("user_name", "Tom", "age", 10).insert(tail("tail_demo"));
		Tail t = eLoadBySQL(Tail.class, "select *, 'China' as address from tail_demo");
		Assert.assertEquals("China", t.getTail("address"));
		Assert.assertEquals("Tom", t.getTail("user_name"));
		t.update(tail("tail_demo"));

		iExecute("alter table tail_demo add address varchar(10)");
		gctx().reloadTailModels();
		t.putTail("address", "Canada");
		t.update(tail("tail_demo"));

		t = eLoadBySQL(Tail.class, "select * from tail_demo");
		Assert.assertEquals("Canada", t.getTail("address"));

		Assert.assertEquals(1, t.deleteTry(tail("tail_demo")));
	}

	@Test
	public void putValuesTest() {
		TailDemo t = new TailDemo();
		t.iExecute("delete from tail_demo");
		t.forFields("userName", "age", "birthDay");
		t.putValues("Foo", 10, new Date()).insert();
		t.putValues("Bar", 20, new Date()).insert();
		Assert.assertEquals(2, t.countAll());

		t.iExecute("delete from tail_demo");
		t.forTails("user_name", "age", "birth_Day");
		t.putValues("Foo", 30, new Date()).insert(TAIL);
		t.putValues("Bar", 40, new Date()).insert(TAIL);
		Assert.assertEquals(2, t.countAll());

		Tail tail = new Tail();
		tail.iExecute("delete from tail_demo");
		tail.forTails("user_name", "age", "birth_Day");
		tail.putValues("Foo", 30, new Date()).insert(tail("tail_demo"));
		tail.putValues("Bar", 40, new Date()).insert(tail("tail_demo"));
		Assert.assertEquals(2, t.countAll());

		Tail t2 = tail.loadById("Foo", tail("tail_demo")).putTail("age", 100).update(tail("tail_demo"))
				.load(tail("tail_demo"));
		Assert.assertEquals(100, t2.getTail("age"));

	}

}
