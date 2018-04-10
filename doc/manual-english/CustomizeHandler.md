Custom Handler class is very simple in jSqlBox, only need to implement the relevant interface, and the interface related to custom Handler class is:  
ResultSetHandler: All jSqlBox Handlers must implement this interface. It has a handle method to transform the Sql execution result set.  
BeforeSqlHandler: There is a handleSql method that is called once before the Sql executes.  
AroundSqlHandler: There are two methods, handleSql and handleResult, which are called before and after Sql execution.  
CacheSqlHandler: There are two methods, readFromCache and writeToCache, which are called once before and after Sql execution, for query cache implementation.  

For example, customize a Handler class, print a "Hello" before the SQL execution, print "Bye" after the end, examples are as follows:  
```
  Public static class MyAroundSqlHandler implements ResultSetHandler, AroundSqlHandler {
  @Override
  Public String handleSql(QueryRunner query, String sql, Object... params) {
   System.out.println("Hello");
   Return sql;
  }

  @Override
  Public Object handleResult(QueryRunner query, Object result) {
   System.out.println("Bye");
   Return result;
  }

  @Override
  Public Object handle(ResultSet result) throws SQLException {
   Return result;
  }
 }
```
The parameter QueryRunner is inherited from the beginning of DbUtils. In the jSqlBox environment, the parameter QueryRunner can be cast to the SqlBoxContext type.  

The main program can be used like this:  
```
List<Map<String, Object>> result =
  ctx.nQuery(new Wrap(new MapListHandler(), new MyAroundSqlHandler()),
"select u.* from DemoUser u where u.age>?", 0);