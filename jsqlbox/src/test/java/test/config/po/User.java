package test.config.po;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jsqlbox.Entity;

/**
 * User class is not a POJO, need extends from EntityBase(For JAVA7-) or
 * implements EntityInterface interface(for JAVA8+)<br/>
 * 
 * Default database table equal to entity name or add a "s" suffix , in this
 * example it will use "users" as table name
 * 
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */

public class User implements Entity {
	private String id;
	private String userName;
	private String phoneNumber;
	private String address;
	private Integer age;
	private Boolean active;

	public static String ddl(Dialect d) {
		return "create table " + d.check("users") //
				+ "(" + d.VARCHAR("id", 32) //
				+ "," + d.VARCHAR("username", 50) //
				+ "," + d.VARCHAR("Phone_Number", 50) //
				+ "," + d.VARCHAR("Address", 50) //
				+ "," + d.BOOLEAN("active") //
				+ "," + d.INTEGER("Age") //
				+ ")" + d.engine();
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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

	// Below methods are for refactor support, not compulsory but suggest have
	// Hope in future JAVA version can support access private field name
	// directly
	//@formatter:off 
	public String ID(Object... option)             {return box().getColumnName("id", option);	}
	public String USERNAME(Object... option)       {return box().getColumnName("userName", option);}
	public String PHONENUMBER(Object... option)    {return box().getColumnName("phoneNumber", option);}
	public String ADDRESS(Object... option)        {return box().getColumnName("address", option);	}
	public String AGE(Object... option)            {return box().getColumnName("age", option);}
	public String ACTIVE(Object... option)         {return box().getColumnName("active", option);	}
}