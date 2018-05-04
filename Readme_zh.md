<p align="center">
  <a href="https://github.com/drinkjava2/jSqlBox">
   <img alt="jsqlbox-logo" src="jsqlbox-logo.png">
  </a>
</p>

<p align="center">
  一盒在手，天下我有
</p>

<p align="center">
  <a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.drinkjava2%22%20AND%20a%3A%22jsqlbox%22">
    <img alt="maven" src="https://img.shields.io/maven-central/v/com.github.drinkjava2/jsqlbox.svg?style=flat-square">
  </a>

  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
</p>

# 简介 | Intro

jSqlBox是一个小而全的跨数据库、提供多种SQL写法、ActiveRecord、ORM查询、主从及分库分表、声明式事务等功能的数据库持久层工具。简单地说，是一个全栈数据库持久层工具。  
jSqlBox运行于Java6及以上。

# 优点 | Advantages

- **架构优良**：模块式架构，各个模块都可以脱离jSqlBox单独存在。
- **跨数据库**：基于jDialects，支持70多种数据库的分页、函数变换，是Hibernate之外少有的支持跨数据库DDL生成的工具。
- **尺寸小**： 仅有jSqlBoxt和DbUtils两个依赖包,合计约500k大小。
- **与DbUtils兼容**：继承于DbUtils, 原有基于DbUtils的项目可以无缝升级到jSqlBox。
- **多种SQL写法**：Inline方法、模板方法、DataMapper、ActiveRecord、链式写法、SqlMapper，NoSQL查询等。
- **多项技术创新**：Inline风格、多行文本支持、NoSQL查询、支持重构的SQL写法、ActiveRecord与SqlMapper合体等。
- **动态配置**：除了支持实体Bean注解式配置，jSqlBox还支持在运行期动态更改配置。
- **无会话设计**：无会话设计(Sessionless)，是一个真正轻量级的、全功能的持久层工具，也可以作为其它持久层工具的补丁来使用。
- **自带声明式事务**：基于独立小巧的声明式事务工具jTransactions。也支持配置成Spring事务。
- **学习曲线平滑**：模块化学习，了解了各个子模块，就掌握了jSqlBox，jSqlBox主体只有30多个类。

# 文档 | Documentation

[中文](../../wikis)  |  [English](https://github.com/drinkjava2/jSqlBox/wiki) | [JavaDoc](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jsqlbox%22)

# 应用示例 | Demo

[jBooox项目](https://gitee.com/drinkjava2/jBooox) 这是一个MVC Web项目，基于三个开源软件jBeanBox、jSqlBox、jWebBox。

[jSqlBox-in-Spring](../../tree/master/demo/jsqlbox-in-spring) 这是一个MVC Web项目，演示jSqlBox在Spring环境下的配置和使用。

[使用BeetlSql模板](../../tree/master/demo/jsqlbox-beetlsql) 演示如何在jSqlBox中开发和使用其它模板引擎如BeetlSQL。

[在MyBatis中使用](https://gitee.com/drinkjava2/jSqlBox/wikis/%E5%9C%A8MyBatis%E4%B8%AD%E4%BD%BF%E7%94%A8?parent=%E7%94%A8%E6%88%B7%E6%89%8B%E5%86%8C%2F%E6%BC%94%E7%A4%BA%E9%A1%B9%E7%9B%AE) 演示如何利用jSqlBox为MyBatis添加分页、DDL生成、ActiveRecord功能。

# 下载地址 | Download

[点此去下载](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jsqlbox%22)

```xml
<dependency>
   <groupId>com.github.drinkjava2</groupId>
   <artifactId>jsqlbox</artifactId>
   <version>1.0.7</version> <!--或最新版-->
</dependency> 
```

# 相关开源项目 | Other Projects

- [一个通用的数据库方言工具 jDialects](https://gitee.com/drinkjava2/jdialects)
- [一个独立的声明式事务工具 jTransactions](https://gitee.com/drinkjava2/jTransactions)
- [一个简单易用的IOC/AOP工具 jBeanBox](https://gitee.com/drinkjava2/jBeanBox)
- [一个500行源码的服务端布局工具 jWebBox](https://gitee.com/drinkjava2/jWebBox)

# 期望 | Futures

欢迎发issue提出更好的意见或提交PR，帮助完善jSqlBox

# 版权 | License

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

# 关注我 | About Me
[Github](https://github.com/drinkjava2)  
[码云](https://gitee.com/drinkjava2)  
