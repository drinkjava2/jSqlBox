jSqlBox中的Handler类等同与其它持久层工具中的Interceptor，因为它可以在SQL的执行前后对SQL文本及查询结果进行变换，从而实现一些特殊目的，如日志输出、结果集装配等， 在jSqlBox中，Handler类是可以直接作为一个参数放在方法里参与查询的，这是从DbUtils的QueryRunner开始就有的一种用法，算是DbUtils的一个小亮点，如：
```
List<Map<String, Object>> result = 
  ctx.query("select u.* from DemoUser u where u.age>?", new MapListHandler(),0);
```
jSqlBox中的Handler类功能早已不止是简单地对结果集变换，但是因为继承于DbUtils架构的问题，jSqlBox的所有Handler类都必须实现ResultSetHandler接口而不能换一个命名更合适的接口，这是jSqlBox架构上的一个小缺憾。  
　　  
jSqlBox中的Handler类从来源来看，主要分为三类：  
1.从DbUtils带来的，这一类Handler类只能对SQL查询的返回结果进行变换，主要有以下几个：  
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
具体每个Handler的作用请参见DbUtils的使用手册，这里不多作介绍。  
　　  
2.jSqlBox的jDbPro模块在继承DbUtils的基础上，又添加了如下Handler类：  
SimpleCacheHandler：这是一个缓存工具，利用内存HashMap实现了简单的LRU查询缓存。  
PrintSqlHandler：这是一个调试Handler，用于SQL执行前打印出SQL。  
Wrap：这是一个包装类，作用是将不同的Handler打包在一起，这样一个方法里就可以塞进多个Handler了, 使用示例如下:  
```
List<DemoUser> result = ctx.nQuery(new Wrap(new EntityListHandler(DemoUser.class),
  new PaginHandler(1, 5), PrintSqlHandler.class), "select u.** from DemoUser u where u.age>?", 0);
```
Wrap构造函数也允许接收类作为参数，如上例中的PrintSqlHandler.class。
　　  

3.jSqlBox在继承jDbPro的基础上，又添加了如下Handler类：  
PaginHandler：这是一个分页Handler，使用示例见上，用于将普通SQL转化为分页SQL，其内部调用了jDialects的分页函数。  
EntitySqlMapListHandler：这个用于处理"select u.** from ..." 这种非标SQL，并将查询结果装配成一个List\<Map\<String,Object\>\>结构。  
EntityListHandler： 这个用于处理"select u.** from ..." 这种非标SQL, 并将查询结果装配成一个List\<Entity\>结构。  
EntityNetHandler：  这个用于处理"select u.** from ..." 这种非标SQL, 并将查询结果装配成一个EntityNet。  
 
Handler类还有两种特殊的用法:    
用法1： 配置成全局Handler
在SqlBoxContext构造时，可以设定一组Handler类，这组Handler类将会在所有SQL执行时被调用，所以被称为全局Handler:
```
DataSource ds=....;
SqlBoxContextConfig config=new SqlBoxContextConfig();
config.setHandlers(new PrintSqlHandler(), new SimpleCacheHandler());
config.setAllowSqlSql(true);
SqlBoxContext ctx=new SqlBoxContext(ds, config);
```
则所有SQL执行时都将打印SQL输出，并开启了查询缓存。  
全局Handler影响的范围比较大，所以如果没有特殊原因，一般不建议配置全局Handler。  

用法2：线程局部变量Handler  
利用线程局部变量，可以随时给任何jSqlBox的SQL相关方法强行添加一组Handler类，而无需作为方法体的参数传入，如下例用法可以强行加入分页和打印SQL：
```
ctx.getThreadedHandlers().add(new PaginHandler(1, 5));
ctx.getThreadedHandlers().add(new PrintSqlHandler());
try{
 ctx.nQuery("select * from users");
} finally{
 ctx.getThreadedHandlers().clear();
}
```
注意：虽然jSqlBox在每次SQL执行后会自动清空线程局部变量Handler，但为安全起见(如异常发生情况下)，这种用法必须用try...finally来确保清除线程局部变量，以避免线程局部变量泄漏造成后面的程序出现严重的逻辑错误。