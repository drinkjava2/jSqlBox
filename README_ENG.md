<p align="center">
  <a href="https://github.com/drinkjava2/jSqlBox">
   <img alt="jsqlbox-logo" src="jsqlbox-logo.png">
  </a>
</p>

<p align="center">
  Java persistence tool Java
</p>

<p align="center">
  <a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.drinkjava2%22%20AND%20a%3A%22jsqlbox%22">
    <img alt="maven" src="https://img.shields.io/maven-central/v/com.github.drinkjava2/jsqlbox.svg?style=flat-square">
  </a>

  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
</p>

# Intro
jSqlBox is a DAO tool based on Apache-commons-DbUtils core. 

# Advantages
- **Excellent architecture**: Modular architecture, each module can be separated from jSqlBox alone.
- **Cross-database**: Based on jDialects, support pagination、functions translating, and DDL output for more than 70 kinds of databases.
- **Small size**: All dependent packages total about 500k.
- **Compatible with DbUtils**: Inherited from DbUtils, the original DbUtils-based project can be seamlessly upgrade to jSqlBox.
- **Multiple SQL methods**: Inline method, template method, DataMapper, ActiveRecord, chaining, query, etc.
- **A number of technical innovations**: Inline writing, multi-line text support, NoSQL query, ActiveRecord, and SqlMapper fit.
- **Dynamic Configuration**: In addition to support annotation configuration, jSqlBox also supports dynamic configuration changes at runtime.
- **No session design**: Sessionless, a truly lightweight tool
- **Comes with declarative transaction**: based on the independent declarative transaction tool jTransactions, and can be configured as a Spring transaction.
- **Smooth learning curve**: Modular learning, understanding of the various sub-modules, to master the jSqlBox, jSqlBox main body only more ~30 classes.

# Documentation

[Chinese中文](https://gitee.com/drinkjava2/jSqlBox/wikis/%E7%AE%80%E4%BB%8B)  |  [English User Manual](https://github.com/drinkjava2/jSqlBox/wiki)  | [JavaDoc](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jsqlbox%22)

# Download

[Maven site](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jsqlbox%22)

```xml
<dependency>
   <groupId>com.github.drinkjava2</groupId>
   <artifactId>jsqlbox</artifactId>
   <version>5.0.11.jre8</version> <!--Or newest version-->
</dependency> 
```

# First Example  
```
pom.xml：
    <dependency>
      <groupId>com.github.drinkjava2</groupId>
       <artifactId>jsqlbox</artifactId> 
       <version>5.0.12.jre8</version> <!-- Java8 -->
    </dependency>

    <dependency>
      <groupId>com.h2database</groupId> <!--H2 database->
      <artifactId>h2</artifactId>  
      <version>1.3.176</version>
    </dependency>

And create below java file in Eclipse or Idea:

import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import static com.github.drinkjava2.jsqlbox.DB.*;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveEntity;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;

public class HelloWorld implements ActiveEntity<HelloWorld> {
  @Id
  @UUID25
  private String id;
  private String name;
  public String getId() {return id;}
  public void setId(String id) {this.id = id;}
  public String getName() {return name;}
  public HelloWorld setName(String name) {this.name = name;return this;}

    public static void main(String[] args) {
     DataSource ds = JdbcConnectionPool  
                     .create("jdbc:h2:mem:demo;MODE=MYSQL;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
     DbContext ctx = new DbContext(ds);
     ctx.setAllowShowSQL(true); 
     DbContext.setGlobalDbContext(ctx);  
     ctx.quiteExecute(ctx.toDropAndCreateDDL(HelloWorld.class));  
     ctx.tx(() -> { 
        HelloWorld h = new HelloWorld().setName("Foo").insert().putField("name", "Hello jSqlBox").update();
        System.out.println(DB.qryString("select name from HelloWorld where name like", que("H%"),
					" or name=", que("1"), " or name =", que("2")));
        h.delete(); 
     });
     ctx.executeDDL(ctx.toDropDDL(HelloWorld.class)); 
   }
}
```
Below is the log output:
```
SQL: drop table HelloWorld if exists
PAR: []
SQL: create table HelloWorld ( id varchar(250),name varchar(250), primary key (id))
PAR: []
SQL: insert into HelloWorld (name, id)  values(?,?)
PAR: [Foo, emeai4bfdsciufuuteb9a7nmo]
SQL: update HelloWorld set name=?  where id=?
PAR: [Hello jSqlBox, emeai4bfdsciufuuteb9a7nmo]
SQL: select name from HelloWorld where name like? or name=? or name =?
PAR: [H%, 1, 2]
SQL: delete from HelloWorld where id=? 
PAR: [emeai4bfdsciufuuteb9a7nmo]
SQL: drop table HelloWorld if exists
PAR: []
```
More documents please see wiki. 

# Demo

* [jBooox](https://gitee.com/drinkjava2/jBooox) A micro mvc web demo based on jBeanBox+jSqlBox+jWebBox.
* [jsqlbox-beetl](../../tree/master/demo/jsqlbox-beetlsql) A demo shows how to use Beetl as SqlTemplateEngine.
* [jsqlbox-actframework](../../tree/master/demo/jsqlbox-actframework) Shows how to use jSqlBox in ActFramework，and use TinyTx+Guice's AOP to achieve declarative transaction.
* [jsqlbox-jfinal](../../tree/master/demo/jsqlbox-jfinal) Shows use jSqlBox in jFinal.
* [jSqlBox-Spring](../../tree/master/demo/jsqlbox-spring) Shows use jSqlBox in Spring+Tomcat.
* [jsqlbox-springboot](../../tree/master/demo/jsqlbox-springboot) Shows use jSqlBox in SpringBoot.
* [jsqlbox-springboot-mybatis](../../tree/master/demo/jsqlbox-springboot-mybatis) Shows mixed use jSqlBox and MyBatis in SpringBoot.
* [jsqlbox-java](../../tree/master/demo/jsqlbox-java8) Shows jSqlBox-Java8 version usage and use Lambda to write SQL。
* [jsqlbox-atomikos](../../tree/master/demo/jsqlbox-atomikos) Shows sharding feature in jSqlBox when use XA transaction be implemented by Atomikos 。
 
# Related Other Projects

- [jDialects, a database dialect tool](https://github.com/drinkjava2/jDialects)
- [jTransactions, a declarative transaction tool](https://github.com/drinkjava2/jTransactions)
- [jBeanBox, a simple IOC/AOP tool](https://github.com/drinkjava2/jBeanBox)
- [jWebBox, a JSP/Freemaker layout tool](https://github.com/drinkjava2/jWebBox)

# Futures

Welcome post issue or submit PR, to help improve jSqlBox

# License

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

# About Me
[Github](https://github.com/drinkjava2)  
[码云](https://gitee.com/drinkjava2)  
