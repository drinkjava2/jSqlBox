<p align="left">
  <a href="README_ENG.md">
	English instructions please see "README_ENG.md"
  </a>
</p>

<p align="center">
  <a href="https://github.com/drinkjava2/jsqlbox">
   <img alt="jsqlbox-logo" src="jsqlbox-logo.png">
  </a>
</p>

<p align="center"> 
  基于DbUtils内核的全功能数据库持久层工具
</p>

<p align="center">
  <a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.drinkjava2%22%20AND%20a%3A%22jsqlbox%22">
    <img alt="maven" src="https://img.shields.io/maven-central/v/com.github.drinkjava2/jsqlbox.svg?style=flat-square">
  </a>

  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
</p>

## 简介 | Intro
jSqlBox是一个全功能开源Java数据库持久层工具，在架构、功能、易用性等方面都不输于其它持久层工具，可以说，只要是与数据库操作相关的功能，jSqlBox都已具备，如DDL操作、分页、分库分表、声明式事务、分布式事务、关联映射查询等，所有这些功能都包含在一个1M大小的jar包中，不依赖任何第三方库。 

jSqlBox的最大特点是拥抱SQL，提倡直接在Java里写出可维护的SQL，它首创了参数内嵌式SQL写法。  
比如说下面这种传统SQL写法是不可维护的，当要添加或删除字段时会很麻烦，因为要找到列名和参数之间的对应关系是很花时间的:
```
insert into tb_price_setting (id,code,adult_price,child_price,total_price,adult_note,child_note,currency,type,time_zone,status,include_tax,adult_discount,child_discount,total_discount,created_at,updated_at,) values(1200, "BJD837434", 50, 30, 80, "15以上全价", "8-15半价", "USD, 8, "UTC", "A", 3.03, 0, 0, 0, "2019-09-17 04:07:55", "2020-03-10 22:43:00");
```
而采用jSqlBox，写法如下，这种写法在不降低原生SQL的可读性的前提下，极大地提高了原生SQL的可维护性:  
```
 DB.exe("insert into tb_price_setting (", //
	"id,", par(1200), //
	"code,", par("BJD837434"), //
	"adult_price,", par(50), //
	"child_price,", par(30), //
	"total_price,", par(80), //
	"adult_note,", par("15以上全价"), //
	"child_note,", par("8-15半价"), //
	"currency,", par("USD"), //
	"type,", par(8), //
	"time_zone,", par("UTC"), //
	"status,", par("A"), //
	"include_tax,", par(3.03), //
	"adult_discount,", par(0), //
	"child_discount,", par(0), //
	"total_discount,", par(0), //
	"created_at,", par("2019-09-17 04:07:55"), //
	"updated_at,", par("2020-03-10 22:43:00"), //
	")", valuesQuestions());
``` 

再进一步，利用jSqlBox的根据数据库生成Q类插件(详见用户手册"支持重构的SQL"一节)，还可以写出可重构的SQL来，进一步提高原生SQL的可维护性:
```
 QTbPriceSetting p=QTbPriceSetting.instance;
 DB.exe("insert into ",p," (", //
	p.id, ",", par(1200), //
	p.code, ",", par("BJD837434"), //
	p.adult_price, ",", par(50), //
	p.child_price, ",", par(30), //
	p.total_price, ",", par(80), //
	p.adult_note, ",", par("15以上全价"), //
	p.child_note, ",", par("8-15半价"), //
	p.currency, ",", par("USD"), //
	p.type, ",", par(8), //
	p.time_zone, ",", par("UTC"), //
	p.status, ",", par("A"), //
	p.include_tax, ",", par(3.03), //
	p.adult_discount, ",", par(0), //
	p.child_discount, ",", par(0), //
	p.total_discount, ",", par(0), //
	p.created_at, ",", par("2019-09-17 04:07:55"), //
	p.updated_at, par("2020-03-10 22:43:00"), //
	")", valuesQuestions());
```
使用jSqlBox并不意味要使用它的所有功能，对于小项目来说，上述的SQL式写法就足够支撑普通CRUD开发了，不一定要引入复杂的ORM。  

