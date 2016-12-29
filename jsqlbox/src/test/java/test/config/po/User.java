package test.config.po;

import com.github.drinkjava2.jsqlbox.IEntity;

/**
 * Entity class is not a POJO, need extends from EntityBase or implements EntityInterface interface<br/>
 * 
 * Default database table equal to entity class(s) name, in this example it will use "users" as table name
 * 
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class User implements IEntity { 
	private Integer id;
	private String userName;
	private String phoneNumber;
	private String address;
	private Integer age;
	private Boolean alive;

	public Boolean getAlive() {
		return alive;
	}

	public void setAlive(Boolean alive) {
		this.alive = alive;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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

	// Below method are friendly for JDBC, it's not compulsory but recommended to have
	public String id() {
		return box().getColumnName();
	}

	public String userName() {
		return box().getColumnName();
	}

	public String phoneNumber() {
		return box().getColumnName();
	}

	public String address() {
		return box().getColumnName();
	}

	public String age() {
		return box().getColumnName();
	}

	public String alive() {
		return box().getColumnName();
	}

}