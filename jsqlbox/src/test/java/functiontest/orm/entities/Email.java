package functiontest.orm.entities;

import com.github.drinkjava2.jsqlbox.ActiveRecord;

public class Email extends ActiveRecord {
	String id;
	String emailName;
	String userId;

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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}