## jSqlBox与其它持久层工具对比
请见[与其它DAO工具对比](https://gitee.com/drinkjava2/jsqlbox/wikis/pages?sort_id=1010925&doc_id=92178), 可以对jSqlBox的功能与特点有一个大概的了解。  

## 架构 | Architecture  
![image](arch.png)  

## 主要优点 | Advantages

- **不依赖任何第三方库**：jSqlBox只有一个约1M大小的单个Jar包，不依赖任何第三方库。  
- **架构合理**：模块式架构，各个子模块(jBeanBox,jDbPro,jDialects,jTransaction)都可以脱离jSqlBox单独存在。  
- **跨数据库**：基于jDialects模块，支持80多种数据库的分页、DDl脚本生成、从数据库生成实体源码、函数变换、主键生成等功能。  
- **与DbUtils兼容**：内核基于DbUtils, 原有基于DbUtils的旧项目可以无缝升级到jSqlBox。  
- **多种SQL写法**：Inline方法、模板方法、DataMapper、ActiveRecord、链式写法、缓存翻译等。  
- **多项技术创新**：参数内嵌式SQL写法、多行文本支持、实体关联查询、树结构查询等。  
- **动态配置**：除了支持实体Bean注解式配置，jSqlBox还支持在运行期动态更改配置。  
- **无会话设计**：无会话设计(Sessionless)，是一个真正轻量级的、全功能的持久层工具，也可以作为其它持久层工具的补丁来使用。  
- **主从、分库分表**：无需引入第三方工具，jSqlBox本身就具备主从、分库分表功能。  
- **自带声明式事务**：内置微型IOC/AOP工具，不依赖Spring就可提供声明式事务。  
- **自带分布式事务**：无需引入第三方工具，jSqlBox本身就提供无侵入的分布式事务功能，和Seata项目类似，它可以自动生成回滚SQL，但源码远比Seata简洁。
- **学习曲线平滑**：模块化学习，了解了各个子模块，就掌握了jSqlBox，jSqlBox主体模块源码只有40多个类。  
- **兼容主要JPA注解**：jSqlBox可以识别并兼容JPA实体类的主要注解(15个)。  

 
## 文档 | Documentation

[中文](https://gitee.com/drinkjava2/jsqlbox/wikis/pages) | [English](https://github.com/drinkjava2/jsqlbox/wiki) | [JavaDoc](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jsqlbox%22) | [PDF](https://gitee.com/drinkjava2/jsqlbox/wikis/pages/export?type=pdf&info_id=92178)

## 配置 | Configuration
在pom.xml中加入以下依赖即可：  
```xml
<dependency>
   <groupId>com.github.drinkjava2</groupId>
   <artifactId>jsqlbox</artifactId>  
   <version>5.0.9.jre8</version> <!-- 或最新版 -->
</dependency> 
```
jSqlBox没有用到第三方依赖，对于需要学习或更改它的源码的场合，甚至可以直接将jSqlBox的源码拷到项目目录里就可以使用它了。  

## 入门 | First Example
以下示例演示了jSqlBox的基本配置和使用:
```
pom.xml中引入：
    <dependency>
      <groupId>com.github.drinkjava2</groupId>
       <artifactId>jsqlbox</artifactId> 
       <version>5.0.9.jre8</version> <!-- 或最新版 -->
    </dependency>

    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId> <!--H2内存数据库-->
      <version>1.3.176</version>
    </dependency>

在IDE里输入以下源程序：

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
     DataSource ds = JdbcConnectionPool  //这个示例使用h2内存数据库
                     .create("jdbc:h2:mem:demo;MODE=MYSQL;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
     DbContext ctx = new DbContext(ds);
     ctx.setAllowShowSQL(true);  //开启SQL日志输出
     DbContext.setGlobalDbContext(ctx);  //设定全局上下文
     ctx.quiteExecute(ctx.toDropAndCreateDDL(HelloWorld.class));  //生成DDL,建数据库表
     ctx.tx(() -> {  //开启事务
        HelloWorld h = new HelloWorld().setName("Foo").insert().putField("name", "Hello jSqlBox").update();
        System.out.println(DB.qryString("select name from HelloWorld where name like", que("H%"),
					" or name=", que("1"), " or name =", que("2")));
        h.delete();  //删除实体
     });
     ctx.executeDDL(ctx.toDropDDL(HelloWorld.class)); //删除数据库表
   }
}
```
上面这个演示包括了根据实体类生成DDL并执行、插入实体到数据库、执行更新、查询出结果、即打印出"Hello jSqlBox"、删除实体、删除数据库。
示例中的实体类只需要声明接口(限Java8版)。查询语句使用了jSqlBox独创的参数内嵌式SQL写法，可以自由拼接复杂的条件SQL，也不用考虑参数和问号对齐的问题了。  
因为开启了日志输出，可以看到命令行打印出的SQL执行日志:
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
以上是jSqlBox最简短的入门介绍，详细的使用说明请参见[用户手册](https://gitee.com/drinkjava2/jsqlbox/wikis/pages)。  

## 范例 | Demo
以下范例位于jSqlBox的demo目录下：  
* [jbooox](../../tree/master/demo/jsqlbox-jbooox) 这是一个微型Web演示项目，基于三个开源项目jBeanBox、jSqlBox、jWebBox的整合。
* [jsqlbox-actframework](../../tree/master/demo/jsqlbox-actframework) 演示jSqlBox与ActFramework框架的整合，分别展示利用jBeanBox和Guice来实现声明式事务。
* [jsqlbox-jfinal](../../tree/master/demo/jsqlbox-jfinal) 演示jSqlBox与jFinal的整合，用jSqlBox替换掉jFinal自带的DAO工具。
* [jSqlBox-Spring](../../tree/master/demo/jsqlbox-spring) 演示jSqlBox在Spring+Tomcat环境下的配置和使用
* [jsqlbox-springboot](../../tree/master/demo/jsqlbox-springboot) 演示jSqlBox在SpringBoot环境下的配置和使用。  
* [jsqlbox-mybatis](../../tree/master/demo/jsqlbox-mybatis) 演示在SpringBoot环境下jSqlBox和MyBatis的混合使用。
* [jsqlbox-beetl](../../tree/master/demo/jsqlbox-beetl) 演示如何在jSqlBox中自定义SQL模板引擎，此演示使用Beetl作为SQL模板。
 
## 相关开源项目 | Related Projects
- [数据库方言工具 jDialects](https://gitee.com/drinkjava2/jdialects)
- [独立的声明式事务工具 jTransactions](https://gitee.com/drinkjava2/jTransactions)
- [微型IOC/AOP工具 jBeanBox](https://gitee.com/drinkjava2/jBeanBox)
- [前端直接写SQL和业务逻辑 GoSqlGo](https://gitee.com/drinkjava2/gosqlgo)

## 期望 | Futures

欢迎发issue提出更好的意见或提交PR，帮助完善项目

## 版权 | License

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## 关注我 | About Me
[码云](https://gitee.com/drinkjava2)  
[Github](https://github.com/drinkjava2)  
