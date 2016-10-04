package test.example1_basic_crud.po;

import com.github.drinkjava2.jsqlbox.Dao;

/**
 * This file should automatically created by a robot
 *
 */
public class User {
	public Dao dao;

	protected Dao dao() {
		if (dao == null)
			dao = Dao.createDefaultDao(this); 
		return dao;
	}

	public void putDao(Dao dao) {
		this.dao = dao;
	}

	public void save() {
		dao().save();
	};

	public void load(Object[] args) {
		dao().load(args);
	};

	public void delete(Object[] args) {
		dao().delete(args);
	};

	public void find(Object[] args) {
		dao().find(args);
	};

	public static final String Id = "id";
	private Integer id;

	public static final String Username = "usernmae";
	private String username;

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

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

}