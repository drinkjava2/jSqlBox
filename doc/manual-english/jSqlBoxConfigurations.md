SqlBoxContext is a sessionless, thread-safe lightweight object. The easiest way to use it is to create an instance using the new SqlBoxContext() or new SqlBoxContext(DataSource) methods, which can be used quickly by newcomers. But for the case that you need to change the default configuration and provide more features (such as transaction management, log output, etc.), you must do more configuration. There is a SqlBoxContextConfig parameter in the SqlBoxContext's build parameters, which is dedicated to this configuration. After the configuration is completed, the configuration of the SqlBoxContext is fixed and cannot be changed to ensure its use is "thread-safe".   

SqlBoxContext is usually configured as follows:
```
DataSource dataSource=....;
SqlBoxContextConfig config=new SqlBoxContextConfig();
config.setXXXX();
config.setXXXX();
....
SqlBoxContext ctx=new SqlBoxContext(dataSource, config);
```
The SqlBoxContext can also be configured using Spring-like IOC tools. See the "jsqlbox-spring" project example in the demo directory.   

The SqlBoxContextConfig object has the following configuration methods:  
* setAllowSqlSql(Boolean);//Open or close the log output function  
* setLogger(DbProLogger);//Set a log handler   
* setBatchSize(Integer);//Set the default number of batch cache  
* setConnectionManager (ConnectionManager) ;/ / set the connection manager  
* setHandlers(List<ResultSetHandler>);//Sets a set of global Handler class instances  
* setTemplateEngine(SqlTemplateEngine);//Set the SqlBoxContext template engine  
* setDialect(Dialect dialect);// manually specify the database dialect of SqlBoxContext instead of jSqlBox   automatically guessing the dialect type based on DataSource  

It is normal to use a parameter that is passed in a SqlBoxContextConfig instance, but the SqlBoxContext also provides the following static methods for quick configuration. Just call the following static method before the new SlqBoxContext() or new SlqBoxContext(DataSource) method. To achieve the configuration purpose:  
* setGlobalDialect(Dialect);  
* setGlobalAllowShowSql(Boolean);  
* setGlobalBatchSize(Integer);  
* setGlobalConnectionManager(ConnectionManager);  
* setGlobalLogger(DbProLogger);  
* setGlobalHandlers(List<ResultSetHandler>);  
* setGlobalTemplateEngine(SqlTemplateEngine);  

These methods tell the SqlBoxContext what the default configuration parameters are if there are no SqlBoxContextConfig instance parameters passed in. All methods start with “setGlobal”, which means that it is a global switch method that will affect the default configuration of the entire project. Therefore, it should be used with caution. Normally, it is only run once at the beginning of the program, and each default parameter is set. Note that it only affects the creation of the next SqlBoxContext instance. For example, calling setGlobalAllowShowSql(true) without affecting the log output of the previously created SqlBoxContext when the SqlBoxContext has been created.  

SqlBoxContext also has a special static method setGlobalSqlBoxContext(SqlBoxContext), which means to set the default global SqlBoxContext instance of the entire project, usually used for ActiveRecord class, for example "new User().insert();" The User class does not configure its own SqlBoxContext (see the "Fixed and Dynamic Configuration" section), it will call this default global SqlBoxContext instance for database access operations.  
In the program you can use the static method SqlBoxContext.getGlobalSqlBoxContext() or gctx() method to get this default global SqlBoxContext instance.  