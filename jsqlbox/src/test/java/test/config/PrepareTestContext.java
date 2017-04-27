package test.config;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.JBeanBoxConfig.DefaultSqlBoxContextBox;
import test.config.po.User;

/**
 * This is a configuration class, equal to XML in Spring
 *
 */
public class PrepareTestContext {

	/**
	 * Drop and rebuild all tables
	 */
	public static void prepareDatasource_setDefaultSqlBoxConetxt_recreateTables() {
		BeanBox.defaultContext.close();
		SqlBoxContext.setDefaultSqlBoxContext(BeanBox.getBean(DefaultSqlBoxContextBox.class)); 
		System.out.println("Drop and re-create a demo \"users\" table for next unit test ...");
		Dao.executeQuiet("drop table users");
		Dao.execute(User.ddl(Dao.getDialect()));
		Dao.refreshMetaData();
	}

	/**
	 * Drop and rebuild all tables
	 */
	@Deprecated() // will delete
	public static void prepareDatasource_setDefaultSqlBoxConetxt_recreateTablesOld() {
		Dialect d = Dao.getDialect();

		System.out.println("Drop and re-create all tables for a new test ...");
		if (d.isOracleFamily()) {
			Dao.executeQuiet("DROP TRIGGER TGR_2");
			Dao.executeQuiet("DROP SEQUENCE SEQ_2");
			Dao.executeQuiet("DROP TRIGGER TGR_1");
			Dao.executeQuiet("DROP SEQUENCE SEQ_1");
		}

		if (d.isMySqlFamily() || d.isH2Family()) {
		}
		if (d.isSQLServerFamily()) {
		}

		if (d.isOracleFamily()) {

			Dao.execute(
					"CREATE SEQUENCE SEQ_1 MINVALUE 1 MAXVALUE 99999999 START WITH 1 INCREMENT BY 1 NOCYCLE CACHE 10");
			Dao.execute(
					"CREATE TRIGGER TGR_1 BEFORE INSERT ON USERS FOR EACH ROW BEGIN SELECT SEQ_1.NEXTVAL INTO:NEW.ID FROM DUAL; END;");
			Dao.execute(
					"CREATE SEQUENCE SEQ_2 MINVALUE 1 MAXVALUE 99999999 START WITH 1 INCREMENT BY 1 NOCYCLE CACHE 10");
			Dao.execute(
					"CREATE TRIGGER TGR_2 BEFORE INSERT ON USERS2 FOR EACH ROW BEGIN SELECT SEQ_2.NEXTVAL INTO:NEW.ID FROM DUAL; END;");
		}
		Dao.refreshMetaData();
	}

	/**
	 * Close BeanBox Context
	 */
	public static void closeDatasource_closeDefaultSqlBoxConetxt() {
		BeanBox.defaultContext.close();// This will close HikariDataSource
										// because preDestroy method set to
										// "Close"
		SqlBoxContext.getDefaultSqlBoxContext().close();
	}

	@Test
	public void testCreateTables() {
		System.out.println("===============================Testing TestPrepare===============================");
		prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users"));
		closeDatasource_closeDefaultSqlBoxConetxt();
	}

}
