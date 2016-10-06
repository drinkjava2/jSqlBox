package test.example1_basic_crud;

import static com.github.drinkjava2.jsqlbox.SQLHelper.s0;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SQLBoxContext;

import test.example1_basic_crud.TesterBox.Context2;
import test.example1_basic_crud.po.User;

public class Tester {

	public void tx_insertNormalUser() {
		System.out.println("====Test 1.1====");

		User user = new User();
		user.dao().execute("delete from user");
		user.setUsername("aaaa");
		user.setAddress("bbbb");
		user.setAge(9);
		user.dao().save();

		long start = System.currentTimeMillis();
		int qty = 1000;
		for (int i = 0; i < qty; i++)
			user.dao().execute("insert user (username,age, address) values(?,?,?)" + s0("Zhang San") + s0("" + i)
					+ s0("some city"));
		long end = System.currentTimeMillis();
		System.out.println(
				String.format("%45s|%6sms", "Insert " + qty + " lines without batch, time used:", end - start));

		start = System.currentTimeMillis();
		user.dao().cleanCachedSql();
		for (int i = 0; i < qty; i++)
			user.dao().cacheSQL("insert user (username,age, address) values(?,?,?)" + s0("Zhang San") + s0("" + i)
					+ s0("some city"));
		user.dao().executeCatchedSQLs();
		end = System.currentTimeMillis();
		System.out.println(String.format("%45s|%6sms", "Insert " + qty + " lines with batch, time used:", end - start));

	}

	public void tx_insertDefaultProxyUser() {
		System.out.println("====Test 1.2====");
		User user = SQLBoxContext.createDefaultProxy(User.class);
		user.setUsername("cccc");
		user.setAddress("dddd");
		user.dao().save();
	}

	public void tx_insertCtxProxyUser() {
		System.out.println("====Test 1.3====");
		SQLBoxContext ctx = BeanBox.getBean(Context2.class);
		User user = ctx.createProxy(User.class);
		user.setUsername("eeee");
		user.setAddress("ffff");
		user.dao().save();
	}

	public void tx_main() {
		tx_insertNormalUser();
		// tx_insertDefaultProxyUser();
		// tx_insertCtxProxyUser();
	}

	public static void main(String[] args) {
		Tester tester = BeanBox.getBean(Tester.class);
		tester.tx_main();
	}

}