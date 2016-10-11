package test.test1_basic_crud.po;
 
import com.github.drinkjava2.jsqlbox.SQLBox;

public class UserBX extends SQLBox {
	{
		this.setBeanClass(User.class);
		this.getColumn(User.UserName);
	}
}