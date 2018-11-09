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
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.model.TableModel;

/**
 * Test Date time type
 * 
 * @author Yong Zhu
 * @since 1.7.0
 */
public class TableModelUtilsOfDbTest extends TestBase {
	{
		regTables(DbSample.class);
	}

	public static class DbSample {
		@Id
		@UUID25
		String id;

		@Column(columnDefinition = "TIMESTAMP")
		java.util.Date d1;

		java.util.Date d2;

		java.sql.Date d3;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public java.util.Date getD1() {
			return d1;
		}

		public void setD1(java.util.Date d1) {
			this.d1 = d1;
		}

		public java.util.Date getD2() {
			return d2;
		}

		public void setD2(java.util.Date d2) {
			this.d2 = d2;
		}

		public java.sql.Date getD3() {
			return d3;
		}

		public void setD3(java.sql.Date d3) {
			this.d3 = d3;
		}
	}

	@Test
	public void doDbToModelTest() throws Exception {
		DataSource ds = JBEANBOX.getBean(DataSourceBox.class);
		Connection conn = null;
		conn = ds.getConnection();
		Dialect dialect = Dialect.guessDialect(conn);
		TableModel[] models = TableModelUtilsOfDb.db2Model(conn, dialect);
		for (TableModel model : models) {
			System.out.println(model.getDebugInfo());
			for (String ddl : dialect.toCreateDDL(model)) {
				System.out.println(DDLFormatter.format(ddl));
			}
		}
		conn.close();
	}

}
