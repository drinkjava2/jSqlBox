package textsample;

import java.util.List;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jsqlbox.annotation.Handler;
import com.github.drinkjava2.jsqlbox.annotation.Sql;

/*-
 * This is a sample to show put SQL in annotation and in multiple line Strings, this is a
 * Java file, and also is a resource file, to do that put a build-helper-maven-plugin
 * plugin in pom.xml, detail see user manual of jSqlBox.
 * 
* @author Yong Zhu 
*/
public interface ISampleUser {
	// insertOneUser method is extended from SampleUser2

	@Sql("update users set name=?, address=?")
	public void updateAllUser(String name, String address);

	@Sql("delete from users where name=? or address=?")
	public void deleteUsers(String name, String address);

	@Handler(MapListHandler.class)
	public List<SampleUser> selectUsers(String name, String address);
	/*-
	   select * 
	   from 
	   users 
	      where 
	         name=:name 
	         and address=:address
	 */

}
