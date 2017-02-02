##jSQLBox (In Developing)
**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)  

jSQLBox is a micro scale persistence tool for Java8+.

Other persistence tools' problem:  
Hibernate: It's a ORM tool, too complicated, the XML or Annotation configurations are fixed, fields definations, "One to Many", "Many to One" relationships are fixed, hard to create/modify/re-use configuations at runtime. For example, can not temporally change configuration at runtime to exclude some fields to avoid lazy loading.  
Other DB tools: Have no a balance between powerfulness and simpliness.  

Feature of jSQLBox:  
1) Simple, very few source code, No XML, easy to learn and use.   
2) The Java-Based Bean configuration can be created/modified dynamically at runtime(similar like jBeanBox project).  
3) The configuration and Bean classes can be created by source code generation tool because it's a simple map of database.  
4) jSQLBox is based on Active Record design mode but entity class do not extends from any base class.  
5) There is no "many to one", "One to Many", "Cascade update" concept in jSQLBox, need maintence relationship by hand.  
6) jSQLBox support transparent persistence based on a transaction scope 1st level cache (supported by AOP tool like jBeanBox).  
7) jSQLBox offers basic CURD methods, but encourage mixed use with raw SQL, it's safe to mix use CRUD methods and sql.  
8) Current version jSQLBox only tested for MySQL and Oracle, in future version will support more databases. 
9) To make it simple, jSQLBox project only focus on basic CRUD method + SQL + transparent persistence, do not offer complex functions like paginnation, lazy load.  

How to use jSQLBox?  
To be released into Maven central repository

How to import jSQLBox project into Eclipse(for developer)?  
1)install JDK1.8+, Git bash, Maven3.3.9+, on command mode, run:
2)git clone https://github.com/drinkjava2/jSQLBox
3)cd jsqlbox
4)mvn eclipse:eclipse
5)mvn test
6)Open Eclipse, "import"->"Existing Projects into Workspace", select jsqlbox folder, there are 2 module projects "jsqlbox" and "jsqlbox-test" will be imported in Eclipse. 















 

