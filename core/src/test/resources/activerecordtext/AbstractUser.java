package activerecordtext;

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.handlers.MapListHandler;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.annotation.Ioc;
import com.github.drinkjava2.jsqlbox.annotation.Model;
import com.github.drinkjava2.jsqlbox.annotation.New;
import com.github.drinkjava2.jsqlbox.annotation.Sql;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;

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

	public abstract void updateUserPreparedSQL(String name, String address);
	/*- 
	   update users 
	      set name=:name, address=:address
	*/

	@New(MapListHandler.class)
	public abstract List<Map<String, Object>> selectUserListMap(String name, String address);
	/*-
	   select * 
	   from 
	   users 
	      where 
	         name=:name 
	         and  address=:address
	 */

	@Model(TextedUser.class)
	@Ioc(EntityListHandlerBox1.class)
	public abstract List<TextedUser> selectAbstractUserList1(String name, String address);
	/*-
	   select u.*
	   from 
	   users u
	      where 
	         u.name=:name and address=:address
	 */

	public static class EntityListHandlerBox1 extends BeanBox {
		public EntityListHandler create() {
			return new EntityListHandler();
		}
	}

	@Ioc(EntityListHandlerBox2.class)
	public abstract List<TextedUser> selectAbstractUserList2(String name, TextedUser u);
	/*-
	   select u.* 
	   from 
	   users u
	      where 
	         u.name=:name and address=#{u.address}
	 */

	public static class EntityListHandlerBox2 extends BeanBox {
		public Object[] create() {
			return new Object[] { new EntityListHandler(), TextedUser.class };
		}
	}

}
