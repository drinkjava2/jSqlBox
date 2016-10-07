package test.example1_basic_crud;

import static com.github.drinkjava2.jsqlbox.SQLHelper.W;
import static com.github.drinkjava2.jsqlbox.SQLHelper.K;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SQLBoxContext;

import test.example1_basic_crud.TesterBox.Context2;
import test.example1_basic_crud.po.User;

public class Tester {

	public void tx_insertDemo() {
		Dao.dao.execute("delete from user");
		Dao.dao.execute("insert user (username,age) values(" + W("user1") + "," + W(10) + ")");
		Dao.dao.execute("insert user (username,age) values(" + W("user2", 20) + ")");
		Dao.dao.execute("insert user (username,age) values(?,?)" + K("user3") + K(30));
		Dao.dao.execute("insert user (username,age) values(?,?)" + K("user4", 40));
		Dao.dao.execute(
				"insert " + User.Table + " (" + User.UserName + "," + User.Age + ") values(" + W("user5", 50) + ")");
		Dao.dao.execute("update user set username=?,address=? " + K("Sam", "BeiJing") + " where age=" + W(50));
		User user = new User();
		user.setUsername("user3");
		user.setAge(40);
		user.dao().save();// TODO not finished
	}

	public void tx_batchInsertDemo() {
		for (int i = 6; i < 1000; i++)
			Dao.dao.cacheSQL("insert user (username,age) values(?,?)" + K("user" + i, 60));
		Dao.dao.executeCatchedSQLs();
	}

	public void tx_insertDefaultProxyUser() {
		User user = SQLBoxContext.createDefaultProxy(User.class);
		user.setUsername("cccc");
		user.setAddress("dddd");
		user.dao().save();
	}

	public void tx_insertCtxProxyUser() {
		SQLBoxContext ctx = BeanBox.getBean(Context2.class);
		User user = ctx.createProxy(User.class);
		user.setUsername("eeee");
		user.setAddress("ffff");
		user.dao().save();
	}

	public void tx_main() {
		tx_insertDemo();
		tx_batchInsertDemo();
		// tx_insertDefaultProxyUser();
		// tx_insertCtxProxyUser();
	}

	public static void main(String[] args) {
		Tester tester = BeanBox.getBean(Tester.class);
		tester.tx_main();
	}

}