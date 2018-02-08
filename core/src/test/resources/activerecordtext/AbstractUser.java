package activerecordtext;

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jsqlbox.annotation.Handler;
import com.github.drinkjava2.jsqlbox.annotation.Sql;

/**
 * This is a sample to show put SQL in multiple line Strings(Text) for a
 * abstract class, a build-helper-maven-plugin in pom.xml is required, detail
 * see Readme.md
 * 
 * @author Yong Zhu
 */
public abstract class AbstractUser extends TextedUser {

	@Sql("update users set name=?, address=?")
	public void updateAllUser(String name, String address) {
		this.guess(name, address);
	};

	@Sql("delete from users where name=? or address=?")
	public abstract void deleteUsers(String name, String address);

	@Handler(MapListHandler.class)
	public abstract List<Map<String, Object>> selectUsersByText(String name, String address);
	/*-
	   select * 
	   from 
	   users 
	      where 
	         name=:name 
	         and  address=:address
	 */

 
}
