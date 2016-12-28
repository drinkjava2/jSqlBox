package test.crud_method;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.EntityBase;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.id.UUIDGenerator;
import com.github.drinkjava2.jsqlbox.tinyjdbc.DatabaseType;

import test.config.TestPrepare;

public class DataTypeMapTest {

	@Before
	public void setup() {
		TestPrepare.prepareDatasource_SetDefaultSqlBoxConetxt_RecreateTables();
	}

	@After
	public void cleanUp() {
		TestPrepare.closeDatasource_CloseDefaultSqlBoxConetxt();
	}

//	@Test
	public void insertForMysqlOnly() {
		if (SqlBox.getDefaultDatabaseType() != DatabaseType.MYSQL)
			return;
		SqlBox.executeQuiet("drop table datatypes");
		SqlBox.execute("create table datatypes ("//
				, "id", " varchar(32)", ","//
				, "integerField", " int", ","//
				, "longField", " BIGINT", ","//
				, "shortField", " SMALLINT", ","//
				, "floatField", " FLOAT", ","//
				, "doubleField", " DOUBLE", ","//
				, "bigDecimalField", " NUMERIC", ","//
				, "byteField", " TINYINT", ","//
				, "booleanField", " BIT", ","//
				, "dateField", " DATE", ","//
				, "timeField", " Time", ","//
				, "timestampField", " TIMESTAMP", ","//
				, "stringField", " VARCHAR(10)"//
				, ")ENGINE=InnoDB DEFAULT CHARSET=utf8;");
		SqlBox.refreshMetaData();
		DataTypes dataTypes = new DataTypes();
		dataTypes.box().configIdGenerator("id", UUIDGenerator.INSTANCE);
		dataTypes.insert();
	}

	@Test
	public void insertForOracleOnly() {
		if (SqlBox.getDefaultDatabaseType() != DatabaseType.ORACLE)
			return;
		SqlBox.executeQuiet("drop table datatypes");
		SqlBox.execute("create table datatypes ("//
				, "id", " varchar(32)", ","//
				, "integerField", " int", ","//
				, "longField", " NUMERIC", ","//
				, "shortField", " SMALLINT", ","//
				, "floatField", " FLOAT", ","//
				, "doubleField", " NUMERIC", ","//
				, "bigDecimalField", " NUMERIC", ","//
				, "byteField", " NUMERIC", ","//
				, "booleanField", " NUMERIC", ","//
				, "dateField", " DATE", ","//
				, "timeField", " DATE", ","//
				, "timestampField", " DATE", ","//
				, "stringField", " VARCHAR(10)"//
				, ")");
		SqlBox.refreshMetaData();
		DataTypes dataTypes = new DataTypes();
		dataTypes.box().configIdGenerator("id", UUIDGenerator.INSTANCE);
		dataTypes.insert();
	}

	public static class DataTypes extends EntityBase {
		private String id;
		private Integer integerField = 1;
		private Long longField = 2l;
		private Short shortField = 3;
		private Float floatField = 4.0f;
		private Double doubleField = 5.0d;
		private BigDecimal bigDecimalField = new BigDecimal("6.0");
		private Byte byteField = 7;
		private String stringField = "str";
		private Boolean booleanField = true;
		private Date dateField = new Date();
		private Date timeField = new Date();
		private Date timestampField = new Date();

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Integer getIntegerField() {
			return integerField;
		}

		public void setIntegerField(Integer integerField) {
			this.integerField = integerField;
		}

		public Long getLongField() {
			return longField;
		}

		public void setLongField(Long longField) {
			this.longField = longField;
		}

		public Short getShortField() {
			return shortField;
		}

		public void setShortField(Short shortField) {
			this.shortField = shortField;
		}

		public Float getFloatField() {
			return floatField;
		}

		public void setFloatField(Float floatField) {
			this.floatField = floatField;
		}

		public Double getDoubleField() {
			return doubleField;
		}

		public void setDoubleField(Double doubleField) {
			this.doubleField = doubleField;
		}

		public BigDecimal getBigDecimalField() {
			return bigDecimalField;
		}

		public void setBigDecimalField(BigDecimal bigDecimalField) {
			this.bigDecimalField = bigDecimalField;
		}

		public String getStringField() {
			return stringField;
		}

		public void setStringField(String stringField) {
			this.stringField = stringField;
		}

		public Byte getByteField() {
			return byteField;
		}

		public void setByteField(Byte byteField) {
			this.byteField = byteField;
		}

		public Boolean getBooleanField() {
			return booleanField;
		}

		public void setBooleanField(Boolean booleanField) {
			this.booleanField = booleanField;
		}

		public Date getDateField() {
			return dateField;
		}

		public void setDateField(Date dateField) {
			this.dateField = dateField;
		}

		public Date getTimeField() {
			return timeField;
		}

		public void setTimeField(Date timeField) {
			this.timeField = timeField;
		}

		public Date getTimestampField() {
			return timestampField;
		}

		public void setTimestampField(Date timestampField) {
			this.timestampField = timestampField;
		}

	}

}