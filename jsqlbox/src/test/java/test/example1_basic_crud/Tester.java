package test.example1_basic_crud;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SQLBox;

import test.example1_basic_crud.po.User;
import test.example1_basic_crud.po.UserBox2;

public class Tester {

	public void insertUser() {
		User user = SQLBox.create(UserBox2.class);
		user.setAddress("aaaa");
		System.out.println(user);

	}

	public static void main(String[] args) {
		Tester tester = BeanBox.getBean(Tester.class);
		tester.insertUser();

	}

}