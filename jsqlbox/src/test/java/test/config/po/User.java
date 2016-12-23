package test.config.po;

import java.io.Serializable;

import com.github.drinkjava2.jsqlbox.EntityBase;

/**
 * Entity class is not a POJO, extends from EntityBase class or it's child class<br/>
 * But for some reason if can't extend from EntityBase, can copy all method in EntityBase into entity class<br/>
 * 
 * Default database table equal to entity class(s) name, in this example it will use "users" as table name
 * 
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class User extends EntityBase implements Serializable {
	private static final long serialVersionUID = 7547093565698490399L;
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

	// Below method are for JDBC friendly use, it's not compulsory but recommended to add below methods
	public String id() {
		return dao().getColumnName();
	}

	public String userName() {
		return dao().getColumnName();
	}

	public String phoneNumber() {
		return dao().getColumnName();
	}

	public String address() {
		return dao().getColumnName();
	}

	public String age() {
		return dao().getColumnName();
	}

	public String alive() {
		return dao().getColumnName();
	}

}