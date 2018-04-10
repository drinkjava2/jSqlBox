jSqlBox's query cache is implemented by the Handler class, and only for the query method to take effect. The first query reads the database, and subsequent queries will be read from the cache. The use of cache may cause dirty data, that is, the value in the cache is inconsistent with the value in the actual database, and should be used when using it. Using the jSqlBox's query caching feature, you usually add the Handler class manually to the query method. For example:
```
List<DemoUser> result = ctx.nQuery(
  New Wrap(new EntityListHandler(DemoUser.class), new SimpleCacheHandler()),
"select u.** from DemoUser u where u.age>?", 0);
```
jSqlBox comes with a simple cacheHandler SimpleCacheHandler is a simple LRU cache, the use of memory in the LinkedHashMap table to cache the SQL query results, when the cache is full of data, the longest has not been accessed to remove the data, SimpleCacheHandler has two structures the way:
```
SimpleCacheHandler()
SimpleCacheHandler(int aliveSeconds)
```
The latter configuration allows to set the cache invalidation time in units of seconds (the internal value will be adjusted to the nearest value with 1, 10, 100, 1000...). If you use the first parameterless constructor, the default cache expiration time is 1000 seconds.  

Another way to use the cache is to set the Cache SimpleCacheHandler class to a global cache when the SqlBoxContext is constructed, such as:  
```
SqlBoxContextConfig config=new SqlBoxContextConfig();
config.setHandlers(new SimpleCacheHandler());
SqlBoxContext ctx=new SqlBoxContext(ds, config);
```
This eliminates the need to manually add the cache Handler each time in the query method, but because the cache may cause the presence of dirty data, this method should not normally participate in transaction operations. That is, you can create a separate SqlBoxContext that supports caching. It specifically allocates a data source, usually only for queries.  

jSqlBox query caching mechanism is relatively simple, and MyBatis, it is coarse-grained, not for the rowset, the primary key cache using SQL + parameters this way, as long as the SQL spelling or parameter values ​​are any different, jSqlBox that is this Two different queries, so what queries need to use the cache need to be carefully considered, and queries with frequently changing parameters should not use the cache.  

SimpleCacheHandler only allows storing up to 500 records. If you want to write your own cache Handler class, you must implement both the ResultSetHandler and CacheSqlHandler interfaces. You can refer to the source code of the SimpleCacheHandler class.  