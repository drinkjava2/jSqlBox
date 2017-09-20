package entity;

import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.annotation.Column;
import com.github.drinkjava2.jdialects.annotation.Entity;
import com.github.drinkjava2.jdialects.annotation.Table;
import com.github.drinkjava2.jdialects.hibernatesrc.utils.DDLFormatter;
import com.github.drinkjava2.jdialects.model.TableModel;
import com.github.drinkjava2.jsqlbox.EntityBase;
import com.github.drinkjava2.jsqlbox.SqlBox;

/**
 * User class is not a POJO, need extends from EntityBase(For Java6 & 7) or
 * implements EntityInterface interface(for Java8)<br/>
 * 
 * Default database table equal to entity name, in this
 * example it will use "users" as table name
 * 
 * @author Yong Zhu
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
public class User extends EntityBase {
	private String id;
	@Column(name = "user_name1")
	private String userName;
	private String phoneNumber;
	private String address;
	private Integer age;
	private Boolean active;

	public static void main(String[] args) {
		String[] ddls = Dialect.H2Dialect.toCreateDDL(User.tableModel());
		for (String ddl : ddls) {
			System.out.println(DDLFormatter.format(ddl));
		}
	}

	public static TableModel tableModel() {
		TableModel t = TableModel.fromPojo(User.class);
		t.column("id").VARCHAR(32);
		t.column("user_name2").VARCHAR(50).pojoField("userName").defaultValue("'aaa'");
		t.column("Phone_Number").VARCHAR(50).singleIndex("IDX_PhoneNM");
		t.column("Address").VARCHAR(50).singleIndex().singleFKey("users", "id");
		t.column("active").BOOLEAN();
		t.column("Age").INTEGER().check("Age > 0");
		return t;
	}

	{
		SqlBox box = this.box();
		box.configTableName("users");
		box.getColumn("username").pojoField("user_name").CHAR(23);
	}

	public static class UserBox extends SqlBox {
		{

		}
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
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