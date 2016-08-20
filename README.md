jSQLBox
====

**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

jSQLBox is a micro scale ORM Framework, simpler than Hibernate and EBean, based on same "box oriented programming"  
concept similar like jBeanBox and jWebBox project.  
*(jSQLBox hasn't start to code, looking partners to do this project)  

Feature of jSQLBox:  
1) Simple, very few source code(less than 2000 lines), No XML, very few Annotations(if have). Easy to learn and use.  
2) The Java-Based Bean configuration is simple and easy to use, can be automatically created by source code generation tool.  
3) The Java-Based Bean configuration can be dynamically created/modified at runtime(similar like jBeanBox project).  
4) jSQLBox is based on Active Record design, but, it supports transparent persistence benifits from it's dynamic Bean  
configuration.  
5) jSQLBox offers basic CURD methods but encourage mixed use them with raw SQL.  
6) Current version jSQLBox only support MySQL, but it's easy to immigrate to other databases, in future(if more people   
use it), it may support more databases.
7) jSQLBox only focus on database access, do not supply upper layer functions like pagination/Lucene/caches/...  

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
Example 1 - Basic CRUD 
```
public static class Order extends SQLBox{//Automatically created by source code generation tool
   String orderID;  
   String orderNO;  
   String customerID;  
   //getters & setters...
   {setTable("Order");   //if DB table name same as class name, default no need set it manually 
    setColumn("orderID","OrderID");//If DB column name same as field name, default no need set it manually 
  }
}

public static class Customer extends SQLBox{//Automatically created by source code generation tool
   String customerID; 
   String customerName;  
   Integer totalOrders;
   //getters & setters...
} 

public class Tester {
    public void insertOrder(String customerID){
    
    }
    
    public static void main(String[] args) {
        Tester t=new Tester();
        t.insertOrder();
    }
} 
```

