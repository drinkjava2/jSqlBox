package test.examples.orm.entities;

import java.util.Set;

import com.github.drinkjava2.jsqlbox.Entity;

public class User implements Entity {
	public static final String CREATE_SQL = "create table users("//
			+ "id varchar(32),"//
			+ "userName varchar(32) )";

	String id;
	String userName;
	Address address;
	Set<Email> emails;
	{
		this.box().configAlias("u"); 
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Set<Email> getEmails() {
		return emails;
	}

	public void setEmails(Set<Email> emails) {
		this.emails = emails;
	}

	public String ID() {
		return box().getColumnName("id");
	}

	public String ADDRESS() {
		return box().getColumnName("address");
	}

	public String EMAILS() {
		return box().getColumnName("emails");
	}
}