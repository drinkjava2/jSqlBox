##jSQLBox (In Developing)
**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)  

jSQLBox是一个微型的、易学易用的、支持基本的透明持久化的工具，目标是用来代替功能强大但过于复杂的Hibernate以及一些简单的但功能不足的持久层工具如JDBC/JDBCTemplate/MyBatis/DButils/EBean/OpenJPA/jFinal/jActiveRecord/ActiveJDBC/JOOQ等。 目前jSQLBox项目正在开发阶段，欢迎有兴趣者加入。

一张对比图显示jSQLBox项目的定位和开发目标：
![image](jsqlbox.png)

jSQLBox优点:  
*提供CRUD方法，CRUD占持久层日常~70%的工作量，对一个持久层工具来说是必须提供的基本功能。  
*无DAO显示注入，将与业务无关的持久层对象从业务代码中剔除。  
*不重新发明SQL,直接使用原生SQL。  
*对JDBC的改进，SQLHelper类支持字符串拼接的SQL自动包装成preparedStatement，防止SQL注入，提高性能，提高可读性和可维护性。  
*低侵入，PO类不继承其它类，不继承其它接口(见下)  
*无配置，PO可由代码工具根据数据库自动生成，且无任何配置文件。  
*可配置，当数据库表字段与PO不一致时，或需要存取个别字段或按SQL存取时，可用配置的方式来解决，配置为纯Java类，类似jBeanBox项目。  
*多配置和动态配置，同一个PO可采用不同的配置以进行多种方式的存取,配置可以继承重用已有的配置，配置可以动态生成和修改。  
*一级缓存，与Hibernate类似，提供以ID为主键的L1缓存。  
*多主键支持，一级缓存支持多主键，适于开发使用了业务主键的（遗留）数据库。  
*脏检查(透明持久化)，与Hibernate类似，对PO代理实例的存取不再重复访问数据库，除低数据库压力。  
*同时提供PO原型和PO代理两种方式，对不需要透明持久化代理的简单操作可以直接使用原型PO以简化操作。  
*跨数据库, CRUD方法在主流数据库上通用。  
*没有OpenSessionInView、对象关联、LazyLoad带来的问题, PO类可以直接传递到View层(JSP)，PO自带的Dao类在View层事务终结后依然可以继续存取数据库(通常只读)。  
*易学易维护易扩充，大量借用成熟工具(内部借用了Spring的JdbcTemplate)，此项目源码将不超过3000行。  

jSQLBox缺点:  
*比较新，缺少测试，缺少开发者（如果看好这个项目，请伸出您的援助之手，任何建设和批评都会促使这个项目不断改进）  
*虽然不占用JAVA的单继承，但是通过直接在每个PO类源码中添加方法来实现ActiveRecord模式，这与实现接口等效，造成PO对jSQLBox类的依赖。  
*没有一对多，多对一概念，实体类无关联，无级联更新等功能，需手工维护。(但从另一方面看这也是优点，因为降低了复杂性和学习成本)  
*暂无版本检查和二级缓存功能，可能在后续版本中加入  
*无分页、全文检索等高级功能，需手工实现。  

##(以下为旧的README.md内容， 是关于jSQLBox项目最初的一些构思，实际开发中代码已与下文不符，待改正)
jSQLBox is a micro scale persistence tool based on Active Record design, simpler than Hibernate but more powerful than JDBC/JDBCTemplate/MyBatis/DButils/EBean/OpenJPA/jFinal/jActiveRecord/ActiveJDBC/JOOQ. 

Other persistence tools' problem:  
Hibernate: It's a ORM tool, too complicated, the XML or Annotation configurations are fixed, fields definations, "One to Many", "Many to One" relationships are fixed, hard to create/modify/re-use configuations at runtime. For example, can not temporally change configuration at runtime to exclude some fields to avoid lazy loading.  
Other DB tools: Have no a balance between powerfulness and simpliness.  

Feature of jSQLBox:  
1) Simple, very few source code, No XML, easy to learn and use.   
2) The Java-Based Bean configuration can be created/modified dynamically at runtime(similar like jBeanBox project).  
3) The configuration and Bean classes can be created by source code generation tool because it's a simple map of database tables, it's a database driven tool，design database first, then config it 2nd.  
4) jSQLBox is based on Active Record mode.  
5) There is no "many to one", "One to Many", "Cascade update" concept in jSQLBox, need load and maintence beans by hand.  
6) jSQLBox support transparent persistence based on a transaction scope bean cache (supported by AOP tool like jBeanBox)  
7) jSQLBox offers basic CURD methods, but encourage mixed use with raw SQL, it's safe to mix use CRUD methods and sql, jSQLBox do not re-invent SQL language, only do a simple pack of SQL to let it can work with bean cache.  
8) Current version jSQLBox only support MySQL, in future version will support most databases(depends on how many contrubutors.)  
9) To make it simple (The more time you learn = the harder to maintenance), jSQLBox project only focus on basic CRUD method + SQL + transparent persistence, do not offer complex functions like paginnation, lazy load.  

How to use jSQLBox?  
//to add  

How to import jSQLBox project into Eclipse?  
//to add

