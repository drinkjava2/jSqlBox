package demo.transaction.jsqlbox;

import static act.controller.Controller.Util.redirect;
import static act.controller.Controller.Util.render;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.gctx;

import java.sql.Connection;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;
import org.osgl.$;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.PostAction;
import org.osgl.mvc.result.Result;
import org.osgl.util.IntRange;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.TX;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;

import act.Act;
import act.app.ActionContext;
import act.job.OnAppStart;

/**
 * A Simple Demo application controller
 * 
 * 这个例子中用的是
 * 
 * @since 2.0.2
 */
public class TransactionDemoApp {

	public static final String ACC_A = "A";
	public static final String ACC_B = "B";

	@GetAction
	public Result home() {
		Account accA = new Account(ACC_A).load();
		Account accB = new Account(ACC_B).load();
		return render(accA, accB);
	}

	// 这里使用jBeanBox来生成支持声明式事务的代理类，很古怪，但没办法，ActFramework的注赖注入不支持AOP!
	// 也可以使用Spring-ioc来生成代理类，效果相同，但是用了Spring内核，通常也会顺手用它的事务，太臃肿了(然后再顺手用一下Spring的MVC，于是项目就变成SpringBoot了)
	@PostAction("/transfer")
	public Result transfer(int amount, boolean btnA2B, boolean btnB2A, ActionContext context) {
		Account dao = BeanBox.getBean(Account.class);
		try {
			if (btnA2B)
				dao.transfer(amount, ACC_A, ACC_B);
			else
				dao.transfer(amount, ACC_B, ACC_A);
			context.flash().success("Transaction committed successfully");
		} catch (Exception e) {
			context.flash().error("Transaction failed. Possible reason: no enough balance in the credit account");
		}
		return redirect("/");
	}

	// jBeanBox数据源配置
	public static class DataSourceBox extends BeanBox {
		public DataSource create() {
			return JdbcConnectionPool.create("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0",
					"sa", "");
		}
	}

	/**
	 * <pre>
	 * Actframework本身的IOC工具不完整，不支持AOP，所以必须使用第三方事务工具如Spring等，这里为了演示，使用jTransactions,它是一个独立的声明式事务工具。
	 * 声明式事务有3个关键，所有支持声明式事务的框架都具备这三要素：
	 * 1.IOC/AOP工具，这里就用jBeanBox
	 * 2.声明式事务切面处理器，这里就用jTransactions中的TinyTx  
	 * 3.连接管理器，这里就用jTransactions中的TinyTxConnectionManager
	 * </pre>
	 */
	// jBeanBox声明式事务切面处理器配置，这里用jTransactions中的TinyTx，它的构造器要传入一个数据源作参数
	public static class TxBox extends BeanBox {
		{
			this.setConstructor(TinyTx.class, BeanBox.getBean(DataSourceBox.class),
					Connection.TRANSACTION_READ_COMMITTED);
		}
	}

	@OnAppStart
	public void onStart() {
		SqlBoxContextConfig.setGlobalNextConnectionManager(TinyTxConnectionManager.instance()); 
		SqlBoxContext ctx = new SqlBoxContext((DataSource) BeanBox.getBean(DataSourceBox.class));
		BeanBox.regAopAroundAnnotation(TX.class, TxBox.class); 
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// 全局缺省上下文，适用于单数据源场合

		String[] ddls = gctx().toCreateDDL(Account.class);// 第一次要建表
		for (String ddl : ddls)
			gctx().nExecute(ddl);

		new Account(ACC_A).put("amount", $.random(IntRange.of(100, 2000))).insert();// 第一次要插值
		new Account(ACC_B).put("amount", $.random(IntRange.of(200, 300))).insert();
	}

	public static void main(String[] args) throws Exception {
		Act.start("Transaction Demo");
	}

}
