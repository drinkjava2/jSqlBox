package test.config.po;

import com.github.drinkjava2.jsqlbox.SqlBox;

public class UserBX extends SqlBox {
	{
		//this.setBeanClass(User.class);
		this.setTableName("users");
		this.setColumnName(User.UserName, "UserName");
		this.setColumnName(User.Address, "Address");
		this.setColumnName(User.PhoneNumber, "PhoneNumber");
	}
}