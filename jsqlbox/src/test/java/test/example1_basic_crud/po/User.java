package test.example1_basic_crud.po;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SQLHelper;

/**
 * This file should automatically created by a code generator robot
 *
 */
public class User {
	// Oh my god, why there is no "#include" keyword in Java
	// ============= have to copy below garbage in each entity class: =========
	public Dao dao;

	protected Dao dao() {
		if (dao == null)
			dao = Dao.createDefaultDao(this);
		return dao;
	}

	public void putDao(Dao dao) {
		this.dao = dao;
	}

	public SQLHelper sqlHelper() {
		return dao().sqlHelper();
	};

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

	// ============= garbage code end =========

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