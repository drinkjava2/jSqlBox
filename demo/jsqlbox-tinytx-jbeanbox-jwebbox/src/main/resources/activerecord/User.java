package activerecord;

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.TextUtils;
import com.github.drinkjava2.jsqlbox.annotation.Handler;
import com.github.drinkjava2.jsqlbox.annotation.Sql;

/**
 * This is a sample to show put SQL in multiple line Strings(Text), a
 * build-helper-maven-plugin in pom.xml is required, detail see Readme.md
 * 
 * @author Yong Zhu
 */
@Table(name = "users")
public class User extends ActiveRecord {
	@Id
	String name;
	String address;

	public User() {
	}

	public User(String name, String address) {
		this.name = name;
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Sql("insert into users (name,address) values(?,?)")
	public void insertOneUser(String name, String address) {
		this.guess(name, address);
	};

	public void updateAllUser(String name, String address) {
		this.guess(name, address);
	};
	/*- 
	 update users 
	      set name=?, address=?
	*/

	@Handler(MapListHandler.class)
	public List<Map<String, Object>> selectUsers(String name, String address) {
		return this.guess(name, address);
	};
	/*- 
	 select * from users 
	     where name=? and address=?
	*/

	public void deleteUsers(String name, String address) {
		this.guess(name, address);
	};
	/*- 
	 delete from users where name=? or address=?
	*/

	@Handler(MapListHandler.class)
	public List<Map<String, Object>> selectUsersByText(String name, String address) {
		return this.guess(name, address);
	};
	/*-
	   select * 
	   from 
	   users 
	      where 
	         name=:name 
	         and address=:address
	 */

	public static void main(String[] args) {
		String javaSourceCode = TextUtils.getJavaSourceCodeUTF8(User.class);
		System.out.println(javaSourceCode);
	}
}
