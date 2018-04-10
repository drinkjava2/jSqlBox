The jSqlBox log is configured as follows:
```
DataSource dataSource=....;
SqlBoxContextConfig config=new SqlBoxContextConfig();
config.setAllowSqlSql(true);//Enable or disable the log output function, the default is to close, that is not output SQL and parameters to the log
config.setLogger (xxx) ;/ / set a logger, if you do not configure, default use jSqlBox comes with DefaultDbProLogger
SqlBoxContext ctx=new SqlBoxContext (dataSource, config);
```
The DefaultDbProLogger that comes with jSqlBox uses the Logger provided by the JDK by default, and if it finds that Apache Commons Logging exists in the project classpath, then commongsLogging is used, if Apache Commons Logging is also configured to call Log4J, for example in common-logging.properties The file has the following configuration:
org.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger
Then turn to use Log4j as log output and read its log4j.properties file. This design is to ensure that Logger package conflicts do not occur when jSqlBox is used in any environment.  

If you want to replace the default Logger of jSqlBox with your own developed Logger, you need to implement the DbProLogger interface and set it with the config.setLogger (custom logger) method.  

The SqlBoxContext also provides the following static methods for quick configuration of the log, as long as the following static methods are called before the new SlqBoxContext() or new SlqBoxContext(DataSource) method, the configuration log can be reached:  
SqlBoxContext.setGlobalAllowShowSql(Boolean);  
SqlBoxContext.setGlobalLogger(DbProLogger);  
These methods all start with "setGlobal", which means that it is a global switch method that will affect the default configuration of the entire project, so use it with caution, usually only once at the beginning of the program. Note that it only affects the creation of the next SlqBoxContext instance. For example, calling setGlobalAllowShowSql(true) without affecting the log output of the previously created SqlBoxContext when the SqlBoxContext instance has been created.  