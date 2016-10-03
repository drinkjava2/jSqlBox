package test.example1_basic_crud;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.Dao;

import test.example1_basic_crud.po.User;

public class Tester {

	public void tx_updateUser() {
		User user = Dao.create(User.class);
		user.setAddress("aaaa");
		user.dao.setSql("aaa");
		user.save();
		System.out.println(user.dao.getSql());
	}

	public void tx_insertUser() {
		User user = new User();
		user.setId(23);
		user.save();
		tx_updateUser();
	}

	public static void main(String[] args) {
		Tester tester = BeanBox.getBean(Tester.class);
		tester.tx_insertUser();
	}

}