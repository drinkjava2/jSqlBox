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

	public void putDao(Dao dao) {
		this.dao = dao;
	}
	// ============= Dao code end =========

	public static String User = "user";
	public static String Id = "id";
	private Integer id;

	public static String UserName = "username";
	private String userName;

	public static String Address = "address";
	private String address;

	public static String Age = "age";
	private Integer age;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return userName;
	}

	public void setUsername(String username) {
		this.userName = username;
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

	// public static void main(String[] args) throws Exception {
	// Field[] fields = User.class.getFields();
	// for (Field field : fields) {
	// System.out.println(field.getName() + "=" + field.get(null));
	// }
	// }
}