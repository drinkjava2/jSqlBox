jDialects is an important part of jSqlBox. It provides functions such as DDL generation, JPA annotation parsing, paging, and function transformation. The jSqlBox project depends on jDialects.  
jDialects is a stand-alone project that supports more than 70 database dialects. For a detailed introduction to it, please go to its home page: [jDialects](../../jDialects)  

When you create a SqlBoxContext instance using the SqlBoxContext ctx= new SqlBoxContext(dataSource) method, you can use the ctx.getDialect() method to obtain the Dialect instance of the current data source. Of course, you can manually specify the dialect type of the SqlBoxContext. For details, see the "Advanced Configuration of the jSqlBox" chapter.   

jDialects has many features, please refer to its home page for details. Here is a brief introduction:    
1. Database DDL generation function  
String[] ddls = ctx.getDialect().toCreateDDL(HelloWorldTest.class);  
Dialect's toCreateDDL and toDropAndCreateDDL methods can translate entity classes into database DDL scripts.  

2. Paging function  
In the place of the SQL text, you can use the dialect.pagin(pageNumber, pageSize, sql) method to convert an ordinary SQL to the paged SQL corresponding to the current database dialect. In the jSqlBox, the ctx.pagin() method is equivalent to ctx. The getDialect().pagin() method is as follows for a pagination query example:  
 List<Map<String, Object>> users = ctx.nQuery(new MapListHandler(), ctx.pagin(2, 5, "select concat(firstName, ' ', lastName) as UserName, age from users where age>?" ), 50);  
jSqlBox can also use the Handler method like PaginHandler to perform page transformations. For example:  
List<User> users = ctx.nQuery(new Wrap(new EntityListHandler(User.class), new PaginHandler(2, 5)), "select u.** from users u where u.age>?", 0);  
This method also calls jDialects's pagin() method at the bottom.  

3. Function conversion function  
In the place of SQL text, you can use dialect.trans (sql) method to convert a SQL containing "common function" to SQL corresponding to the current database dialect, and all "generic functions" are converted into unique functions of the current database. In jSqlBox, the ctx.trans() method is equivalent to the ctx.getDialect().trans() method.  

4. Combination of paging and function transformation  
The ctx.paginAndTrans(pageNumber, pageSize, sql) method can page and function transform sql at the same time.  

5. Primary key generation and JPA annotation support  
jDialects supports ten primary key generation methods and support for major JPA annotations. Please see its home page for details.  

In addition, the TableModel class in jDialects occupies an important position in jSqlBox. TableModel is a virtual data table model that has nothing to do with the actual database. jSqlBox also borrows this virtual table model for dynamic configuration support. Please refer to “Fixed and Dynamic Configuration” for details. " chapter.  