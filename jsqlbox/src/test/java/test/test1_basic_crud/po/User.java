package test.test1_basic_crud.po;

import com.github.drinkjava2.jsqlbox.Dao;

/**
 * This file should automatically created by a code generator robot
 *
 */
public class User {
	// ==== have to copy below Dao methods in each entity class:=====
	private Dao dao;

	public Dao dao() {
		if (dao == null)
			dao = Dao.defaultDao(this);
		return dao;
	}
	// ============= Dao code end =========

	public static final String Table = "user";

	public static String Id = "id";
	private Integer id;

	public static final String UserName = "userName";
	private String userName;

	public static final String PhoneNumber = "phoneNumber";
	private String phoneNumber;

	public static final String Address = "address";
	private String address;

	public static final String Age = "age";
	private Integer age;

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

}