package com.github.drinkjava2.test;

import static com.github.drinkjava2.jdbpro.DbPro.param;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.cglib3_2_0.proxy.Enhancer;
import com.github.drinkjava2.cglib3_2_0.proxy.MethodInterceptor;
import com.github.drinkjava2.cglib3_2_0.proxy.MethodProxy;
import com.github.drinkjava2.jdialects.StrUtils;
import com.github.drinkjava2.jdialects.TableModelUtils;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Column;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.model.ColumnModel;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxException;
import com.zaxxer.hikari.HikariDataSource;

public class NoramlTest {
	public static ThreadLocal<String[]> thdMethodName = new ThreadLocal<String[]>();

	public static class UserDemo extends ActiveRecord {
		@UUID25
		@Id
		public String id;

		@Column(name = "usr_name")
		public String name;

		public String address;

		@Column(name = "usr_age")
		public Integer age;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
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
	}

	public static class ProxyBean implements MethodInterceptor {
		private TableModel tableModel;

		public ProxyBean(TableModel tableModel) {
			this.tableModel = tableModel;
		}

		public Object intercept(Object obj, Method method, Object[] args, MethodProxy cgLibMethodProxy)
				throws Throwable {
			if (method != null && tableModel != null) {
				String fieldName = method.getName().substring(3);
				for (ColumnModel col : tableModel.getColumns()) {
					if (col.getEntityField().equalsIgnoreCase(fieldName)) {
						thdMethodName.set(new String[] { tableModel.getAlias(), col.getColumnName() });
					}
				}
			}
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T aliasProxy(Class<T> c, String alias) {
		TableModel t = TableModelUtils.entity2Model(c);
		t.setAlias(alias);
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(c);
		enhancer.setCallback(new ProxyBean(t));
		return (T) enhancer.create();
	}

	private static void checkArrayStringExist(String[] a) {
		if (a == null || a.length != 2)
			throw new SqlBoxException("No column found.");
		if (StrUtils.isEmpty(a[0]))
			throw new SqlBoxException("Alias name not set.");
		if (StrUtils.isEmpty(a[1]))
			throw new SqlBoxException("Column name not found.");
	}

	public static String as(Object o) {
		String[] a = thdMethodName.get();
		checkArrayStringExist(a);
		return new StringBuilder(a[0]).append(".").append(a[1]).append(" as ").append(a[0]).append("_").append(a[1])
				.toString();
	}

	public static String col(Object o) {
		String[] a = thdMethodName.get();
		checkArrayStringExist(a);
		return new StringBuilder(a[0]).append(".").append(a[1]).toString();
	}

	/**
	 * This is for Java6 and 7 how to use method name to support refector
	 */
	@Test
	public void normalTest() {
		HikariDataSource ds = new HikariDataSource();
		ds.setDriverClassName("org.h2.Driver"); // H2 Memory database
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setUsername("sa");
		ds.setPassword("");

		SqlBoxContext.setGlobalAllowShowSql(true); // Log output
		SqlBoxContext ctx = new SqlBoxContext(ds); // Here you go
		SqlBoxContext.setGlobalSqlBoxContext(ctx);

		String[] ddlArray = ctx.toDropAndCreateDDL(UserDemo.class);
		for (String ddl : ddlArray)
			ctx.quiteExecute(ddl);

		try {
			ctx.nBatchBegin();
			for (int i = 0; i < 100; i++) {
				UserDemo u = new UserDemo();
				u.setName("Foo" + i);
				u.setAge(i);
				u.insert();
			}
		} finally {
			ctx.nBatchEnd();
		}
		Assert.assertEquals(100, ctx.iQueryForLongValue("select count(*) from UserDemo"));

		UserDemo a = aliasProxy(UserDemo.class, "a");
		List<?> list = ctx.iQuery(new MapListHandler(), //
				"select a.*, ", as(a.getAddress()), ", ", as(a.getName())//
				, " from UserDemo a where "//
				, col(a.getName()), ">=?", param("Foo90") //
				, " and ", col(a.getAge()), ">?", param(1) //
		);
		Assert.assertTrue(list.size() == 10);
		ds.close();
	}
}