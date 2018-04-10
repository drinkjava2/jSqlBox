The Handler class in jSqlBox is equivalent to Interceptor in other persistence tools because it can transform SQL text and query results before and after SQL execution to achieve some special purposes, such as log output, result set assembly, etc., in jSqlBox. Handler class can be directly involved in the query as a parameter in the method, which is a use of DbUtils's QueryRunner, is a small highlight of DbUtils, such as:
```
List<Map<String, Object>> result =
  Ctx.query("select u.* from DemoUser u where u.age>?", new MapListHandler(),0);
```
The Handler class function in jSqlBox has long been more than simply transforming the result set, but because of inheriting from the DbUtils architecture, all Handler classes in jSqlBox must implement the ResultSetHandler interface instead of a more appropriately named interface. This is the jSqlBox architecture. A small imperfection.
 
The Handler class in jSqlBox is divided into three categories based on the source:
1. From DbUtils, this type of Handler class can only transform the results returned by the SQL query, mainly the following:
ArrayHandler
ArrayListHandler
BeanHandler
BeanListHandler
BeanMapHandler
ColumnListHandler
KeyedHandler
MapHandler
MapListHandler
ScalarHandler
The specific role of each Handler please refer to the DbUtils user manual, not much to introduce here.

2. The jDbPro module of jSqlBox inherits DbUtils and adds the following Handler class:
SimpleCacheHandler: This is a caching tool that uses a memory HashMap to implement a simple LRU query cache.
PrintSqlHandler: This is a debug Handler that prints out SQL before SQL execution.
Wrap: This is a wrapper class, the role is to package different Handler together, this method can be put into multiple Handler, use examples are as follows:
```
List<DemoUser> result = ctx.nQuery(new Wrap(new EntityListHandler(DemoUser.class),
  New PaginHandler(1, 5), PrintSqlHandler.class), "select u.** from DemoUser u where u.age>?", 0);
```
The Wrap constructor also allows receiving a class as a parameter, such as PrintSqlHandler.class in the above example.
 
3. jSqlBox inherits jDbPro and adds the following Handler class:
PaginHandler: This is a paging Handler, use examples to see, used to convert ordinary SQL to paging SQL, which calls jDialects paging function.
EntitySqlMapListHandler: This is used to process the "select u.** from ..." non-standard SQL and assemble the query results into a List\<Map\<String,Object\>\> structure.
EntityListHandler: This is used to process non-standard SQL "select u.** from ..." and assemble the query result into a List\<Entity\> structure.
EntityNetHandler: This is used to process non-standard SQL "select u.** from ..." and assemble the query results into an EntityNet.  

There are two special uses of the Handler class:  
Usage 1: Configure as a global Handler  
When the SqlBoxContext is constructed, a set of Handler classes can be set. This set of Handler classes will be called when all SQL is executed, so it is called global Handler:
```
DataSource ds=....;
SqlBoxContextConfig config=new SqlBoxContextConfig();
config.setHandlers(new PrintSqlHandler(), new SimpleCacheHandler());
config.setAllowSqlSql(true);
SqlBoxContext ctx=new SqlBoxContext(ds, config);
```
All SQL execution will print the SQL output and turn on the query cache.  
The scope of the global Handler is relatively large, so if there is no special reason, it is generally not recommended to configure the global Handler.  

Usage 2: thread-local variables Handler  
Using thread-local variables, you can forcibly add a set of Handler classes to any jSqlBox's SQL-related methods at any time without passing them as method body parameters. Example usage can force pagination and print SQL:
```
ctx.getThreadedHandlers().add(new PaginHandler(1, 5));
ctx.getThreadedHandlers().add(new PrintSqlHandler());
Try{
 ctx.nQuery("select * from users");
}
 ctx.getThreadedHandlers().clear();
}
```
Note: Although jSqlBox automatically clears the thread-local variable Handler after each SQL execution, but for security purposes (such as when an exception occurs), this usage must use try...finally to ensure that thread-local variables are cleared to avoid threads Local variable leaks cause serious logic errors in subsequent programs.