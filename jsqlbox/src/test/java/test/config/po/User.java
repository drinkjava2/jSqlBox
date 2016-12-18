package test.config.po;

/**
 * This entity class should automatically created by a code generator tool
 * 
 */
public class User extends EntityBase {
	private Integer id;

	public String id() {
		return dao().getColumnName();
	}

	private String userName;

	public String userName() {
		return dao().getColumnName();
	}

	private String phoneNumber;

	public String phoneNumber() {
		return dao().getColumnName();
	}

	private String address;

	public String address() {
		return dao().getColumnName();
	}

	private Integer age;

	public String age() {
		return dao().getColumnName();
	}

	private Boolean alive;

	public String alive() {
		return dao().getColumnName();
	}

	// getter & setters
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