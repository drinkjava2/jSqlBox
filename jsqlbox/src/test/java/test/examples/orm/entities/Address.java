package test.examples.orm.entities;

import com.github.drinkjava2.jsqlbox.Entity;

public class Address implements Entity {
	public static final String CREATE_SQL = "create table address("//
			+ "id varchar(32),"//
			+ "addressName varchar(32) ," //
			+ "uid varchar(32) )";
	String id;
	String addressName;
	String uid;// userID;
	User user;
	{
		this.box().configAlias("a");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAddressName() {
		return addressName;
	}

	public void setAddressName(String addressName) {
		this.addressName = addressName;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String ID() {
		return box().getColumnName("id");
	}

	public String UID() {
		return box().getColumnName("uid");
	}

	public String USER() {
		return box().getColumnName("user");
	}
}