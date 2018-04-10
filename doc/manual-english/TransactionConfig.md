jSqlBox works by default in the auto-commit mode. To make it work in transaction mode, there are usually three ways. The first is to open and close the transaction manually. An example is as follows:
```
 SqlBoxContext ctx=new SqlBoxContext();
 Connection conn= null;
 Try {
Conn= dataSource.getConnection();
conn.setAutoCommit(false);
Ctx.update(conn, 'update users set age=5');
        ......
Conn.commit();
 } Catch (SQLException e) {
Conn.rollback();
 } finally {
Ctx.close(conn);
 }
```
The limitations of the above methods are relatively large, because the method must pass a Connection parameter, and it is cumbersome to use, and it is rarely used in actual projects.  

The second way is to use Spring's TransactionAwareDataSourceProxy. This is done by setting up a proxy on DataSource to implement declarative transactions. It is suitable for the transformation of old programs. I don't like this model too much, so I won't go into details here.  

The third method is the declarative transaction that this article focuses on. By setting a SqlBoxContext to set a connection manager that supports declarative transactions, let's use a practical example to illustrate the declarative transaction configuration of jSqlBox. The source code can be found in the jBooox project in the demo directory:
First add the jTransactions dependency in pom.xml:
```
    <dependency>
        <groupId>com.github.drinkjava2</groupId>
        <artifactId>jtransactions</artifactId>
        <version>1.0.1</version>
    </dependency>
```
Configuration code:
```
Public class Initializer implements ServletContextListener {

Public static class DataSourceBox extends BeanBox {
{ setProperty("jdbcUrl", "jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
setProperty("driverClassName", "org.h2.Driver");
setProperty("username", "sa");
setProperty("password", "");
}

Public HikariDataSource create() {
HikariDataSource ds = new HikariDataSource();
ds.addDataSourceProperty("cachePrepStmts", true);
ds.addDataSourceProperty("prepStmtCacheSize", 250);
ds.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
ds.addDataSourceProperty("useServerPrepStmts", true);
ds.setMaximumPoolSize(10);
ds.setConnectionTimeout(5000);
this.setPreDestory("close");// jBeanBox will close pool
Return ds;
}
}

Public static class TxBox extends BeanBox {
{
            this.setConstructor(TinyTx.class, BeanBox.getBean(DataSourceBox.class),
Connection.TRANSACTION_READ_COMMITTED);
}
}

@Override
Public void contextInitialized(ServletContextEvent context) {
BeanBox.regAopAroundAnnotation(TX.class, TxBox.class);
SqlBoxContextConfig config = new SqlBoxContextConfig();
config.setConnectionManager(TinyTxConnectionManager.instance());
SqlBoxContext ctx = new SqlBoxContext((DataSource) BeanBox.getBean(DataSourceBox.class), config);
SqlBoxContext.setGlobalSqlBoxContext(ctx);
}

@Override
Public void contextDestroyed(ServletContextEvent context) {
SqlBoxContext.setGlobalSqlBoxContext(null);
BeanBox.defaultContext.close();
}

}
```

In simple terms, the core steps of the jSqlBox transaction configuration are the following:
```
BeanBox.regAopAroundAnnotation(TX.class, TxBox.class);
SqlBoxContextConfig config = new SqlBoxContextConfig();
config.setConnectionManager(TinyTxConnectionManager.instance());
SqlBoxContext ctx =
  New SqlBoxContext((DataSource) BeanBox.getBean(DataSourceBox.class), config);
```

The following is a more detailed introduction to the declarative transaction configuration in this example. The comparison is very simple. Students who are familiar with declarative transactions can quickly take a quick look:  
* Use declarative transactions, provided that you have an IOC/AOP tool to provide implementation support for aspect programming. This example uses jBeanBox. If you want to know how to use Spring and use Spring transactions to declarative transaction configuration for jSqlBox, see The jsqlbox-in-spring demo project in the demo directory.  
* The first thing to do is to ensure that the project is configured at startup. This example is a web project that implements the ServletContextListener interface and configures the configuration code in web.xml to run first.  
* Then the data source, usually configured as a singleton model, is demonstrated here using the H2 in-memory database + the HikariDataSource data pool. Note that the datapool does not forget to close after the project ends or after each unit test. The "this.setPreDestory("close");" in the above example indicates that if the jBeanBox environment is closed, the datapool's close method will be called. When the contextDestroyed method is called after the Web project ends, then the jBeanBox environment is closed and the data pool's close method is called.  
* TxBox is a singleton for transaction aspect processing. Its first parameter is a TinyTx which indicates that the TinyTx class in the jTransactions project is used. Its constructor accepts a data source and a transaction type parameter.  
* TX is an AOP surround annotation and the BeanBox.regAopAroundAnnotation(TX.class, TxBox.class) method binds the AOP annotation to a transaction aspect handling class (here TxBox class). If the TX annotation is annotated with a method of a class and the class is created using BeanBox.getBean (a class.class) this way, an instance of the proxy class will be obtained, and the TX method of the proxy class instance will be annotated. It will be handled by the corresponding instance of TxBox, which is the working principle of the declarative transaction.  
* If you do not want to use the default TX annotations, such as @Transaction, you can customize AOP annotations. See [here] for details (../blob/master/demo/jbooox/src/main/java/com /jsqlboxdemo/init/Initializer.java).  
* In order to let SqlBoxContext know to use declarative transactions, you need to configure its connection manager. Normally, jSqlBox will call DataSource's getConnection method to get a Connection to operate the database. After configuring the connection manager, get the Connection The work is handed to the connection manager, which in this case is a singleton of TinyTxConnectionManager.  
* This uses two objects in the jTransaction module, TinyTx and TinyTxConnectionManager, which are two paired objects, Meng is not defocused, Jiao is not separated from Meng, a facet callback is responsible for handling transaction methods, one is used to get and release the connection The communication between them is based on the same DataSource, TinyTx has saved a DataSource, and TinyTxConnectionManager uses DataSource stored in the SqlBoxContext.  
* jTransaction is a stand-alone, compact declarative transaction tool, where the TinyTx source code is only a few classes, if you want to switch to using Spring's transaction affairs, in addition to the two paired objects replaced by SpringTx and SpringTxConnectionManager, As well as adding Spring's library dependencies, the rest of this article applies.  