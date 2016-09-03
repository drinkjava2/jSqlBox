#jSQLBox
====

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

jSQLBox is a micro scale ORM Framework, simpler than Hibernate but powerful than JDBC/JDBCTemplate/MyBatis/DButils.
#(jSQLBox project is in pending, currently only made some concept design)  

Other ORM frameworks' problem:
The configurations of entity beans are fixed, fields definations, "One to Many", "Many to One" relationships are fixed, hard to create/modify configuations at runtime.  

Feature of jSQLBox:  
1) Simple, very few source code, No XML, Easy to learn and use.   
2) The Java-Based Bean configuration can be created/modified dynamically at runtime(similar like jBeanBox project). 
3) The Java-Based Bean configuration can be created by source code generation tool because it's a simple map of DB structure; 
4) There is no "many to one", "One to Many", "Cascade update" concept in jSQLBox, replaced by simple "hook" concept. hook method is manually called by hand, this give more flexibility on business logic. And a default hook setting can be set at beginning if make sure the business logic is fixed.
5) jSQLBox is based on Active Record design, but try support transparent persistence by hook beans to build an "Object graph".
6) jSQLBox offers basic CURD methods, but still encourage mixed use with raw SQL, it's SQL safe. Not like Hibernate mixed use  
raw SQL may damage first level cache.
7) Current version jSQLBox only support MySQL, in future version may support other databases depends how many develpers involved.
8) To make it simple (The more you spent to learn = the harder to maintenance project), jSQLBox only focus on basic O-R Mapping, do not offer complicated functions like pagination/Lucene/2nd caches.

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
public static class Order extends SQLBox{//Automatically created by code generation tool
   String orderID;  
   String orderNO;  
   String customerID;  
   //getters & setters...
   { setDBTable("Order");   //if DB table name same as class name, no need write this line
     setDBField("orderID","DBOrderID");//If DB column name same as field name, no need write this line
   }
}

public static class Customer extends SQLBox{//Automatically created by code generation tool
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
      customer.setOrderCounts('select count(*)+1 from "+ORDER()+" where " + OrderID() + "=" +order.getID()));
      order.save();//customer also be saved, it's called Transparent Persistence
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
   {setDBField("newAddress","New_Address");   //address1 map to database field "db_address1" 
    setDBField("customerName",Null);//Set Null means customerName will not be fetched in SQL.
  }
} 
```
Now Customer2.class has a new field "newAddress" map to database field New_Address, Customer2.class can be created dynamically in services layer, it's a child class of Customer, and in loadByID method, it will fetch New_Address from DB but no longer fetch customerName.
 
