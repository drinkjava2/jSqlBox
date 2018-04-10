If there are a large number of insert and update statements, you can use the database's bulk operations to speed up insertion and update, provided that the database supports batch operations (such as MySQL to set rewriteBatchedStatements=true). In jSqlBox, batch processing has the following usages:  

1. Batch methods inherited from DbUtils:
```
Batch(Connection, String, Object[][])
Batch(String, Object[][])
insertBatch(Connection, String, ResultSetHandler<T>, Object[][])
insertBatch(String, ResultSetHandler<T>, Object[][])
```
These methods throw SqlExecption exceptions, and the parameters are two-dimensional arrays.

2. From the DbPro module, add a batch method like:
```
nBatch(Connection, String, List<List<?>>)
nBatch(String, List<List<?>>)
nInsertBatch(Connection, String, ResultSetHandler<T>, List<List<?>>)
nInsertBatch(String, ResultSetHandler<T>, List<List<?>>)
```
These methods do not throw SqlExecption exceptions. If an error occurs during runtime, a runtime exception will be thrown. The parameter is a double List structure.  

Batch processing method  
There are three ways:
```
nBatchBegin()
nBatchEnd()
nBatchFlush()
```
For all SQL executions, you can set a switch with the nBatchBegin() method of the SqlBoxContext instance. Call ctx.nBatchBegin() before the batch starts, and call ctx.nBatchEnd after the end. All insert and update methods will be automatically compiled into a single switch. Batch operation. The nBatchFlush() method is used to forcibly flush the cache to the database in the middle to prevent sustaining memory and improve performance. The system automatically calls the nBatchFlush() method once every 100 SQL batches. You can use SqlBoxContext.setGlobalBatchSize(). Change this setting.  
The batch switch method is not as efficient as the first two methods, but can support arbitrary SQL write and update operations, and supports all write operations that occur under the current SqlBoxContext, including methods such as insert and update of the ActiveRecord instance.  
Also note that the batch switch method must use a try...finally block to ensure that the nBatchEnd() method is called at the end, otherwise the batch switch is always on, causing subsequent programs fail.  