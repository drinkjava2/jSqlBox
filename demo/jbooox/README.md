## jBooox
=========

这是一个WebApp演示项目，主要架构基于 jbeanbox + jwebbox + jsqlbox，因为有3个Box, 所以简称jBooox。

jBooox项目的架构特点是各个模块之间完全独立，每个模块都可以单独抽取出来使用，在Maven中央库都有下载，这是它与jFinal之类提供一篮子整体解决方案工具的最大区别，jBooox不保证每个模块都是讨人喜欢的，所以不会将它们打包整体提供。jBooox这种模块式架构设计与Spring系列工具类似，但是在易用性和简洁性上要优于Spring。Spring发展到目前，已经变得非常臃肿了,例如它还要支持过时的XML配置。 

jBooox项目各模块简介:   
jsqlbox是一个基于DbUtils和jDialects内核的持久层工具, 它的设计目标是代替Hibernate、MyBatis、JdbcTempalte等工具。  
jbeanbox是一个独立的IOC/AOP工具, 它的设计目标是替换Spring-IOC/AOP内核。  
jWebBox是一个独立的服务端页面布局兼MVC工具，它的设计目标是替换Apache-Tiles，在本项目中客串MVC中的Controller。  

另外jsqlbox本身还依赖于两个子模块，分别为jTransactions和jDialects:  
jTransactions是一个单独的声明式事务工具，它自带一个微型的声明式事务，并可配置成使用Spring-TX。  
jDialects是一个单独的数据库方言工具，支持70多种数据库方言的DDL生成、分页、函数变换。 

jBooox的MVC部分由jWebBox来实现，它本来是一个只有500行源码的服务端布局工具，不具备Bean字段自动填充、校验、i18n、表单的输入内容保持等功能，但相比与架构来说，这些不是重点，而且可以很容易找到第三方工具来实现，所以在jBooox项目中不提供。

jBooox包含了Controller和Service的单元测试演示，没有提供集成测试的演示，因为集成测试可以找一些现成的第三方工具来做，所以也不提供。

###备注:
在jSqlBox的demo目录下，还有另一个演示项目"jsqlbox-in-springs"，架构基于 jSqlBox + SpringTx + SpringIOC + SpringMVC,  有兴趣的同学可以将这两个项目对比看看。

 

 