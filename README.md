<p align="left">
  <a href="README_ENG.md">
	English instructions please see "README_ENG.md"
  </a>
</p>

<p align="center">
  <a href="https://github.com/drinkjava2/jSqlBox">
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
jSqlBox是一个基于DbUtils内核开发的全功能开源Java数据库持久层工具，在架构合理性、功能完备性、易用性等方面都不输于其它持久层工具。  

## 与其它持久层工具对比
请见[与其它DAO工具对比](https://gitee.com/drinkjava2/jSqlBox/wikis/pages?title=%E9%99%84%E5%BD%952%EF%BC%9ADAO%E5%B7%A5%E5%85%B7%E5%AF%B9%E6%AF%94&parent=%E7%94%A8%E6%88%B7%E6%89%8B%E5%86%8C)  
可以对jSqlBox的功能与特点有一个大概的了解。  

## 架构 | Architecture  
![image](arch.png)  

## 优点 | Advantages

- **架构合理**：模块式架构，各个模块都可以脱离jSqlBox单独存在。  
- **跨数据库**：基于jDialects，支持80多种数据库的分页、DDl脚本生成、实体源码生成、函数变换、主键生成等功能。  
- **与DbUtils兼容**：内核基于DbUtils, 原有基于DbUtils的旧项目可以无缝升级到jSqlBox。  
- **多种SQL写法**：Inline方法、模板方法、DataMapper、ActiveRecord、链式写法等。  
- **多项技术创新**：Inline风格、多行文本支持、实体越级关联查询、树结构查询等。  
- **动态配置**：除了支持实体Bean注解式配置，jSqlBox还支持在运行期动态更改配置。  
- **无会话设计**：无会话设计(Sessionless)，是一个真正轻量级的、全功能的持久层工具，也可以作为其它持久层工具的补丁来使用。  
- **自带声明式事务**：内置微型声明式事务工具jTransactions。也支持配置成Spring事务。  
- **主从、分库分表**：无需引入第三方工具，jSqlBox本身就具备主从、分库分表功能。  
- **学习曲线平滑**：模块化学习，了解了各个子模块，就掌握了jSqlBox，jSqlBox模块部分只有20多个类。  

## 文档 | Documentation

[中文](https://gitee.com/drinkjava2/jSqlBox/wikis/pages)  |  [English](https://github.com/drinkjava2/jSqlBox/wiki) | [JavaDoc](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jsqlbox%22)

## 配置 | Configuration
在pom.xml中加入：  
```xml
<dependency>
   <groupId>com.github.drinkjava2</groupId>
   <artifactId>jsqlbox</artifactId> <!--用于Java6、7环境-->
   <version>2.0.4</version> <!--或最新版-->
</dependency> 
```
或
```xml
<dependency>
   <groupId>com.github.drinkjava2</groupId>
   <artifactId>jsqlbox-java8</artifactId> <!--用于Java8及以上环境-->
   <version>2.0.4</version> <!--或最新版-->
</dependency> 
```

## 入门 | First Example
以下示例演示了jSqlBox的基本配置和使用:
```
public class HelloWorld implements ActiveEntity<HelloWorld> {
    private String name;
    public String getName() {return name; }
    public void setName(String name) {this.name = name; }

    public static void main(String[] args) {
        DataSource ds = JdbcConnectionPool
                .create("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
        SqlBoxContext ctx = new SqlBoxContext(ds);
        SqlBoxContext.setGlobalSqlBoxContext(ctx);
        String[] ddls = ctx.toCreateDDL(HelloWorld.class);
        for (String ddl : ddls)
               ctx.nExecute(ddl);

        new HelloWorld().putField("name", "Hello jSqlBox").insert();
        System.out.println(ctx.iQueryForString("select name from HelloWorld"));
    }
}
```

## 范例 | Demo

* [jBooox](https://gitee.com/drinkjava2/jBooox) 这是一个微型MVC Web项目，基于三个开源软件jBeanBox、jSqlBox、jWebBox的整合，需发布到Tomcat下运行。
* [jsqlbox-in-actframework](../../tree/master/demo/jsqlbox-in-actframework) 演示jSqlBox与ActFramework框架的整合，分别展示利用jBeanBox和Guice来实现声明式事务。
* [jsqlbox-in-jfinal](../../tree/master/demo/jsqlbox-in-jfinal) 演示jSqlBox与jFinal的整合，用jSqlBox替换掉jFinal自带的DAO工具。
* [jSqlBox-in-Spring](../../tree/master/demo/jsqlbox-in-spring) 这是一个MVC Web项目，演示jSqlBox在Spring+Tomcat环境下的配置和使用, IOC、AOP和声明式事务均使用Spring的。
* [jsqlbox-in-springboot](../../tree/master/demo/jsqlbox-in-springboot) 演示jSqlBox在SpringBoot环境下的配置和使用。  
* [jsqlbox-in-springboot-mybatis](../../tree/master/demo/jsqlbox-in-springboot-mybatis) 演示在SpringBoot环境下jSqlBox和MyBatis的混合使用。
* [jsqlbox-java8-demo](../../tree/master/demo/jsqlbox-java8-demo) 主要演示jSqlBox-Java8版的两个特点：实体类只需要声明接口、利用Lambda来写SQL。
* [jsqlbox-xa-atomikos](../../tree/master/demo/jsqlbox-xa-atomikos) 一个jSqlBox在分布式事务环境下分库分表操作的演示。  
* [jsqlbox-beetlsql](../../tree/master/demo/jsqlbox-beetlsql) 演示如何在jSqlBox中开发和使用其它模板引擎如BeetlSQL。
 
 
## 作者其它开源项目 | Other Projects

- [数据库方言工具 jDialects](https://gitee.com/drinkjava2/jdialects)
- [一个独立的声明式事务工具 jTransactions](https://gitee.com/drinkjava2/jTransactions)
- [一个微型IOC/AOP工具 jBeanBox](https://gitee.com/drinkjava2/jBeanBox)
- [一个服务端布局工具 jWebBox](https://gitee.com/drinkjava2/jWebBox)
- [人工生命实验项目 frog](https://gitee.com/drinkjava2/frog)

## 期望 | Futures

欢迎发issue提出更好的意见或提交PR，帮助完善jSqlBox

## 版权 | License

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## 关注我 | About Me
[Github](https://github.com/drinkjava2)  
[码云](https://gitee.com/drinkjava2)  
