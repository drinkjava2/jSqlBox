package textsample;

import java.util.List;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jsqlbox.ActiveSql;
import com.github.drinkjava2.jsqlbox.annotation.Handler;
import com.github.drinkjava2.jsqlbox.annotation.Sql;

import functiontest.speedtest.SpeedTest.User;

/*-
 * This is a sample to show put SQL in annotation and in multiple line Strings, this is a
 * Java file, and also is a resource file, to do that put a build-helper-maven-plugin
 * plugin in pom.xml, detail see user manual of jSqlBox.
 * 
* @author Yong Zhu 
*/
public abstract class UserSqls extends ActiveSql {

	@Sql("insert into users (name,address) values(?,?)")
	public abstract void insertOneUser(String name, String address);

	@Sql("update users set name=?, address=?")
	public abstract void updateAllUser(String name, String address);

	@Handler(MapListHandler.class)
	@Sql("select * from users where name=? and address=?")
	public abstract List<User> getUserList(String name, String address);

	@Sql("delete from users where name=? or address=?")
	public abstract void deleteUser(String name, String address);

	public abstract <T> T getUserList2(String name, String address);
	/*-
	   select * 
	   from 
	   users 
	      where 
	              name=:name 
	         and address=:address
	 */

}
