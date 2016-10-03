package test.example1_basic_crud;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.BaseDao;
import com.github.drinkjava2.jsqlbox.Context;

import test.example1_basic_crud.po.User;

public class Tester {

	public void tx_updateUser() {
		Context c=null;
		c.setDataSource(null); 
		User user = BaseDao.create(User.class);
		user.setAddress("aaaa");
		user.dao.setSql("aaa");
		user.dao.save();
	}

	public void tx_insertUser() {
		User user = BaseDao.create(User.class);
		user.setId(23);
		System.out.println("insert" + user);
		tx_updateUser();
	}

	public static void main(String[] args) {
		Tester tester = BeanBox.getBean(Tester.class);
		tester.tx_insertUser();
	}

}