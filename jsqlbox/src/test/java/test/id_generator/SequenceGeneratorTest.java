package test.id_generator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.id.GenerationType;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;

import test.config.TestPrepare;
import test.config.po.User;

public class SequenceGeneratorTest {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeBeanBoxContext();
	}

	@Test
	public void insertUser() {
		if (Dao.dao().getDatabaseType() != DatabaseType.ORACLE)
			return;
		User u = new User();
		u.dao().getContext().sequenceGenerator("seq2", "SEQ_2");
		u.dao().getBox().configGeneratedValue("age", GenerationType.SEQUENCE, "seq2");
		u.setUserName("User1");
		for (int i = 0; i < 60; i++)
			u.dao().insert();
		Assert.assertEquals(60, (int) u.dao().queryForInteger("select count(*) from ", u.table(), " where age>0"));

	}

}