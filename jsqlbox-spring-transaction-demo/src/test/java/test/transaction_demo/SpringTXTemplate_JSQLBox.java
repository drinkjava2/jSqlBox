package test.transaction_demo;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.thinjdbc.jdbc.BadSqlGrammarException;

import test.TestBase;
import test.config.DataSourceConfig.DataSourceBox;
import test.config.entity.User;

/**
 * This is to test use Spring's TransactionTemplate to control transaction
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */

public class SpringTXTemplate_JSQLBox extends TestBase {

	@Test
	public void testTransactionTemplateCallback() {
		DataSourceTransactionManager tm = new DataSourceTransactionManager(BeanBox.getBean(DataSourceBox.class));
		TransactionTemplate tt = new TransactionTemplate(tm);

		try {
			// In Java8 is: tt.execute(status -> { });
			tt.execute(new TransactionCallback<Object>() {
				public Object doInTransaction(TransactionStatus status) {
					User u = new User();
					u.setId(u.nextUUID());
					u.insert();
					Dao.execute("insert into users (id) values (?)", empty(u.nextUUID()));
					Assert.assertEquals(2, (int) Dao.queryForInteger("select count(*) from users"));
					System.out.println("2 records inserted into database");
					Dao.execute("A bad SQL");
					return null;
				}
			});
		} catch (BadSqlGrammarException e) {// should roll back
			Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users"));
			System.out.println("Exception found and roll back");
		}
	}
}