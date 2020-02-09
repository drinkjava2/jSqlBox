package com.github.drinkjava2.jsqlbox.function;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.common.Systemout;
import com.github.drinkjava2.jdialects.DebugUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.PKey;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Convert;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DbContextUtils;
import com.github.drinkjava2.jsqlbox.config.TestBase;
import com.github.drinkjava2.jsqlbox.converter.BaseFieldConverter;

/**
 * This is unit test for test @Version annotation
 * 
 * @author Yong Zhu
 * @since 2.0.5
 */
public class ConverterTest extends TestBase {
	{
		regTables(ConverterDemo.class);
	}

	public static class FooDemo {
		public int id;

		public FooDemo(int id) {
			this.id = id;
		}
	}

	public static class FooConverter extends BaseFieldConverter {
		@Override
		public Object entityFieldToDbValue(ColumnModel col, Object entity) {
			Object value = DbContextUtils.doReadFromFieldOrTail(col, entity);
			return ((FooDemo) value).id;
		}

		@Override
		public void writeDbValueToEntityField(Object entityBean, ColumnModel col, Object value) {
			DbContextUtils.doWriteToFieldOrTail(col, entityBean, new FooDemo(Integer.parseInt(value.toString())));
		}
	}

	public static class ConverterDemo extends ActiveRecord<ConverterDemo> {
		@PKey
		@UUID25
		private String id;

		@Convert(FooConverter.class)
		@Column(columnDefinition = "integer")
		private FooDemo foo;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public FooDemo getFoo() {
			return foo;
		}

		public void setFoo(FooDemo foo) {
			this.foo = foo;
		}

	}

	@Test
	public void testConvert() {
		Systemout.println(DebugUtils.getTableModelDebugInfo(TableModelUtils.entity2Model(ConverterDemo.class)));

		ConverterDemo v = new ConverterDemo();
		v.setFoo(new FooDemo(1));
		v.insert();
		v.setFoo(new FooDemo(2));
		v.update();
		ConverterDemo v2 = new ConverterDemo().putField("id", v.getId()).load();
		Assert.assertEquals(2, v2.getFoo().id);
		v2.delete();
	}
}
