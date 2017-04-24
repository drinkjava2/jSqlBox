package test.function_test.crud_method;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.EntityBase;
import com.github.drinkjava2.jsqlbox.id.UUIDGenerator;

import test.TestBase;

public class DataTypeMapTest extends TestBase {

	@Test
	public void insertForMysqlOnly() {
		Dao.executeQuiet("drop table datasample");
		Dialect d = Dao.getDialect();
		Dao.execute("create table " + d.check("datasample")//
				, " (", d.VARCHAR("id", 32)//
				, ",", d.INTEGER("integerField")//
				, ",", d.BIGINT("longField")//
				, ",", d.SMALLINT("shortField")//
				, ",", d.FLOAT("floatField")//
				, ",", d.DOUBLE("doubleField")//
				, ",", d.BIGDECIMAL("bigDecimalField", 10, 2)//
				, ",", d.TINYINT("byteField")//
				, ",", d.BOOLEAN("booleanField")//
				, ",", d.DATE("dateField")//
				, ",", d.TIME("timeField")//
				, ",", d.TIMESTAMP("timestampField")//
				, ",", d.VARCHAR("stringField", 10)//
				, ")", d.engine(" DEFAULT CHARSET=utf8"));
		Dao.refreshMetaData();
		DataSample dt = new DataSample();
		dt.box().configIdGenerator("id", UUIDGenerator.INSTANCE);
		dt.insert();
		Assert.assertEquals(1, (int) Dao.queryForInteger("select count(*) from datasample"));
		Dao.executeQuiet("drop table datasample");
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