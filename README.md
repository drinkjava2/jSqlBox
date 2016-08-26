#jSQLBox
====

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

jSQLBox is a micro scale ORM Framework, simpler than Hibernate but powerful than JDBC/JDBCTemplate/MyBatis/DButils.
##(jSQLBox is just start to build, welcome everyone join this project yong9981@gmail.com)  

Other ORM frameworks' problem:
The configurations of entity beans are fixed, fields definations, "One to Many", "Many to One" relationships are fixed, hard to create/modify configuations at runtime.  

Feature of jSQLBox:  
1) Simple, very few source code, No XML, Easy to learn and use.   
2) The Java-Based Bean configuration can be created/modified dynamically at runtime(similar like jBeanBox project). 
3) The Java-Based Bean configuration can be created by source code generation tool because it's a simple map of DB structure; 
4) There is no "many to one", "One to Many", "Cascade update" concept in jSQLBox, replaced by simple "hook" concept. hook method is manually called by user or create a configuration at runtime dynamically, this give more flexibility on business logic. And a default hook setting can be set at beginning if you make sure the business logic is fixed.
5) jSQLBox is based on Active Record design, although it supports transparent persistence by hook beans to build an "Object tree".
6) jSQLBox offers basic CURD methods, but still encourage mixed use with raw SQL, it's SQL safe. Not like Hibernate mixed use  
raw SQL may damage first level cache, to use raw SQL give more freedom on business logic implementation.
7) Current version jSQLBox only support MySQL, in future version may support other databases depends on if can find more develpers.
8) To make it easy to learn and use (The time you spent to learn = the hard to maintenance project), jSQLBox only focus on basic O-R Mapping, do not offer complicated functions like pagination/Lucene/caches/..., you need implement these function by yourself, like the project name said, it's only a SQL box, not an all-rounder box.

How to use jSQLBox?  
Download jsqlbox-x.x.jar or jsqlbox-x.x-source.jar from jSQLBox project site, and put below jars in lib folder:  
http://central.maven.org/maven2/.../....jar  
http://central.maven.org/maven2/.../....jar  
jSQLBox is managed as a Maven multi-module project, a "pom.xml" of above jars can be found in "jsqlbox-core" module.  

How to import jSQLBox project into Eclipse?  
1)install JDK1.6+, Git bash, Maven, assume you are in windows, on command mode, run:  
2)git clone https://github.com/drinkjava2/jSQLBox  
3)cd jSQLBox  
4)mvn eclipse:eclipse  
5)mvn test  
6)Open Eclipse, "import"->"Existing Projects into Workspace", select jSQLBox folder, there are 2 module projects "jsqlbox-core" and "jsqlbox-example" will be imported in Eclipse.

A basic introduction of how to use jSQLBox:
---
Example 1 - Basic CRUD & Transparent Persistence demo
```
public static class Order extends SQLBox{//Can be automatically created by source code generation tool
   String orderID;  
   String orderNO;  
   String customerID;  
   //getters & setters...
   { setDBTable("Order");   //if DB table name same as class name, no need set it manually 
     setDBField("orderID","DBOrderID");//If DB column name same as field name, no need set it manually 
  }
}

public static class Customer extends SQLBox{//Can be automatically created by source code generation tool
   String customerID; 
   String customerName;  
   Integer totalOrderCounts;
   //getters & setters...
} 

public class Tester {
    public void insertOrder(String customerID){
      Order order=new Order();
      order.setCustomerID(customerID);
      Customer customer=Customer.loadbyID(customerID).hookedBy(order);//equal to order.hook(customer);
      customer.setOrderCounts('select count(*)+1 from order where orderID=?', order.getID()));
      order.save();//customer also be saved, it's called Transparent Persistence
    }
    
    public static void main(String[] args) {
        Tester t=new Tester();
        t.insertOrder();
    }
} 
```
In above example, a default global singleton defaultContext injected datasource be used, similar concept see jBeanBox project example#3.
 
 
Example 2 - Bean extending
```
 public static class Customer2 extends Customer{ 
   String address1;
   String address2;
   //getters & setters...
   {setDBField("address1","db_address1");   //address1 map to database field "db_address1"
    //setDBField("address2",Null);//Set Null means address2 do not map to any db field, it's default setting
    setDBField("customerName",Null);//Set Null means customerName will not be fetched in automatic SQL.
  }
} 
```
Now Customer2.class has a new field "address1" map to database, and new field "address2" does not map to any database field.
Customer2.class can be created dynamically in services layer, it's a child class of Customer, and in loadByID method sql, it will fetch address1 from DB but no longer fetch customerName from DB.
 

