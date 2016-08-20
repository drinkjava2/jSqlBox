#jSQLBox
====

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

jSQLBox is a micro scale ORM Framework, simpler than Hibernate and EBean, based on same "box oriented programming"  
ideal similar like jBeanBox and jWebBox project.  
##(jSQLBox is only a concept design,  hasn't start to code, looking partners to do this project)  

Other ORM frameworks' problem:
The configurations of entity beans are fixed, "One to Many", "Many to One", "Cascade Update/Delete" relationships
are fixed, hard to create/modify configuations at runtime.  

Feature of jSQLBox:  
1) Simple, very few source code(less than 2000 lines), No XML, very few Annotations(if have). Easy to learn and use.  
2) The Java-Based Bean configuration is simple and easy to use, can be automatically created by source code generation tool.  
3) The Java-Based Bean configuration can be dynamically created/modified at runtime(similar like jBeanBox project).  
4) Do not force use to set "many to one", "One to Many", "Cascade update" relationships at beginning, user can set these relationships at runtime, this give more flexibility on business logic implementation.
5) jSQLBox is based on Active Record design, but, it supports transparent persistence benifits from it's dynamic Bean  
configuration.  
6) jSQLBox offers basic CURD methods but encourage mixed use with raw SQL, it's SQL safe. Not like Hibernate mixed use  
raw SQL may damage first level cache.
7) Current version jSQLBox only support MySQL, but it's easy to improve to support other databases.  
8) To make it easy to learn and use (The time you spent to learn = the hard other people to maintenance project), jSQLBox only focus on basic O-R Mapping, do not offer complicated functions like pagination/Lucene/caches/...

How to use jSQLBox?  
Download jsqlbox-x.x.jar or jsqlbox-x.x-src.jar from jSQLBox project site, and put below jars in lib folder:  
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
public static class Order extends SQLBox{//Automatically created by source code generation tool
   String orderID;  
   String orderNO;  
   String customerID;  
   //getters & setters...
   {//setDBTable("Order");   //if DB table name same as class name, default no need set it manually 
    //setDBField("orderID","DBOrderID");//If DB column name same as field name, default no need set it manually 
  }
}

public static class Customer extends SQLBox{//Automatically created by source code generation tool
   String customerID; 
   String customerName;  
   Integer totalOrderCounts;
   //getters & setters...
} 

public class Tester {
    public void insertOrder(String customerID){
      Order order=new Order();
      
      order.setCustomerID(customerID);
      
      //To disable Transparent persistence, use Customer.loadybID(customeriD)
      Customer customer=order.loadbyID(customerID); // now customer linked to order 
      //To disable Transparent persistence, can also use customer.setTrans(false) after order.orderbyID();
            
      customer.setTotalOrderCounts(customer.getTotalOrderCounts()+1);
      
      order.save();//customer also be saved, it's called Transparent Persistence
    }
    
    public static void main(String[] args) {
        Tester t=new Tester();
        t.insertOrder();
    }
} 

In above example, use a default global singleton datasource setting, source code ignored here (in fact I haven't start to code), but basic concept please see jBeanBox example#3.
```
 
Example 2 - Bean extends
```
 public static class Customer2 extends Customer{ 
   String customerID; 
   String customerName;  
   Integer totalOrderCounts;
   String address1;
   String address2;
   //getters & setters...
   {setDBField("address1","db_address1");   //address1 map to database field "db_address1" in table "customer"
    //setDBField("address2",Null);//Set Null means address2 do not map to any db field, it's default setting
    setDBField("customerName",Null);//Set Null means  customerID will not fetch from SQL.
  }
} 

Now Customer2.class has a new field "address1" map to database, and new field "address2" does not map to any database field.
Please note Customer2.class can be created dynamically in services layer, it's a child class of Customer but if run a SQL, it will fetch address1 from DB but no longer fetch customerName from DB.
```

