package activerecordtext;

 import static com.github.drinkjava2.jdbpro.JDBPRO.param;

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.helloworld.UsageAndSpeedTest.UserAR;
import com.github.drinkjava2.jdbpro.PreparedSQL;
import com.github.drinkjava2.jsqlbox.TextUtils;
import com.github.drinkjava2.jsqlbox.annotation.Handlers;
import com.github.drinkjava2.jsqlbox.annotation.Sql;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;

/**
 * This is a sample to show put SQL in multiple line Strings(Text), a
 * build-helper-maven-plugin in pom.xml is required, detail see Readme.md
 * 
 * @author Yong Zhu
 */
public class TextedUser extends UserAR {

	@Sql("insert into users (name,address) values(?,?)")
	public void insertOneUser(String name, String address) {
		this.guess(name, address);
	};

	public PreparedSQL updateAllUserPreSql(String name, String address) {
		return this.guessPreparedSQL(name, address);
	};
	/*- 
	 update users 
	      set name=?, address=?
	*/

	@Handlers(MapListHandler.class)
	public List<Map<String, Object>> selectUsers(String name, String address) {
		return this.guess(name, address);
	};
	/*- 
	 select * from users 
	     where name=? and address=?
	*/

	public void deleteUsers(String name, String address) {
		this.guess(name, address);
	};
	/*- 
	 delete from users where name=? or address=?
	*/

	@Handlers(MapListHandler.class)
	public List<Map<String, Object>> selectUsersMapListByText(String name, String address) {
		return this.guess(name, address);
	};
	/*-
	   select *
	   from  users 
	      where 
	         name=:name 
	         and address=:address
	 */

	public List<Map<String, Object>> selectUsersMapListByText2(String name, String address) {
		return this.guess(name, address);
	};
	/*-
	   select u.** 
	   from  users u 
	         where 
	         u.name=:name 
	         and u.address=:address
	 */

	public List<TextedUser> selectUsersByText2(String name, String address) {
		return this.guess(name, address);
	}
	/*-
	   select u.** 
	   from 
	   users u
	      where 
	         u.name=? and address=?
	 */

	public List<TextedUser> selectUsersByText3(String name, String address) {
		return this.ctx().iQuery(new EntityListHandler(TextedUser.class), this.guessSQL(), param(name), param(address));
	}
	/*-
	   select u.** 
	   from 
	   users u
	      where 
	         u.name=? and address=?
	 */

	public static void main(String[] args) {
		String javaSourceCode = TextUtils.getJavaSourceCodeUTF8(TextedUser.class);
		System.out.println(javaSourceCode);
	}

}
