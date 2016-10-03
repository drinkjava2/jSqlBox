package test.example1_basic_crud.po;

import com.github.drinkjava2.jsqlbox.BaseDao;

public class UserDao extends BaseDao {
	{
		this.setBeanClass(User.class);
	}
}