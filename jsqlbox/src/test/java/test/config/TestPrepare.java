package test.config;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;

import test.config.JBeanBoxConfig.DataSourceBox;
import test.config.JBeanBoxConfig.TxInterceptorBox;
import test.config.po.DB;

/**
 * This is a configuration class, equal to XML in Spring
 *
 */
public class TestPrepare {

	/**
	 * Drop and rebuild all tables
	 */
	public static void dropAndRecreateTables() {
		BeanBox.defaultContext.close();
		BeanBox.defaultContext.setAOPAround("test.\\w*.\\w*", "tx_\\w*", new TxInterceptorBox(), "invoke");
		SqlBoxContext.defaultSqlBoxContext.setDataSource((DataSource) BeanBox.getBean(DataSourceBox.class));
		SqlBoxContext.defaultSqlBoxContext.setDbClass(DB.class);

		if (SqlBox.getDefaultDatabaseType() == DatabaseType.ORACLE) {
			SqlBox.executeQuiet("DROP TRIGGER TGR_2");
			SqlBox.executeQuiet("DROP SEQUENCE SEQ_2");
			SqlBox.executeQuiet("DROP TRIGGER TGR_1");
			SqlBox.executeQuiet("DROP SEQUENCE SEQ_1");
		}
		SqlBox.executeQuiet("drop table users");
		SqlBox.executeQuiet("drop table users2");

		if (SqlBox.getDefaultDatabaseType() == DatabaseType.MYSQL) {
			SqlBox.execute("create table users ", //
					"(id integer auto_increment ,", //
					"constraint const1 primary key (ID),", //
					"username Varchar (50) ,", //
					"PhoneNumber Varchar (50) ,", //
					"Address Varchar (50) ,", //
					"Alive Boolean, ", //
					"Age Integer )ENGINE=InnoDB DEFAULT CHARSET=utf8;");

			SqlBox.execute("create table users2", //
					"(id integer auto_increment ,", //
					"constraint const2 primary key (ID),", //
					"UserName Varchar (50) ,", //
					"PhoneNumber Varchar (50) ,", //
					"Address Varchar (50) ,", //
					"Alive Boolean, ", //
					"Age Integer )ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		}

		if (SqlBox.getDefaultDatabaseType() == DatabaseType.ORACLE) {
			SqlBox.execute("CREATE TABLE USERS", //
					"(ID INTEGER,", //
					"USERNAME VARCHAR (50) ,", //
					"PHONENUMBER VARCHAR (50) ,", //
					"ADDRESS VARCHAR (50) ,", //
					"ALIVE INTEGER, ", //
					"AGE INTEGER)");
			SqlBox.execute("CREATE TABLE USERS2", //
					"(ID INTEGER,", //
					"USERNAME VARCHAR (50) ,", //
					"PHONENUMBER VARCHAR (50) ,", //
					"ADDRESS VARCHAR (50) ,", //
					"ALIVE INTEGER, ", //
					"AGE INTEGER)");
			SqlBox.execute(
					"CREATE SEQUENCE SEQ_1 MINVALUE 1 MAXVALUE 99999999 START WITH 1 INCREMENT BY 1 NOCYCLE CACHE 10");
			SqlBox.execute(
					"CREATE TRIGGER TGR_1 BEFORE INSERT ON USERS FOR EACH ROW BEGIN SELECT SEQ_1.NEXTVAL INTO:NEW.ID FROM DUAL; END;");
			SqlBox.execute(
					"CREATE SEQUENCE SEQ_2 MINVALUE 1 MAXVALUE 99999999 START WITH 1 INCREMENT BY 1 NOCYCLE CACHE 10");
			SqlBox.execute(
					"CREATE TRIGGER TGR_2 BEFORE INSERT ON USERS2 FOR EACH ROW BEGIN SELECT SEQ_2.NEXTVAL INTO:NEW.ID FROM DUAL; END;");
		}

		SqlBox.refreshMetaData();
	}

	/**
	 * Close BeanBox Context, c3p0 close method will be called before context be closed
	 */
	public static void closeBeanBoxContext() {
		BeanBox.defaultContext.close();
	}

	@Test
	public void testCreateTables() {
		dropAndRecreateTables();
		Assert.assertEquals(0, (int) SqlBox.queryForInteger("select count(*) from users"));
		Assert.assertEquals(0, (int) SqlBox.queryForInteger("select count(*) from users2"));
		closeBeanBoxContext();
	}

}
