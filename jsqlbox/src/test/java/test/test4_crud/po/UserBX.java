package test.test4_crud.po;

import com.github.drinkjava2.jsqlbox.SqlBox;

public class UserBX extends SqlBox {
	{
		this.setBeanClass(User.class);
		this.setTableName("user2");
		this.setColumnName(User.UserName, "UserName");
		this.setColumnName(User.Address, null);
		this.setColumnName(User.PhoneNumber, "PhoneNumber");
	}
}