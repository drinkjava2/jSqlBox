package com.example.jsqlboxinspringboot;

import static com.github.drinkjava2.jsqlbox.DB.gctx;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.jsqlboxinspringboot.entity.Customer;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jtransactions.spring.SpringTxConnectionManager;

//@formatter:off
@SpringBootApplication
public class JsqlboxInSpringbootApplication {
	@Autowired
	DataSource ds; 

	public static void main(String[] args) {
		SpringApplication.run(JsqlboxInSpringbootApplication.class, args);
	}
 
	/* 声明式事务有3个关键： 
	 * 1.IOC/AOP工具， 这里就用Spring自带的。 
	 * 2.声明式事务切面处理器，因为这个项目pom.xml中只有一个H2的依赖，所以Spring在后台已经悄悄地自动创建了一个DataSourceTransactionManager单例，并将H2数据源注入给它了，这也太自动过头了是不是?
	 * 3.连接管理器。不同的事务工具获取和关闭connection的方式不一样，在jTransactions中为纯JDBC、jFinal、Spring等都准备了一个连接管理器,这里设成SpringTxConnectionManager。
	 */
	@Bean
	public DbContext createDefaultDbContext() { 
		DbContext ctx = new DbContext(ds);
		ctx.setConnectionManager(SpringTxConnectionManager.instance());
		DbContext.setGlobalDbContext(ctx);// 设定静态全局上下文

		// 第一次运行要建表，利用jSqlBox来做，自已动手更踏实
		for (String ddl : ctx.toCreateDDL(Customer.class))
			gctx().iExecute(ddl);

		return ctx;
	}

}
