package test.test1_basic_crud.po;

import com.github.drinkjava2.jsqlbox.SQLBox;

public class UserBX extends SQLBox {
	{
		this.setBeanClass(User.class);
		this.setTablename("user2");
		this.setColumnDefinition(User.UserName, "user_name");
		//this.setColumnDefinition(User.Address, "address");
		this.setColumnDefinition(User.Age, "age");
		this.setColumnDefinition(User.PhoneNumber, "phone_number");
	}
}