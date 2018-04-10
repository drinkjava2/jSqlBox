### Configure jSqlBox
jSqlBox requires Java6 or above support, add in project pom.xml file:
```
<dependency>
   <groupId>com.github.drinkjava2</groupId>
   <artifactId>jsqlbox</artifactId>
   <version>1.0.7</version> (or the latest version)
</dependency>
```
Maven will automatically download jsqlbox-1.0.7.jar, commons-dbutils-1.7.jar, and jdialects-1.0.7.jar for a total of approximately 500k. From the 1.0.7 version jDbPro module is included in the jSqlBox in the form of source code, so from the previous 4 dependent packages into 3, (of course jDbPro module in the central library there is a separate release package).  
jSqlBox works in auto-commit mode by default. If you want to configure jSqlBox transactions, you must also add jTransactions dependencies and select an IOC/AOP tool. For details, see the jTransactions project and the two demonstration Web projects "jBooox" in the jSqlBox directory. "jsqlbox-spring".  

Getting Started Example:
```
Public class HelloWorldTest {
Private String name;

Public String getName() {
Return name;
}

Public void setName(String name) {
This.name = name;
}

@Test
Public void doText() {
HikariDataSource ds = new HikariDataSource();
ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
ds.setDriverClassName("org.h2.Driver");
ds.setUsername("sa");
ds.setPassword("");
ds.setMaximumPoolSize(8);
ds.setConnectionTimeout(2000);

SqlBoxContext ctx = new SqlBoxContext(ds);
String[] ddls = ctx.getDialect().toCreateDDL(HelloWorldTest.class);
For (String ddl : ddls)
ctx.nExecute(ddl);

HelloWorldTest hello = new HelloWorldTest();
hello.setName("ActiveRecordDemoTest");
Ctx.insert(hello);
Assert.assertEquals("ActiveRecordDemoTest", ctx.nQueryForString("select name from HelloWorldTest"));
Ds.close();
}
}
```
This is just the most basic configuration demo, run the unit test, no error, indicating that the database has successfully inserted a row of data. The getDialect() method in the above example obtains the Dialect instance of the current SqlBoxContext and uses the Dialect function to convert the entity bean into a database script.  

Using jSqlBox first create a SqlBoxContext instance, there are several constructors:  
SqlBoxContext()  
SqlBoxContext(SqlBoxContextConfig)  
SqlBoxContext(DataSource)  
SqlBoxContext(DataSource, SqlBoxContextConfig)  
When the constructor parameter is empty, it means to create a SqlBoxConext instance without a data source. In this case, because the SqlBoxContext does not know where the data source is, it must add a Connection instance as a parameter to each method when it is used. For example,  
Ctx.query(conn, "select * from users where name=?", "tom");  

When there is a DataSource instance in the constructor parameter, it means to create a SqlBoxConext instance with a data source. In this case, the Connection instance is not required as a parameter in the method. For example,   ctx.query("select * from users where name=?", " Tom");  

Classmates familiar with DbUtils may be familiar with these usages because SqlBoxContext is a subclass of QueryRunner, so there are also constructors similar to DbUtils' QueryRunner and inherit all methods of QueryRunner.  
 
The SqlBoxContextConfig in the constructor parameter is used to create the SqlBoxContext instance in a complex case, such as settings for logs, templates, dialects, transactions, and Handler classes. For details, see the "jSqlBox Advanced Configuration" chapter.  

SqlBoxContext is a thread-safe instance, that is, it can be shared by multiple threads. In order to avoid affecting other threads, usually once it is created, all its configurations are no longer changed. If you need to change the SqlBoxContext instance configuration you have created at runtime, you can create a new SqlBoxContext instance. If the original SqlBoxContext is not a global variable, it is automatically destroyed by the Java memory manager after the thread ends.  