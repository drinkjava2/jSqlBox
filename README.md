#jSQLBox
====

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

jSQLBox is a micro scale database access tool, simpler than Hibernate but more powerful and simplier than JDBC/JDBCTemplate/MyBatis/DButils.  
#(jSQLBox project is in pending, currently only have concept design)  

Other persistence tools' problem:
Hibernate: it's ORM tool, too complicated, the configurations of entity beans are fixed, fields definations, "One to Many", "Many to One" relationships are fixed, hard to create/modify configuations at runtime.  
Other tools:Have no a balance between powerfulness and simpliness. 

Feature of jSQLBox:  
1) Simple, very few source code, No XML, Easy to learn and use.   
2) The Java-Based Bean configuration can be created/modified dynamically at runtime(similar like jBeanBox project).  
3) The Java-Based Bean configuration can be created by source code generation tool because it's a simple map of DB structure;  
4) jSQLBox is based on Active Record design, but support transparent persistence by register beans in cache.  
5) There is no "many to one", "One to Many", "Cascade update" concept in jSQLBox.  
6) jSQLBox support transparent persistence based on a bean cache.  
7) jSQLBox offers basic CURD methods, but still encourage mixed use with raw SQL, it's SQL safe to mixed use CRUD mehtod and cache.  
8) Current version jSQLBox only support MySQL, in future version will support more databases depends how many develpers involved.  
8) To make it simple (The more time you learn = the harder to maintenance), jSQLBox only focus on basic CRUD method + SQL + transparent persistence, do not offer complicated functions.  

How to use jSQLBox?  
//to add  

How to import jSQLBox project into Eclipse?  
//to add

A basic introduction of how to use jSQLBox:
---
Example 1 - Basic CRUD & Transparent Persistence demo
```
public static class Order extends SQLBox{//Can automatically created by code generation tool based on DB
   String orderID;  
   String orderNO;  
   String customerID;  
   //getters & setters...
   { setDBTable("Order");   //if DB table name same as class name, no need write this line
     setDBField("orderID","DBOrderID");//If DB column name same as field name, no need write this line
   }
}

public static class Customer extends SQLBox{//Can automatically created by code generation tool based on DB
   String customerID; 
   String customerName;  
   Integer totalOrderCounts;
   //getters & setters...
} 

public class Tester {
    public void insertOrder(String customerID){
      Order order=new Order();
      order.setCustomerID(customerID).putinCache();  //.putinCache can omit if SQLBOX.setCacheAll(true) be set
      Customer customer=Customer.loadbyID(customerID);
      
      /* Important! below code use a raw sql, jSQLBox does not re-invent SQL language but support cache 
       * dirty-checking to impliment transparent persistence, it's based on a threadlocal variable works on background,
       * and it also do some other jobs like change it to PreparedStatement to avoid SQL injection, all above functions are 
       * benefited from ActiveRecord architecture.
       */
      customer.setOrderCounts('select count(*)+1 from "+ORDER()+" where "+ORDER.OrderID()+" = "+ order.getID()));
      
      SQLBOX.flushCache(); //All beans in cache are saved, = SQLBOX.defaultContext.flushCache();
    }
    
    public static void main(String[] args) {
        Tester t=new Tester();
        t.insertOrder();
    }
} 
```
In above example, a default global singleton defaultContext be used, similar concept see jBeanBox project example#3, this defaultContext included datasource setting.
 
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
   { setOnlyKeep(customerID);    
     setOnlyKeep(newAddress);  //only field "customerID" and "newAddress" are kept, other fields are not visible
  }
} 
```
Now Customer2.class has a new field "newAddress" map to database field New_Address, Customer2.class can be created dynamically in services layer, it's a child class of Customer, and in loadByID method, it will fetch New_Address from DB but no longer fetch customerName.
And Customer3 only keep customerID and newAddress fields, other fields are dispeared.

Example 3 - This example shows how to use jBeanBox and jSQLBOx to achieve "Declarative Transaction". This example integrated "C3P0" + "jSQLBox" + "Spring Declarative Transaction Service" but replace Spring's IOC/AOP core with jBeanBox, more detail of jBeanBox project can see https://github.com/drinkjava2/jBeanBox.
```
public class TesterBox extends BeanBox {
    static {
        BeanBox.defaultBeanBoxContext.setAOPAround("examples.example3_transaction.Test\\w*", "insert\\w*",
                new TxInterceptorBox(), "invoke");
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

    static class TxInterceptorBox extends BeanBox {// Advice
        {
            Properties props = new Properties();
            props.put("insert*", "PROPAGATION_REQUIRED");
            setConstructor(TransactionInterceptor.class, TxManagerBox.class, props);
        }
    }

    public static class SQLBoxBox extends BeanBox {
        {
            setConstructor(SQLBox.class, DSPoolBeanBox.class);
        }
    }
}

public class Tester { 

    public void insertUser() {
        SQLBox.execute("insert into "+USERS()+" values ('"+VALUE("User1")+"')");
        // int i = 1 / 0; // Throw a runtime Exception to roll back transaction
        SQLBox.execute("insert into users values ('User2')");
    }

    public static void main(String[] args) {
        Tester tester = BeanBox.getBean(Tester.class);
        tester.insertUser();
    }
}
```

