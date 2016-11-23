package test.config.po;

import com.github.drinkjava2.jsqlbox.SqlBox;

public class UserBX extends SqlBox {
	{
		//this.setBeanClass(User.class);
		this.configTableName("users");
		this.configColumnName(User.UserName, "UserName");
		this.configColumnName(User.Address, "Address");
		this.configColumnName(User.PhoneNumber, "PhoneNumber");
	}
	
	
}