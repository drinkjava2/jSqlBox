<p align="center">
  <a href="https://github.com/drinkjava2/jSqlBox">
   <img alt="jsqlbox-logo" src="jsqlbox-logo.png">
  </a>
</p>

<p align="center">
  The brand new Java persistence tool 全新的Java持久层工具
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

jSqlBox is a Java persistence tool, runs on JDK6 or above.

# Advantages
- **Excellent architecture**: Modular architecture, each module can be separated from jSqlBox alone.
- **Cross-database**: Based on jDialects, support pagination、functions translating, and DDL output for more than 70 kinds of databases.
- **Small size**: All dependent packages total about 500k.
- **Compatible with DbUtils**: Inherited from DbUtils, the original DbUtils-based project can be seamlessly upgrade to jSqlBox.
- **Multiple SQL methods**: Inline method, template method, DataMapper, ActiveRecord, chaining, SqlMapper, NoSQL query, etc.
- **A number of technical innovations**: Inline writing, multi-line text support, NoSQL query, ActiveRecord, and SqlMapper fit.
- **Dynamic Configuration**: In addition to support annotation configuration, jSqlBox also supports dynamic configuration changes at runtime.
- **No session design**: Sessionless, a truly lightweight tool
- **Comes with declarative transaction**: based on the independent declarative transaction tool jTransactions, and can be configured as a Spring transaction.
- **Smooth learning curve**: Modular learning, understanding of the various sub-modules, to master the jSqlBox, jSqlBox main body only more ~30 classes.

# Documentation

[Chinese中文用户手册](https://gitee.com/drinkjava2/jSqlBox/wikis/%E7%AE%80%E4%BB%8B)  |  [English User Manual](../../wiki)  | [JavaDoc](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jsqlbox%22)

# Demo

[jBooox MVC Project](../../tree/master/demo/jbooox)

[jSqlBox-in-Spring](../../tree/master/demo/jsqlbox-in-spring)

[Use BeetlSql](../../tree/master/demo/jsqlbox-beetlsql)

[Use in MyBatis](https://github.com/drinkjava2/jDialects/blob/master/demo/demo-mybatis/src/test/java/com/github/drinkjava2/demo/TestDemo.java)

# Download

[Maven site](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jsqlbox%22)

```xml
<dependency>
   <groupId>com.github.drinkjava2</groupId>
   <artifactId>jsqlbox</artifactId>
   <version>1.0.7</version> <!--Or latest version-->
</dependency> 
```

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
