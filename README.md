<p align="center">
  <a href="https://github.com/drinkjava2/jSqlBox">
   <img alt="jsqlbox-logo" src="jsqlbox-logo.png">
  </a>
</p>

<p align="center">
  科技以懒人为本 
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

jSqlBox是一个支持动态配置、多种SQL写法、跨数据库的Java持久层工具。运行于Java6及以上。

# 优点 | Advantages

- **架构优良**：模块式架构，各个模块都可以脱离jSqlBox单独存在。
- **跨数据库**：基于jDialects，支持70多种数据库的分页、函数变换，是Hibernate之外少有的支持DDL生成的持久层工具。
- **尺寸小**：所有依赖包合计约500k大小。
- **与DbUtils兼容**：继承于DbUtils, 原有基于DbUtils的项目可以无缝移植到jSqlBox。
- **多种SQL写法**：Inline方法、模板方法、DataMapper、ActiveRecord、链式写法、SqlMapper，NoSQL查询等。
- **多项技术创新**：Inline写法、多行文本支持、NoSQL查询、ActiveRecord与SqlMapper合体等。
- **动态配置**：除了支持实体Bean注解式配置，jSqlBox还支持在运行期动态更改配置。
- **无会话设计**：无会话设计(Sessionless)，是真正的轻量级工具，可以随用随弃。
- **自带声明式事务**：基于独立的声明式事务工具jTransactions,并可配置成Spring事务。
- **学习曲线平滑**：模块化学习，了解了各个子模块，就掌握了jSqlBox，jSqlBox主体只有30多个类。

# 文档 | Documentation

[中文](../../wiki)  |  [English](../../wiki)

[JavaDoc](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jsqlbox%22)

# 应用示例 | Demo

[jBooox项目](../../tree/master/demo/jbooox)

[jSqlBox-in-Spring](../../tree/master/demo/jsqlbox-in-spring)

[使用BeetlSql模板](../../tree/master/demo/jsqlbox-beetlsql)

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

- [一个通用的数据库方言工具 jDialects](https://gitee.com/drinkjava2/jDialects)
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
