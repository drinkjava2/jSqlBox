package test.config.po;

import com.github.drinkjava2.jsqlbox.Box;

/**
 * This example shows if do not extend from EntityBase class, how to write the entity class
 * 
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class User2 {
	{
		// configure to use database table "users2"
		this.box().configTable("users2");
	}

	// ==============================================================================================
	// If entity class do not extends from EntityBase, need put below fields and method in each class
	// ==============================================================================================
	private Box box;

	public void pubBox(Box box) {
		this.box = box;
	}

	public Box box() {
		return Box.getBox(this, box);
	}

	public String table() {
		return box().getRealTable();
	}

	public String star() {
		return box().getStar();
	}

	public void insert() {
		this.box().insert();
	}

	public void update() {
		this.box().update();
	}

	public void delete() {
		this.box().delete();
	}

	// ======================================================================================

	// Below are normal POJO fields and getters and setters
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

}