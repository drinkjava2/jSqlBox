package test.config.po;

import com.github.drinkjava2.jsqlbox.SqlBox;

public class UserBX extends SqlBox {
	{
		// this.setBeanClass(User.class);
		this.configTable("users");
		this.configColumnName("userName", "UserName");
		this.configColumnName("address", "Address");
		this.configColumnName("phoneNumber", "PhoneNumber");
	}

}