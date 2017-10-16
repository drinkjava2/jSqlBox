package functiontest.orm.entities;

import com.github.drinkjava2.jsqlbox.ActiveRecord;

public class Address extends ActiveRecord {
	String id;
	String addressName;
	String userId; 

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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}