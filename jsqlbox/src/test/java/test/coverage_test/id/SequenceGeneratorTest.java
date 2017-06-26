package test.coverage_test.id;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.Column;
import com.github.drinkjava2.jdialects.model.Table;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.id.SequenceGenerator;

import test.TestBase;
import test.config.entity.User;

/**
 * For this test, I made 2 sequences in Oracle, SEQ_1 is for "id" , SEQ_2 is for
 * "age"
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class SequenceGeneratorTest extends TestBase {// TODO fix this sequence
														// test

	public static class SequenceGeneratorBox extends BeanBox {
		{
			this.setConstructor(SequenceGenerator.class, "age_seq");
		}
	}

	@Test
	public void insertUser() {
		Dialect d = Dao.getDialect();
		System.out.println("Current Dialect=" + Dao.getDialect());
		if (!d.getDdlFeatures().supportBasicOrPooledSequence())
			return;
		Table t = User.model();
		t.addSequence("AGE_SEQ", "AGE_SEQ", 1, 1);
		Column col = User.model().getColumn("Age");
		System.out.println("col=" + col);
		Dao.executeManyQuiet(d.toDropAndCreateDDL(t));
		Dao.refreshMetaData();

		User u = new User();
		u.box().configIdGenerator("age", BeanBox.getBean(SequenceGeneratorBox.class));
		u.setUserName("User1");
		for (int i = 0; i < 60; i++)
			u.insert();
		Assert.assertEquals(60, (int) Dao.queryForInteger("select count(*) from ", u.table(), " where age>0"));
	}

}