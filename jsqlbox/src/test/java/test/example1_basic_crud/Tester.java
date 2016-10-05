package test.example1_basic_crud;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SQLBoxContext;
import static com.github.drinkjava2.jsqlbox.SQLHelper.V;

import test.example1_basic_crud.TesterBox.Context2;
import test.example1_basic_crud.po.User;

public class Tester {

	public void tx_insertNormalUser() {
		User user = new User();
		user.setUsername("aaaa");
		user.setAddress("bbbb");
		user.save();
	}

	public void tx_insertDefaultProxyUser() {
		User user = SQLBoxContext.createDefaultProxy(User.class);
		user.setUsername("cccc");
		user.setAddress("dddd");
		user.save();
		user.sqlHelper()
				.execute("update user set username='newuser' where username<>" + V("abcd") + " and age<>" + V(0));
	}

	public void tx_insertCtxProxyUser() {
		SQLBoxContext ctx = BeanBox.getBean(Context2.class);
		User user = ctx.createProxy(User.class);
		user.setUsername("eeee");
		user.setAddress("ffff");
		user.save();
	}

	public void tx_main() {
		tx_insertNormalUser();
		tx_insertDefaultProxyUser();
		tx_insertCtxProxyUser();
	}

	public static void main(String[] args) {
		Tester tester = BeanBox.getBean(Tester.class);
		tester.tx_main();
	}

}