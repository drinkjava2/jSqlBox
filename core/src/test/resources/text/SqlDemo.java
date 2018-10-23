package text;

import com.github.drinkjava2.jsqlbox.Text;

/**
 * This is a sample to show put SQL in multiple line Strings(Text), a
 * build-helper-maven-plugin in pom.xml is required, detail see Readme.md
 * 
 * @author Yong Zhu
 */
public class SqlDemo {

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

	public static void main(String[] args) {
		System.out.println(new SelectUsers());
	}
}
