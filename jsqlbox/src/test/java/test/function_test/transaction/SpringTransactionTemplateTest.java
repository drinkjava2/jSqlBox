package test.function_test.transaction;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;

import test.config.JBeanBoxConfig.DataSourceBox;
import test.config.po.User;

/**
 * This is to test use Spring's TransactionTemplate to control transaction
 *
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */

public class SpringTransactionTemplateTest {

	@Test
	public void testTransactionTemplateCallback() {
		DataSource ds = BeanBox.getBean(DataSourceBox.class);
		SqlBoxContext.setDefaultSqlBoxContext(new SqlBoxContext(ds));

		Dao.executeQuiet("drop table users");// auto commit mode
		Dao.execute(User.ddl(Dao.getDialect()));// auto commit mode
		Dao.refreshMetaData();

		DataSourceTransactionManager tm = new DataSourceTransactionManager(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);

		try {
			// or tt.execute(status -> { });
			tt.execute(new TransactionCallback<Object>() {
				public Object doInTransaction(TransactionStatus status) {
					User u = new User();
					u.setId(u.nextUUID());
					u.insert();
					Dao.execute("insert into users (id) values (?)", empty(u.nextUUID()));
					Assert.assertEquals(2, (int) Dao.queryForInteger("select count(*) from users"));
					Dao.execute("A bad SQL");
					return null;
				}
			});
		} catch (BadSqlGrammarException e) {// should roll back
			Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users"));
		}
	}

}