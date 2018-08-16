package demo.transaction.jsqlbox;

import static act.controller.Controller.Util.redirect;
import static act.controller.Controller.Util.render;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.gctx;
import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.subclassesOf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcConnectionPool;
import org.osgl.$;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.PostAction;
import org.osgl.mvc.result.Result;
import org.osgl.util.IntRange;

import com.github.drinkjava2.jsqlbox.SqlBoxContext;
import com.github.drinkjava2.jsqlbox.SqlBoxContextConfig;
import com.github.drinkjava2.jtransactions.tinytx.TinyTx;
import com.github.drinkjava2.jtransactions.tinytx.TinyTxConnectionManager;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import act.Act;
import act.app.ActionContext;
import act.job.OnAppStart;

/**
 * A Simple Demo application controller
 * 
 * @since 2.0.2
 */
public class TransactionDemoApp {

	public static final String ACC_A = "A";
	public static final String ACC_B = "B";

	public static void main(String[] args) throws Exception {
		Act.start("Transaction Demo1");
	}

	@OnAppStart
	public void onStart() {
		SqlBoxContextConfig.setGlobalNextConnectionManager(TinyTxConnectionManager.instance());
		SqlBoxContext ctx = new SqlBoxContext(dataSource);
		SqlBoxContext.setGlobalSqlBoxContext(ctx);// 全局缺省上下文

		for (String ddl : ctx.toCreateDDL(Account.class))// 第一次要建表
			gctx().nExecute(ddl);

		new Account(ACC_A).put("amount", $.random(IntRange.of(100, 2000))).insert();// 准备数据
		new Account(ACC_B).put("amount", $.random(IntRange.of(200, 300))).insert();
	}

	@GetAction
	public Result home() {
		Account accA = new Account(ACC_A).load();
		Account accB = new Account(ACC_B).load();
		return render(accA, accB);
	}

	@PostAction("/transfer")
	public Result transfer(int amount, boolean btnA2B, boolean btnB2A, ActionContext context) {
		Account dao = INJECTOR.getInstance(Account.class);
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

	static DataSource dataSource = JdbcConnectionPool
			.create("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	public @interface MyTX {
	}

	private static final Injector INJECTOR = Guice.createInjector(new Module() {
		@Override
		public void configure(Binder binder) {
			binder.bindInterceptor(subclassesOf(Account.class), annotatedWith(MyTX.class), new TinyTx(dataSource));
		}
	});

}
