自定义Handler类在jSqlBox中是很简单的，只需要实现相关的接口即可，与自定义Handler类相关的接口有：  
ResultSetHandler:所有jSqlBox的Handler必须实现这个接口，它有一个handle方法用于对Sql执行结果集进行变换。  
BeforeSqlHandler:有一个handleSql方法，在Sql执行前被调用一次。  
AroundSqlHandler:有handleSql和handleResult两个方法，分别在Sql执行前和执行后被调用一次。  
CacheSqlHandler:有readFromCache和writeToCache两个方法，分别在Sql执行前和执行后被调用一次，用于查询缓存的实现。  

例如自定义一个Handler类，在SQL执行前打印一段"Hello",结束后打印"Bye"，示例如下：
```
  public static class MyAroundSqlHandler implements ResultSetHandler, AroundSqlHandler {
  @Override
  public String handleSql(QueryRunner query, String sql, Object... params) {
   System.out.println("Hello");
   return sql;
  }

  @Override
  public Object handleResult(QueryRunner query, Object result) {
   System.out.println("Bye");
   return result;
  }

  @Override
  public Object handle(ResultSet result) throws SQLException {
   return result;
  }
 }
```
参数QueryRunner是从DbUtils开始就继承下来的，在jSqlBox环境下，参数QueryRunner可以强制转换为SqlBoxContext类型。

主程序中就可以这样使用了:
```
List<Map<String, Object>> result = 
  ctx.nQuery(new Wrap(new MapListHandler(), new MyAroundSqlHandler()),
	 "select u.* from DemoUser u where u.age>?", 0);
```