package test.test4_crud;

import com.github.drinkjava2.BeanBox;

import test.Config;
import test.test4_crud.po.User; 

public class Tester {
	public void tx_CrudDemo() {
		User user = new User();
		user.setUserName("User1");
		user.setAddress("Address1");
		user.dao().getSqlBox().setColumnName(User.PhoneNumber, null);
		user.setPhoneNumber("11111");
		user.setAge(1);
		user.dao().save();

		User user2 = new User();
		user2.setUserName("User2");
		user2.setAddress("Address2");
		user2.setPhoneNumber("22222");
		user2.setAge(1);
		user2.dao().save();

		// User user2 = SqlBox.get(User.class);
		// user2.setUserName("User2");
		// user2.setPhoneNumber("222");
		// user2.setAddress("Address2");
		// user2.setAge(2);
		// user2.dao().save();
		//
		// User2 u2 = new User2();
		// u2.setUserName("u2");
		// u2.setAddress("a2");
		// u2.setPhoneNumber("u2");
		// u2.setAge(22);
		// u2.dao().save();
		// System.out.println("tx_CrudDemo Done");
	}

	public static void main(String[] args) {
		Config.recreateDatabase();
		Tester tester = BeanBox.getBean(Tester.class);
		tester.tx_CrudDemo();
	}

}