package test.example1_basic_crud.po;

import com.github.drinkjava2.jsqlbox.Dao;

/**
 * This file should automatically created by a code generator robot
 *
 */
public class User {
	// ============= have to copy below Dao methods in each entity class: =========
	private Dao dao;

	public Dao dao() {
		Dao.initialize();
		if (dao == null)
			dao = Dao.createDefaultDao(this);
		return dao;
	}

	public void putDao(Dao dao) {
		this.dao = dao;
	}
	// ============= Dao code end =========

	private Integer id;

	private String username;

	private String address;

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