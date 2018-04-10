package activerecordtext;

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jsqlbox.annotation.Handlers;
import com.github.drinkjava2.jsqlbox.annotation.Sql;

/**
 * This is a sample to show put SQL in multiple line Strings(Text) for a
 * abstract class, a build-helper-maven-plugin in pom.xml is required, detail
 * see Readme.md
 * 
 * @author Yong Zhu
 */
public abstract class AbstractUser extends TextedUser {

	@Sql("delete from users where name=? or address=?")
	public abstract void deleteAbsUser(String name, String address);

	public abstract PreparedSQL updateUserPreparedSQL(String name, String address);
	/*- 
	   update users 
	      set name=?, address=?
	*/

	@Handlers(MapListHandler.class)
	public abstract List<Map<String, Object>> selectUserListMap(String name, String address);
	/*-
	   select * 
	   from 
	   users 
	      where 
	         name=:name 
	         and  address=:address
	 */

	public abstract List<AbstractUser> selectAbstractUserList(String name, String address);
	/*-
	   select u.** 
	   from 
	   users u
	      where 
	         u.name=? and address=?
	 */

}
