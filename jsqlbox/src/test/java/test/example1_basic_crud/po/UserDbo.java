package test.example1_basic_crud.po;

import com.github.drinkjava2.jsqlbox.Dao;

public class UserDbo extends Dao {
	{
		this.setBeanClass(User.class);
	}
}