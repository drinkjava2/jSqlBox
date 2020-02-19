package com.github.drinkjava2.jsqlbox.function.jdialects.typemapping;
/*- JAVA8_BEGIN */
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.config.TestBase;
 
@SuppressWarnings("all")
public class Java8DateTimeTest extends TestBase {
 
	private void useField(int x) {// Use jDialect's dynamic configuration to disable other fields
		TableModel model = TableModelUtils.entity2Model(DT.class);
		for (int i = 1; i <= 6; i++)
			model.column("d" + i).setTransientable(true);
		model.column("d" + x).setTransientable(false);
		TableModelUtils.bindGlobalModel(DT.class, model);
		createAndRegTables(DT.class);
	}

	@Test
	public void testD1() {
		useField(1);
		DT in = new DT();
		in.setD1(java.time.LocalDate.now());
		in.insert(); 
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD1());
		Systemout.println(out.getD1());
	}
	
	@Test
	public void testD2() {
		useField(2);
		DT in = new DT();
		in.setD2(java.time.OffsetTime.now());
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD2());
		Systemout.println(out.getD2());
	}
	
	@Test
	public void testD3() {
		useField(3);
		DT in = new DT();
		in.setD3(java.time.Instant.now());
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD3());
		Systemout.println(out.getD3());
	}

	@Test
	public void testD4() {
		useField(4);
		DT in = new DT();
		in.setD4(java.time.LocalDateTime.now());
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD4());
		Systemout.println(out.getD4());
	}
	
	@Test
	public void testD5() {
		useField(5);
		DT in = new DT();
		in.setD5(java.time.OffsetDateTime.now());
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD5());
		Systemout.println(out.getD5());
	}
	
	@Test
	public void testD6() {
		useField(6);
		DT in = new DT();
		in.setD6(java.time.ZonedDateTime.now());
		in.insert();
		DT out = new DT(in.getId()).load();
		Assert.assertNotNull(out.getD6());
		Systemout.println(out.getD6());
	}
	
	
	public static class DT extends ActiveRecord<DT> {
		@Id
		@UUID25
		String id;

		@Column
		java.time.LocalDate d1;

		@Column
		java.time.OffsetTime d2;

		@Column
		java.time.Instant d3;

		@Column
		java.time.LocalDateTime d4;

		@Column
		java.time.OffsetDateTime d5;

		@Column
		java.time.ZonedDateTime d6;

		public DT() {
		}

		public DT(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public java.time.LocalDate getD1() {
			return d1;
		}

		public void setD1(java.time.LocalDate d1) {
			this.d1 = d1;
		}

		public java.time.OffsetTime getD2() {
			return d2;
		}

		public void setD2(java.time.OffsetTime d2) {
			this.d2 = d2;
		}

		public java.time.Instant getD3() {
			return d3;
		}

		public void setD3(java.time.Instant d3) {
			this.d3 = d3;
		}

		public java.time.LocalDateTime getD4() {
			return d4;
		}

		public void setD4(java.time.LocalDateTime d4) {
			this.d4 = d4;
		}

		public java.time.OffsetDateTime getD5() {
			return d5;
		}

		public void setD5(java.time.OffsetDateTime d5) {
			this.d5 = d5;
		}

		public java.time.ZonedDateTime getD6() {
			return d6;
		}

		public void setD6(java.time.ZonedDateTime d6) {
			this.d6 = d6;
		}

	}

}
/* JAVA8_END */
