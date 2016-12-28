package test.config.po;

import com.github.drinkjava2.jsqlbox.Box;

public class UserBX extends Box {
	{
		// this.setBeanClass(User.class);
		this.configTable("users");
		this.configColumnName("userName", "UserName");
		this.configColumnName("address", "Address");
		this.configColumnName("phoneNumber", "PhoneNumber");
	}

}