A basic introduction of how to use jSQLBox:
---
Example 1 - Basic configuration and CRUD & Transparent Persistence
```
public static class Order extends SQLBox{//Can automatically created by code generation tool
   String orderID;  
   String orderNO;  
   String customerID;  
   //getters & setters...
   { setDBTable("Order");   //if DB table name same as class name, no need write this line
     setDBField(orderID,"DBOrderID");//If DB column name same as field name, no need write this line
   }
}

public static class Customer extends SQLBox{// Can automatically created by code generation tool
   String customerID; 
   String customerName;  
   Integer totalOrderCounts;
   //getters & setters...
} 

public class Tester {
    public void insertOrder(String customerID){
      Order order=new Order(); 
      //".putInCache()" can omit if SQLBox.setBeanCache(true) be called, default is true
      order.setCustomerID(customerID).putInCache(); 
      
      //customer also be cached, as said before, default setBeanCache is true
      Customer customer=Customer.loadbyID(customerID); //load from DB or from cache 
      
      /* Below code use a raw sql, jSQLBox does not re-invent SQL language but support cached beans' 
       * dirty-checking to impliment transparent persistence, it's based on a threadlocal cache works on 
       * background, and it also do some other complex jobs like change sql to PreparedStatement to 
       * prevent SQL injection, all above functions are benefited from ActiveRecord design architecture.
       * ORDER() is a static imported method to tell SQLBox the table name.
       */
      customer.setOrderCounts('select count(*)+1 from "+ORDER()+" where "+ORDER.CustomerID()
                   +" = "+ order.getID()));
      
      //below line = SQLBox.defaultContext.flushCache();
      SQLBox.flushCache(); //All beans in cache, if be modified will be saved, 
      //In fact, no need explicitly call flushCache method, it can be configured in AOP like transation.
    }
    
    public static void main(String[] args) {
        Tester t=new Tester();
        t.insertOrder("001");
    }
} 
```
In above example, a default global singleton defaultContext be used, similar concept can see jBeanBox project example#3, this defaultContext included datasource setting, if in a project used multipule datasource, need use context's methods instead of directly use SQLBox's (default global context) static methods.
 
Example 2 - Bean configuration reuse
```
 public static class Customer2 extends Customer{ 
   String newAddress;
   //getters & setters...
   {setDBField(newAddress,"New_Address");   //address1 map to database field "db_address1" 
    setDBField(customerName, Null);//Set Null means customerName will not be fetched in SQL.
  }
} 

 public static class Customer3 extends Customer{ 
   { setOnlyYou(customerID);    
     setOnlyYou(newAddress); //only "customerID" and "newAddress" are kept, other fields are not visible
  }
} 
```
Now Customer2.class has a new field "newAddress" map to database field New_Address, Customer2.class can be created dynamically in services layer, it's a child class of Customer, and in loadByID method, it will fetch New_Address from DB but no longer fetch customerName. And Customer3 only keep customerID and newAddress fields, other fields are disappeared.

Example 3 - This example shows how to use jBeanBox and jSQLBOx to achieve "Declarative Transaction". This example integrated "C3P0" + "jSQLBox" + "Spring Declarative Transaction Service" but replace Spring's IOC/AOP core with jBeanBox, more detail of jBeanBox project can see https://github.com/drinkjava2/jBeanBox.
```
public class TesterBox extends BeanBox {
    static {
        BeanBox.defaultBeanBoxContext.setAOPAround("examples.example3_transaction.Test\\w*", "insert\\w*",
                new TxInterceptorBox(), "invoke");
        SQLBox.defaultContext.setDataSource(BeanBox.getBean(DSPoolBeanBox.class));       
    }

    static class DSPoolBeanBox extends BeanBox {
        {
            setClassOrValue(ComboPooledDataSource.class);
            setProperty("jdbcUrl", "jdbc:mysql://127.0.0.1:3306/test?user=root&password=yourpwd");
            setProperty("driverClass", "com.mysql.jdbc.Driver");
            setProperty("maxPoolSize", 10);
            setProperty("CheckoutTimeout", 2000);
        }
    }

    static class TxManagerBox extends BeanBox {
        {
            setClassOrValue(DataSourceTransactionManager.class);
            setProperty("dataSource", DSPoolBeanBox.class);
        }
    }

    static class TxInterceptorBox extends BeanBox {// Advice for Spring transaction
        {
            Properties props = new Properties();
            props.put("insert*", "PROPAGATION_REQUIRED");
            setConstructor(TransactionInterceptor.class, TxManagerBox.class, props);
        }
    }
    
    static class JSQLCacheIntercepterBox extends BeanBox {// Advice for jSQLBox Cache
        {
            Properties props = new Properties();
            props.put("insert*", "FLUSHCACHE_WHEN_EXIT");
            setConstructor(JSQLCacheIntercepter.class, SQLBox.defaultContext, props);
        }
    }
}

public class Tester { 

    public void insertUser() {
        SQLBox.execute("insert into "+USERS()+" values ('"+VAL("User1")+"')");
        // int i = 1 / 0; // Throw a runtime Exception to roll back transaction
        SQLBox.execute("insert into users values ('User2')");
    }

    public static void main(String[] args) {
        Tester tester = BeanBox.getBean(Tester.class);
        tester.insertUser();
    }
}
```

