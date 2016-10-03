package test.example1_basic_crud.po;

import com.github.drinkjava2.jsqlbox.Dao;

/**
 * This file should automatically created by a robot, it's not a human's job
 *
 */
public class User {
	public static Dao dao = Dao.defaultDao(User.class);

	public Dao dao() {
		return dao;
	}

	public void save() {
		dao.save();
	};

	public void load(Object[] args) {
		dao.load(args);
	};

	public void delete(Object[] args) {
		dao.delete(args);
	};

	public void find(Object[] args) {
		dao.find(args);
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