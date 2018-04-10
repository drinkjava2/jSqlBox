SqlBoxContext是一个无会话的(Sessionless)、线程安全的轻量级对象，最简单的使用方式是用new SqlBoxContext()或new SqlBoxContext(DataSource)方法创建一个实例，这样对于新手来说可以很快上手使用，但是对于需要改变默认配置、提供更多功能的情况下(如事务管理、日志输出等)，就必须进行更多配置，SqlBoxContext的构建参数里有一个SqlBoxContextConfig参数，就是专门用于这个配置的，通常配置完成后，SqlBoxContext的配置就固定下来不可以再改变了，以保证它“线程安全”的使用目的。SqlBoxContext通常的配置方式如下：
```
DataSource dataSource=....;
SqlBoxContextConfig config=new SqlBoxContextConfig();
config.setXXXX();
config.setXXXX();
....
SqlBoxContext ctx=new SqlBoxContext(dataSource, config);
```
SqlBoxContext也可以使用Spring之类的IOC工具来配置，见demo目录下的"jsqlbox-spring"项目示例。  
　　  
SqlBoxContextConfig对象有以下配置方法:  
* setAllowSqlSql(Boolean);//开启或关闭日志输出功能，详见“日志配置”一节  
* setLogger(DbProLogger);//设定一个日志处理器，详见“日志配置”一节  
* setBatchSize(Integer);//设定批处理默认缓存条数，详见“批处理”一节  
* setConnectionManager(ConnectionManager);//设定连接管理器，详见“事务配置”一节  
* setHandlers(List<ResultSetHandler>);//设定一组全局Handler类实例，详见“Handler类介绍”一节  
* setTemplateEngine(SqlTemplateEngine);//设定SqlBoxContext的模板引擎，详见“模板配置”一节  
* setDialect(Dialect dialect);//手工指定SqlBoxContext的数据库方言，而不是由jSqlBox自动根据DataSource来猜测方言类型

利用传入一个SqlBoxContextConfig实例参数进行配置，是正常的做法，但是SqlBoxContext还提供了以下静态方法，用于快速配置，只要在new SlqBoxContext()或new SlqBoxContext(DataSource)方法之前调用以下静态方法，就可以达到配置目的:  
* setGlobalDialect(Dialect);  
* setGlobalAllowShowSql(Boolean);  
* setGlobalBatchSize(Integer);  
* setGlobalConnectionManager(ConnectionManager);  
* setGlobalLogger(DbProLogger);  
* setGlobalHandlers(List<ResultSetHandler>);  
* setGlobalTemplateEngine(SqlTemplateEngine);

这些方法告诉SqlBoxContext，如果没有SqlBoxContextConfig实例参数传入的话，默认的各个配置参数是什么。所有方法都是以“setGlobal”开头，表示它是一个全局开关方法，会影响到整个项目的默认配置，所以使用时要慎重，通常只在程序开始时运行一次，设定好各个默认参数。注意它只影响到下一次SqlBoxContext实例的创建，例如在SqlBoxContext已经创建好的情况下调用setGlobalAllowShowSql(true)是不会影响到之前创建的SqlBoxContext的日志输出的。

SqlBoxContext中还有一个特殊的静态方法setGlobalSqlBoxContext(SqlBoxContext)，表示设定整个项目的默认全局SqlBoxContext实例，通常用于ActiveRecord类来使用，例如“new User().insert();”这种用法，如果User类没有配置自己的SqlBoxContext(详见“固定和动态配置”一节），它就会调用这个默认全局SqlBoxContext实例来进行数据库存取操作。  
在程序中可以用静态方法SqlBoxContext.getGlobalSqlBoxContext()或gctx()方法来获取这个默认全局SqlBoxContext实例。  
