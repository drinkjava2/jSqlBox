package test.id_generator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.id.SequenceGenerator;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;

import test.config.TestPrepare;
import test.config.po.User;

/**
 * For this test, I made 2 sequences in Oracle, SEQ_1 is for "id" , SEQ_2 is for "age"
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class SequenceGeneratorTest {

	@Before
	public void setup() {
		TestPrepare.dropAndRecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDefaultContexts();
	}

	public static class SequenceGeneratorBox extends BeanBox {
		{
			this.setConstructor(SequenceGenerator.class, "SEQ_2");
		}
	}

	@Test
	public void insertUser() {
		if (SqlBox.getDefaultDatabaseType() != DatabaseType.ORACLE)
			return;
		User u = new User();
		u.box().configIdGenerator("age", BeanBox.getBean(SequenceGeneratorBox.class));
		u.setUserName("User1");
		for (int i = 0; i < 60; i++)
			u.insert();
		Assert.assertEquals(60, (int) SqlBox.queryForInteger("select count(*) from ", u.table(), " where age>0"));
	}

}