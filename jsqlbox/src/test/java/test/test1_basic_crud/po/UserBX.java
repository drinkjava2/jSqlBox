package test.test1_basic_crud.po;

import com.github.drinkjava2.jsqlbox.SQLBox;

public class UserBX extends SQLBox {
	{
		this.setBeanClass(User.class);
		this.overrideTableName("user2");
		this.overrideColumnDefinition(User.UserName, "user_name");
		this.overrideColumnDefinition(User.Address, null);
		this.overrideColumnDefinition(User.PhoneNumber, "phone_number");
	}
}