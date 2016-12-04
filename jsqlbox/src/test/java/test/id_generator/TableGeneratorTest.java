package test.id_generator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.jpa.GenerationType;

import test.config.InitializeDatabase;
import test.config.po.User;

public class TableGeneratorTest {

	@Before
	public void setup() {
		InitializeDatabase.dropAndRecreateTables();
	}

	@Test
	public void insertUser1() {
		User u = new User();
		u.dao().executeInSilence("drop table temp_pk_table");
		u.dao().box().configTableGenerator("creator1", "temp_pk_table", "pk_col_name", "pk_col_val",
				"val_col_name", 1, 50);
		u.dao().box().configIdGenerator(User.Age, GenerationType.TABLE, "creator1");
		u.setUserName("User1");
		u.dao().save();
		Assert.assertEquals(1,
				(int) Dao.dao().queryForInteger("select count(*) from " + u.Table() + " where " + u.Age() + ">0 "));
	}

}