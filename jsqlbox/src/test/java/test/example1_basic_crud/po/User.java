package test.example1_basic_crud.po;

import com.github.drinkjava2.jsqlbox.BaseDao;

/**
 * This file should automatically created by a database to Java code generator tool<br/>
 *
 */
public class User {
	public BaseDao dao;

	public BaseDao getDao() {
		return dao;
	}

	public void setDao(BaseDao dao) {
		this.dao = dao;
	}

	public void save() {
	};

	public void load(Object... args) {
	};

	public void delete(Object... args) {
	};

	public static final String Id = "id";
	private Integer id;

	public static final String Username = "usernmae";
	private String username;

	public static final String Address = "address";
	private String address;

	public static final String Phone = "phone";
	private String phone;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

}