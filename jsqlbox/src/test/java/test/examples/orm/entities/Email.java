package test.examples.orm.entities;

import com.github.drinkjava2.jsqlbox.Entity;

public class Email implements Entity {
	public static final String CREATE_SQL = "create table email("//
			+ "id varchar(32),"//
			+ "emailName varchar(32)," //
			+ "uid varchar(32) )";
	String id;
	String emailName;
	String uid;// userID;
	User user;
	{
		this.box().configAlias("e");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmailName() {
		return emailName;
	}

	public void setEmailName(String emailName) {
		this.emailName = emailName;
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