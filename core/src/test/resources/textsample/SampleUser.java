package textsample;

import java.util.List;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Test;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.TextUtils;
import com.github.drinkjava2.jsqlbox.annotation.Handler;
import com.github.drinkjava2.jsqlbox.annotation.Sql;

/**
 * This is a sample to show put SQL in annotation and in multiple line Strings,
 * a build-helper-maven-plugin plugin in pom.xml is needed, detail see pom.xml
 * 
 * @author Yong Zhu
 */
public class SampleUser extends ActiveRecord {
	@Id
	private String name;
	private String address;
	private Integer age;

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

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	@Sql("insert into users (name,address) values(?,?)")
	public void insertOneUser(String name, String address) {
		this.bet(name, address);
	};

	@Sql("update users set name=?, address=?")
	public void updateAllUser(String name, String address) {
		this.bet(name, address);
	};

	@Sql("delete from users where name=? or address=?")
	public void deleteUsers(String name, String address) {
		this.bet(name, address);
	};

	@Handler(MapListHandler.class)
	public List<SampleUser> selectUsers(String name, String address) {
		return this.bet(name, address);
	};
	/*-
	   select * 
	   from 
	   users 
	      where 
	         name=:name 
	         and address=:address
	 */

	@Test
	public void doTestReadSelfSourceCode() {
		String s = TextUtils.getJavaSourceCodeUTF8(this.getClass());
		System.out.println(s);
	}
}
