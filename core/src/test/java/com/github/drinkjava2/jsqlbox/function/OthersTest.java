package com.github.drinkjava2.jsqlbox.function;

import static com.github.drinkjava2.jsqlbox.DB.getOthers;
import static com.github.drinkjava2.jsqlbox.DB.other;
import static com.github.drinkjava2.jsqlbox.DB.que;
import static com.github.drinkjava2.jsqlbox.DB.when;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import com.github.drinkjava2.jdbpro.handler.PrintSqlHandler;
import com.github.drinkjava2.jdialects.annotation.jdia.PKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID26;
import com.github.drinkjava2.jsqlbox.ActiveEntity;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * This is unit test for test @Version annotation
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public class OthersTest extends TestBase {
	{
		regTables(TitleDemoEntity.class);
	}

	@Test
	public void doTest() {
		TitleDemoEntity demo = new TitleDemoEntity();
		demo.setName("Sam");
		demo.insert();
		String name = "a";
		Map<String, Object> result = DB.qryMap("select ", //
				" id", other("id", 10), //
				when(false, ", name as name1 ", other("name1", "age=5")), //
				when(true, ", name as name2 ", other("name2", "displayWidth=10")), //
				" from TitleDemoEntity", //
				" where id<>", que("a"), //
				when(name != null, " and name like ", que("%" + name + "%")), //
				new PrintSqlHandler() //
		);
		System.out.println(result);
		for (Object[] titles : getOthers())
			System.out.println(Arrays.deepToString(titles));
	}

	public static class TitleDemoEntity implements ActiveEntity<TitleDemoEntity> {
		@PKey
		@UUID26
		private String id;

		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

}
