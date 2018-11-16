/*
 * Copyright (C) 2016 Original Author
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.test;

import static com.github.drinkjava2.jbeanbox.JBEANBOX.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.sql.DataSource;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jbeanbox.annotation.AOP;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;
import com.zaxxer.hikari.HikariDataSource;

/**
 * ActiveRecordDemoTest of jSqlBox configurations
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */

public class TransactionDemo {

	public static class DataSourceCfg extends BeanBox {
		{
			injectValue("jdbcUrl", "jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
			injectValue("driverClassName", "org.h2.Driver");
			injectValue("username", "sa");
			injectValue("password", "");
		}

		public HikariDataSource create() {
			HikariDataSource ds = new HikariDataSource();
			ds.setMaximumPoolSize(10);
			ds.setConnectionTimeout(5000);
			this.setPreDestroy("close");// jBeanBox will close pool
			return ds;
		}
	}

	public static class TxBox extends BeanBox {
		{
			this.injectConstruct(TinyTx.class, DataSource.class, inject(DataSourceCfg.class));
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	@AOP
	public static @interface TX {
		public Class<?> value() default TxBox.class;
	}

	@TX
	public void save() {
		new User().putField("name", "Tom").insert();
		System.out.println(SqlBoxContext.gctx().iQueryForString("select name from usertb"));
		System.out.println(1 / 0); // force roll back
	}

	public static void main(String[] args) {
		SqlBoxContext ctx = new SqlBoxContext((DataSource) BeanBox.getBean(DataSourceCfg.class));
		ctx.setConnectionManager(TinyTxConnectionManager.instance());
		SqlBoxContext.setGlobalSqlBoxContext(ctx);
		String[] ddls = ctx.toCreateDDL(User.class);
		for (String ddl : ddls)
			ctx.nExecute(ddl);
		TransactionDemo demo = BeanBox.getBean(TransactionDemo.class);
		demo.save();
		JBEANBOX.close();
	}
}