package demo.transaction.jsqlbox;

import static act.controller.Controller.Util.redirect;
import static act.controller.Controller.Util.render;
import static com.github.drinkjava2.jsqlbox.DB.gctx;
import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.subclassesOf;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;
import org.osgl.$;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.PostAction;
import org.osgl.mvc.result.Result;
import org.osgl.util.IntRange;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jbeanbox.JBEANBOX;
import com.github.drinkjava2.jsqlbox.DbContext;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxAOP;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;
import com.google.inject.Guice;
import com.google.inject.Injector;

import act.Act;
import act.app.ActionContext;
import act.job.OnAppStart;

/**
 * A Simple Demo application controller
 * 
 * @since 2.0.2
 */
public class TransactionDemo {

	public static final String ACC_A = "A";
	public static final String ACC_B = "B";

	public static void main(String[] args) throws Exception {
		Act.start("Transaction Demo1");
	}

	@OnAppStart
	public void onStart() {
		DbContext ctx = new DbContext(dataSource);
		ctx.setConnectionManager(TinyTxConnectionManager.instance());// 这一行是与TinyTx配对的，必须
		DbContext.setGlobalDbContext(ctx);// 全局缺省上下文

		for (String ddl : ctx.toCreateDDL(Account.class))// 第一次要建表
			gctx().exe(ddl);
		new Account(ACC_A).putField("amount", $.random(IntRange.of(100, 2000))).insert();// 第一次要准备数据
		new Account(ACC_B).putField("amount", $.random(IntRange.of(200, 300))).insert();
	}

	@GetAction
	public Result home() {
		Account accA = new Account(ACC_A).load();
		Account accB = new Account(ACC_B).load();
		return render(accA, accB);
	}

	@PostAction("/transfer")
	public Result transfer(int amount, boolean btnA2B, boolean btnB2A, ActionContext context) {
		Account dao = INJECTOR.getInstance(Account.class); // 使用Guice来创建AOP代理类
		Account dao2 = JBEANBOX.getInstance(Account.class); // 使用jBeanBox来创建AOP代理类
		try {
			if (btnA2B)
				dao.transfer(amount, ACC_A, ACC_B);
			else
				dao2.transfer(amount, ACC_B, ACC_A);
			context.flash().success("Transaction committed successfully");
		} catch (Exception e) {
			context.flash().error("Transaction failed. Possible reason: no enough balance in the credit account");
			e.printStackTrace();
		}
		return redirect("/");
	}

	static DataSource dataSource = JdbcConnectionPool
			.create("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");

	public static class TinyTxBox extends BeanBox {
		{
			this.injectConstruct(TinyTxAOP.class);
		}
	}

	private static final Injector INJECTOR = Guice.createInjector(
			binder -> binder.bindInterceptor(subclassesOf(Account.class), annotatedWith(MyTX.class), new TinyTxAOP()));

}
