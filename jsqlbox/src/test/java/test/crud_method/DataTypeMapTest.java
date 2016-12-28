package test.crud_method;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
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

	@Test
	public void insertForMysqlOnly() {
		if (SqlBox.getDefaultDatabaseType() != DatabaseType.MYSQL)
			return;
		SqlBox.executeQuiet("drop table datasample");
		SqlBox.execute("create table datasample ("//
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
		DataSample dataTypes = new DataSample();
		dataTypes.box().configIdGenerator("id", UUIDGenerator.INSTANCE);
		dataTypes.insert();
		Assert.assertEquals(1, (int) SqlBox.queryForInteger("select count(*) from datasample"));
		SqlBox.executeQuiet("drop table datasample");
	}

	@Test
	public void insertForOracleOnly() {
		if (SqlBox.getDefaultDatabaseType() != DatabaseType.ORACLE)
			return;
		SqlBox.executeQuiet("drop table datasample");
		SqlBox.execute("create table datasample ("//
				, "id", " varchar(32)", ","//
				, "integer_Field", " int", ","//
				, "long_Field", " NUMERIC", ","//
				, "short_Field", " SMALLINT", ","//
				, "float_Field", " FLOAT", ","//
				, "double_Field", " NUMERIC", ","//
				, "big_Decimal_Field", " NUMERIC", ","//
				, "byte_Field", " NUMERIC", ","//
				, "boolean_Field", " NUMERIC", ","//
				, "date_Field", " DATE", ","//
				, "time_Field", " DATE", ","//
				, "timestamp_Field", " DATE", ","//
				, "string_Field", " VARCHAR(10)"//
				, ")");
		SqlBox.refreshMetaData();
		DataSample dataTypes = new DataSample();
		dataTypes.box().configIdGenerator("id", UUIDGenerator.INSTANCE);
		dataTypes.insert();
		Assert.assertEquals(1, (int) SqlBox.queryForInteger("select count(*) from datasample"));
		SqlBox.executeQuiet("drop table datasample");
	}

	public static class DataSample extends EntityBase {
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