package sql;

import com.github.drinkjava2.jdbpro.Text;

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
public class SQLs {
	public static class SelectUsers1 extends Text {
		/*-  
		select * from users where 
		       age>#{age} or name='${name}'
		*/
	}

	public static class SelectUsers2 extends Text {
		/*-  
			select u.* from users u where 1=1
			<%if(!isEmpty(u.age)){%>
			  and u.age>#{u.age} 
			<%}%> 
			<%if(!isEmpty(u.name)){%>
			  and u.name=#{u.name} 
			<%}%> 
		*/
	}

}
