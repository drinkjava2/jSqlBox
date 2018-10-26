package text;

import static com.github.drinkjava2.jsqlbox.JSQLBOX.pExecute;
import static com.github.drinkjava2.jsqlbox.JSQLBOX.pQueryForString;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.config.TestBase;
import com.github.drinkjava2.jdbpro.Text;

/**
 * This is a sample to show put SQL in multiple line Strings(Text), a
 * build-helper-maven-plugin in pom.xml is required, otherwise need copy java
 * file in resources folder manually, detail see Readme.md
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */
public class SqlTexts extends TestBase {

	public static class SelectUsers extends Text {
		/*-  
		select u.* 
		from 
		users u
		where 
		 u.name=:name and u.address=#{u.address}
		*/
	}

	public static class UpdateUsers extends Text {
		/*-  
		 update users 
		 set name=?, address=?
		 where not(name is null)
		*/
	}

	public static class InsertDemo extends Text {
		/*-  
		insert into demo
		(id, name)
		values(?, ?)
		*/
	}

	@Test
	public void test() {
		pExecute(new InsertDemo(), "1", "Foo");
		pExecute(InsertDemo.class, "2", "Bar");
		Assert.assertEquals("Foo", pQueryForString("select name from demo where id=?", "1"));
		Assert.assertEquals("Bar", pQueryForString("select name from demo where id=?", "2"));
	}

}
