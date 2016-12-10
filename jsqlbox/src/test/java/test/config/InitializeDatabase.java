package test.config;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;

/**
 * This is a configuration class, equal to XML in Spring
 *
 */
public class InitializeDatabase {

	public static void dropAndRecreateTables() {
		SqlBoxContext.configDefaultContext(SqlBoxConfig.class.getName(), "getSqlBoxContext");
		JBeanBoxConfig.initialize();

		if (Dao.dao().getDatabaseType() == DatabaseType.ORACLE) {
			Dao.dao().executeQuiet("DROP TRIGGER TGR_2");
			Dao.dao().executeQuiet("DROP SEQUENCE SEQ_2");
			Dao.dao().executeQuiet("DROP TRIGGER TGR_1");
			Dao.dao().executeQuiet("DROP SEQUENCE SEQ_1");
		}
		Dao.dao().executeQuiet("drop table users");
		Dao.dao().executeQuiet("drop table users2");

		if (Dao.dao().getDatabaseType() == DatabaseType.MYSQL) {
			Dao.dao().execute("create table users ", //
					"(id integer auto_increment ,", //
					"constraint const1 primary key (ID),", //
					"username Varchar (50) ,", //
					"PhoneNumber Varchar (50) ,", //
					"Address Varchar (50) ,", //
					"Alive Boolean, ", //
					"Age Integer )ENGINE=InnoDB DEFAULT CHARSET=utf8;");

			Dao.dao().execute("create table users2", //
					"(id integer auto_increment ,", //
					"constraint const2 primary key (ID),", //
					"UserName Varchar (50) ,", //
					"PhoneNumber Varchar (50) ,", //
					"Address Varchar (50) ,", //
					"Alive Boolean, ", //
					"Age Integer )ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		}

		if (Dao.dao().getDatabaseType() == DatabaseType.ORACLE) {
			Dao.dao().execute("CREATE TABLE USERS", //
					"(ID INTEGER,", //
					"USERNAME VARCHAR (50) ,", //
					"PHONENUMBER VARCHAR (50) ,", //
					"ADDRESS VARCHAR (50) ,", //
					"ALIVE INTEGER, ", //
					"AGE INTEGER)");
			Dao.dao().execute("CREATE TABLE USERS2", //
					"(ID INTEGER,", //
					"USERNAME VARCHAR (50) ,", //
					"PHONENUMBER VARCHAR (50) ,", //
					"ADDRESS VARCHAR (50) ,", //
					"ALIVE INTEGER, ", //
					"AGE INTEGER)");
			Dao.dao().execute(
					"CREATE SEQUENCE SEQ_1 MINVALUE 1 MAXVALUE 99999999 START WITH 1 INCREMENT BY 1 NOCYCLE CACHE 10");
			Dao.dao().execute(
					"CREATE TRIGGER TGR_1 BEFORE INSERT ON USERS FOR EACH ROW BEGIN SELECT SEQ_1.NEXTVAL INTO:NEW.ID FROM DUAL; END;");
			Dao.dao().execute(
					"CREATE SEQUENCE SEQ_2 MINVALUE 1 MAXVALUE 99999999 START WITH 1 INCREMENT BY 1 NOCYCLE CACHE 10");
			Dao.dao().execute(
					"CREATE TRIGGER TGR_2 BEFORE INSERT ON USERS2 FOR EACH ROW BEGIN SELECT SEQ_2.NEXTVAL INTO:NEW.ID FROM DUAL; END;");
		}

		Dao.dao().refreshMetaData();
	}

	@Test
	public void testCreateTables() {
		dropAndRecreateTables();
		Assert.assertEquals(0, (int) Dao.dao().queryForInteger("select count(*) from users"));
		Assert.assertEquals(0, (int) Dao.dao().queryForInteger("select count(*) from users2"));
	}

	public static void main(String[] args) {
		dropAndRecreateTables();
	}

}
