package com.github.drinkjava2.functionstest.jdialects;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.Test;

import com.github.drinkjava2.config.DataSourceConfig.DataSourceBox;
import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jdialects.DDLFormatter;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.TableModelUtilsOfDb;
import com.github.drinkjava2.jdialects.TableModelUtilsOfJavaSrc;
import com.github.drinkjava2.jdialects.annotation.jdia.FKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * Test Date time type
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class TableModelUtilsOfDbTest extends TestBase {
	{
		regTables(studentSample.class);
		regTables(DbSample.class);
	}

	@Table(name = "student_sample")
	public static class studentSample {
		@Id
		String name;

		@Id
		String address;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}
	}

	@FKey(name = "fkey1", ddl = true, columns = { "address", "email" }, refs = { "student_sample", "address", "email" })
	public static class DbSample {
		@Id
		@UUID25
		String id;

		@Id
		String name;

		String address;

		String email;

		String studentCode;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getStudentCode() {
			return studentCode;
		}

		public void setStudentCode(String studentCode) {
			this.studentCode = studentCode;
		}
	}

	public static class DbSample2 {
		@Id
		String id;

	}

	@Test
	public void doDbToModelTest() throws Exception {
		DataSource ds = JBEANBOX.getBean(DataSourceBox.class);
		Connection conn = null;
		conn = ds.getConnection();
		Dialect dialect = Dialect.guessDialect(conn);
		TableModel[] models = TableModelUtilsOfDb.db2Model(conn, dialect);
		for (TableModel model : models) {
			for (String ddl : dialect.toCreateDDL(model)) {
				System.out.println(DDLFormatter.format(ddl));
			}

			System.out.println(TableModelUtilsOfJavaSrc.modelToJavaSourceCode(model, true, true, "somepackage"));
		}
		conn.close();
	}

}
