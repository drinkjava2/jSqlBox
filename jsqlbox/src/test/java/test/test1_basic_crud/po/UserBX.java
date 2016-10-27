package test.test1_basic_crud.po;

import com.github.drinkjava2.jsqlbox.SqlBox;

public class UserBX extends SqlBox {
	{
		this.setBeanClass(User.class);
		this.overrideTableName("user2");
		this.setColumnName(User.UserName, "UserName");
		this.setColumnName(User.Address, null);
		this.setColumnName(User.PhoneNumber, "PhoneNumber");
	}
}