## jBooox
=========

这是一个WebApp演示项目，主要架构基于 jbeanbox + jwebbox + jsqlbox，因为这三个开源项目都以Box结尾, 所以本项目简称jBooox。

jBooox项目的架构特点是各个模块之间完全独立，每个模块都可以单独抽取出来使用，在Maven中央库都可以单独下载，这是它与jFinal之类提供一篮子整体解决方案工具的最大区别，jBooox不保证每个模块都是令人满意的，也不保证没有人会只喜欢其中的一个模块，所以不将这些模块打包整体提供。jBooox这种模块式架构设计与Spring系列工具类似，但是在易用性和源码的简洁性上作了很多改进。

jBooox项目各模块简介:   
jSqlBox是一个基于DbUtils和jDialects内核的持久层工具, 它的设计目标是代替Hibernate、MyBatis、JdbcTempalte等持久层工具。  
jBeanBox是一个独立的IOC/AOP工具, 它的设计目标是代替Spring-IOC内核。  
jWebBox是一个独立的服务端页面布局工具，它的设计目标是代替Apache-Tiles，在本项目中客串充当Controller层。  

另外jSqlBox模块本身依赖于三个子模块:
Apache-DbUtils是Apache旗下的持久层工具，对Jdbc进行了简单的封装。
jTransactions是一个独立的声明式事务工具，自带一个微型的声明式事务实现TinyTx，并可配置成使用Spring-TX。  
jDialects是一个独立的数据库方言工具，支持70多种数据库方言的DDL生成、分页、函数变换。 

jBooox中的Dispatcher需要自已手工写, Controller层由jWebBox来充当，它是一个源码只有500行的小工具，兼具布局和Controller功能。jBooox项目不提供Bean字段自动填充、校验、i18n、表单内容保持、页面集成测试等功能，但相对于架构来说，这些都不是重点，可以找到第三方工具来实现，所以在jBooox项目中不提供这些杂项功能的演示。


###备注:
在jSqlBox的demo目录下，还有另一个演示项目"jsqlbox-in-springs"，架构基于 jSqlBox + SpringTx + SpringIOC + SpringMVC,  演示了jSqlBox在传统Spring环境下的配置和使用。

 

 