package test.examples.multipleLineSQL;

import static com.github.drinkjava2.jsqlbox.SqlHelper.empty;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jsqlbox.Dao;
import com.github.drinkjava2.jsqlbox.SqlTemplate;

import test.TestBase;

/**
 * This Example shows use Java file as SQL template, and it supports multiple
 * lines String, Note:<br/>
 * 1) Use formatter off to shut off Eclipse's formatter <br/>
 * 2) Copy source file SqlTemplate.java in class root folder, i.e. Maven's
 * resources folder.
 * 
 * @author Yong Zhu
 * @version 1.0.0
 * @since 1.0.0
 */

public class SqlTemplateDemo extends TestBase {

	//@formatter:off	
	public static class InsertUser extends SqlTemplate {
		public InsertUser(Object name, Object address, Object age){ 
		/* insert into 
		   users 
		   (username, address, age) */ empty(name,address,age);
		/* values  (?,?,?)*/ 
		}
	}
	
	public static class FindUsers extends SqlTemplate  {
		public FindUsers(Object name, Object age){ 
		/* select count(*) 
		   from
		   users
		  where */
		/* username=? */empty(name); 
		/* and age>? */empty(age);
		/* order by username */  
		}
	}

    public static class GetUserCount extends SqlTemplate  {    
        /* select count(*) 
           from users  */    
    }   
    
	public static class SqlTemplateEndTag{}
	
	@Test
	public void doTest() { 
		Dao.getDefaultContext().setShowSql(true);
		Dao.executeInsert(new InsertUser("Tom","BeiJing",10).toString());
		Dao.executeInsert(new InsertUser("Sam","ShangHai",20).toString()); 
		Assert.assertEquals((Integer) 1,  Dao.queryForInteger(new FindUsers("Sam",15).toString())); 
		Assert.assertEquals((Integer) 2,  Dao.queryForInteger(new GetUserCount().toString())); 
	} 
	
}