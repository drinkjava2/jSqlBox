package test;

import com.github.drinkjava2.BeanBox;
import com.github.drinkjava2.jsqlbox.SqlBox;
import com.github.drinkjava2.jsqlbox.SqlBoxContext;

import test.config.JBeanBoxConfig.DefaultSqlBoxContextBox;
import test.config.po.User;

public class HelloWorld {

	public static void main(String[] args) {
		SqlBoxContext.setDefaultSqlBoxContext(BeanBox.getBean(DefaultSqlBoxContextBox.class));
		SqlBox.execute("delete from users");
		SqlBox.execute("delete from users2");

		User u = new User();// default use users database table
		u.setUserName("James");
		u.insert();
		System.out.println(SqlBox.queryForString("select ", u.userName(), " from ", u.table()));// print James

		u.box().configTable("users2");// to use users2 database table
		u.box().configColumnName("userName", u.address()); // to map userName field to address column
		u.setUserName("Tom");
		u.insert();
		System.out.println(SqlBox.queryForString("select ", u.address(), " from ", u.table()));// print Tom

	}
}