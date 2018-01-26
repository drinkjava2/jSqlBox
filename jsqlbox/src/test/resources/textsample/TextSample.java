package textsample;

import java.util.List;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jsqlbox.annotation.Handler;
import com.github.drinkjava2.jsqlbox.annotation.Sql;

import functiontest.speedtest.SpeedTest.User;

/*-
 * This is a sample to show let Java support multiple lines String, this is a
 * Java file, and also is a resource file, so can read the source code as normal
 * resource file. only thing need do is to put a build-helper-maven-plugin
 * plugin in pom.xml:  
 * 
* @author Yong Zhu 
*/
public interface TextSample {

	@Sql("insert into users (name,address) values(?,?)")
	public void insertOneUser(String name, String address);

	@Sql("update users set name=?, address=?")
	public void updateAllUser(String name, String address);

	@Handler(MapListHandler.class)
	@Sql("select * from users where name=? and address=?")
	public List<User> getUserList(String name, String address);

	@Sql("delete from users where name=? or address=?")
	public void deleteUser(String name, String address);

	public <T> T getUserList2(String name, String address);
	/*-
	   select 
	      * 
	   from 
	   users 
	      where 
	              name=:name 
	         and address=:address
	 */

}
