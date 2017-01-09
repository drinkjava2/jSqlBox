package test.config.po;

import com.github.drinkjava2.jsqlbox.IEntity;
import com.github.drinkjava2.jsqlbox.id.UUIDGenerator;

/**
 * Entity class is not a POJO, need extends from EntityBase or implements EntityInterface interface<br/>
 * 
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class Email implements IEntity {
	private String id;
	private String userID;
	private String email;
	{
		this.box().configIdGenerator("id", UUIDGenerator.INSTANCE);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}