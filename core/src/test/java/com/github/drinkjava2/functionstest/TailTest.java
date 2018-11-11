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
import com.github.drinkjava2.jsqlbox.TailType;

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

	@Table(name = "tailTb")
	public static class TailSample extends ActiveRecord<TailSample> {
		@PKey
		@Column(name = "user_name")
		String userName;

		Integer age;

		@Column(name = "birth_day")
		Date birthDay;

		public String getUserName() {
			return userName;
		}

		public TailSample setUserName(String userName) {
			this.userName = userName;
			return this;
		}

		public Integer getAge() {
			return age;
		}

		public TailSample setAge(Integer age) {
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
		new TailSample().setUserName("Tom").putTail("age", 10).insert(TAIL);
		TailSample t = eLoadBySQL(TailSample.class, "select *, 'China' as address from tailTb");
		Assert.assertEquals("China", t.getTail("address"));
		Assert.assertEquals("Tom", t.getUserName());
		t.putField("birthDay", new Date());
		t.update();

		iExecute("alter table tailTb add address varchar(10)");
		gctx().reloadTailModels();
		t.putTail("address", "Canada");
		t.update(TAIL);

		t = eLoadBySQL(TailSample.class, "select * from tailTb");
		Assert.assertEquals("Canada", t.getTail("address"));
	}

	@Test
	public void tailTest() { new Tail().putTail("user_name", "Tom", "age", 10).insert(tail("tailTb"));
		Tail t = eLoadBySQL(Tail.class, "select *, 'China' as address from tailTb");
		Assert.assertEquals("China", t.getTail("address"));
		Assert.assertEquals("Tom", t.getTail("user_name"));
		t.update(tail("tailTb"));

		iExecute("alter table tailTb add address varchar(10)");
		gctx().reloadTailModels();
		t.putTail("address", "Canada" );
		t.update(tail("tailTb"));

		t = eLoadBySQL(Tail.class, "select * from tailTb");
		Assert.assertEquals("Canada", t.getTail("address"));

		Assert.assertEquals(1, t.deleteTry(tail("tailTb")));
	}

}
