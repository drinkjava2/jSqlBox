package text;

import static com.github.drinkjava2.jsqlbox.DB.bind;
import static com.github.drinkjava2.jsqlbox.DB.par;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdbpro.Text;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.config.TestBase;

/**
 * This is a sample to show put SQL in multiple line Strings(Text), a plug-in
 * called "build-helper-maven-plugin" in pom.xml is required (can see the
 * pom.xml file of this project), otherwise need manually put a copy of java
 * source code file in resources folder.
 * 
 * If IDE is Eclipse, comments in this file will not be formatted, because it
 * use /*- comments
 * 
 * 
 * @author Yong Zhu
 * @since 2.0.4
 */
public class TextTest extends TestBase {
	{
		this.regTables(Demo.class);
	}

	public static class InsertDemoSQL extends Text {
		/*-  
		insert into demo
		(id, name)
		values(?, ?)
		*/
	}

	public static class UpdateDemoSQL extends Text {
		/*-  
		 update demo 
		 set name=#{d.name}
		 where id=:d.id
		*/
	}

	public static class SelectNameByIdSQL extends Text {
		/*-  
		select name from demo 
		     where id=?
		*/
	}

	@Test
	public void test() {
		DB.exe(new InsertDemoSQL(), par("1", "Foo"));

		Demo d = new Demo().putField("id", "1", "name", "Bar");
		DB.exe(DB.TEMPLATE, UpdateDemoSQL.class, bind("d", d));

		Assert.assertEquals("Bar", DB.qryString(SelectNameByIdSQL.class, par("1")));
	}

}
