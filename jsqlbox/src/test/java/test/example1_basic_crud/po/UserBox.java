package test.example1_basic_crud.po;

import com.github.drinkjava2.jsqlbox.SQLBox;

public class UserBox extends SQLBox {
	{	
		this.setEntityClass(User.class);
	}
}