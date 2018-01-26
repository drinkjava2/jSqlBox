package textsample;

import java.util.List;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jdbpro.inline.SqlAndParams;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
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
public class ActiveUser extends ActiveRecord {

	@Sql("insert into users (name,address) values(?,?)")
	public void insertOneUser(String name, String address) {
		this.run(name, address);
	};

	@Sql("update users set name=?, address=?")
	public void updateAllUser(String name, String address) {
		this.run(name, address);
	};

	@Sql("delete from users where name=? or address=?")
	public void deleteUsers(String name, String address) {
		this.run(name, address);
	};

	@Handler(MapListHandler.class)
	public List<User> selectUsers(String name, String address) {
		return this.tRun(name, address);
		/*-
		   select * 
		   from 
		   users 
		      where 
		         name=:name 
		         and address=:address
		 */
	};

	public String getUserListSql() {
		return this.sql();
		/*-
		   select * 
		   from 
		   users 
		      where 
		         name=:name 
		         and address=:address
		 */
	};

	public SqlAndParams getUserSqlAndParams(String name, String address) {
		return this.sqlAndParams();
		/*-
		   select * 
		   from 
		   users 
		      where 
		         name=:name 
		         and address=:address
		 */
	};

}
