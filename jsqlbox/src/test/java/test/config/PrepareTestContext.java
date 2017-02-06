package test.config;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.JBeanBoxConfig.DefaultSqlBoxContextBox;

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

		System.out.println("Drop and re-create all tables for a new test ...");
		if (Dao.getDefaultDatabaseType().isOracle()) {
			Dao.executeQuiet("DROP TRIGGER TGR_2");
			Dao.executeQuiet("DROP SEQUENCE SEQ_2");
			Dao.executeQuiet("DROP TRIGGER TGR_1");
			Dao.executeQuiet("DROP SEQUENCE SEQ_1");
		}
		Dao.executeQuiet("drop table email");
		Dao.executeQuiet("drop table users");
		Dao.executeQuiet("drop table users2");

		String innoDB = Dao.getDefaultDatabaseType().isMySql() ? "ENGINE=InnoDB DEFAULT CHARSET=utf8;" : "";
		if (Dao.getDefaultDatabaseType().isMySql() || Dao.getDefaultDatabaseType().isH2()) {
			Dao.execute("create table users ", //
					"(id integer auto_increment ,", //
					"username Varchar (50) ,", //
					"Phone_Number Varchar (50) ,", //
					"Address Varchar (50) ,", //
					"active Boolean, ", //
					"Age Integer,", //
					"constraint const1 primary key (id)", //
					")", innoDB);

			Dao.execute("create table users2", //
					"(id integer auto_increment ,", //
					"constraint const4 primary key (ID),", //
					"username Varchar (50) ,", //
					"Phone_Number Varchar (50) ,", //
					"Address Varchar (50) ,", //
					"active Boolean, ", //
					"Age Integer)", innoDB);
		}
		if (Dao.getDefaultDatabaseType().isMsSQLSERVER()) {
			Dao.execute("create table users ", //
					"(id integer identity(1,1),", //
					"username Varchar (50) ,", //
					"Phone_Number Varchar (50) ,", //
					"Address Varchar (50) ,", //
					"active bit, ", //
					"Age Integer,", //
					"constraint const1 primary key (id)", //
					")");

			Dao.execute("create table users2", //
					"(id integer identity(1,1),", //
					"constraint const4 primary key (ID),", //
					"username Varchar (50) ,", //
					"Phone_Number Varchar (50) ,", //
					"Address Varchar (50) ,", //
					"active bit, ", //
					"Age Integer)");
		}

		if (Dao.getDefaultDatabaseType().isOracle()) {
			Dao.execute("CREATE TABLE USERS", //
					"(ID INTEGER,", //
					"USERNAME VARCHAR (50) ,", //
					"PHONE_NUMBER VARCHAR (50) ,", //
					"ADDRESS VARCHAR (50) ,", //
					"ACTIVE NUMBER(8), ", //
					"AGE NUMBER(8),", //
					"CONSTRAINT CONST1 PRIMARY KEY (ID)", //
					")");

			Dao.execute("CREATE TABLE USERS2", //
					"(ID INTEGER,", //
					"USERNAME VARCHAR (50) ,", //
					"PHONE_NUMBER VARCHAR (50) ,", //
					"ADDRESS VARCHAR (50) ,", //
					"ACTIVE NUMBER(8), ", //
					"AGE NUMBER(8)", //					
					")");
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
		BeanBox.defaultContext.close();// This will close HikariDataSource because preDestroy method set to "Close"
		SqlBoxContext.getDefaultSqlBoxContext().close();
	}

	@Test
	public void testCreateTables() {
		System.out.println("===============================Testing TestPrepare===============================");
		prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users"));
		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users2"));
		closeDatasource_closeDefaultSqlBoxConetxt();
	}

	public static void main(String[] args) {
		prepareDatasource_setDefaultSqlBoxConetxt_recreateTables();
		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users"));
		Assert.assertEquals(0, (int) Dao.queryForInteger("select count(*) from users2"));
		closeDatasource_closeDefaultSqlBoxConetxt();
	}
}
