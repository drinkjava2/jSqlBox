## jBooox
=========

这是一个WebApp演示项目，主要架构基于 jbeanbox + jwebbox + jsqlbox，因为这三个开源项目都以Box结尾, 所以项目简称jBooox。  

jBooox项目的架构特点是各个模块之间完全独立，每个模块都可以单独抽取出来使用，在Maven中央库都可单独下载，这是它与jFinal之类提供一篮子整体解决方案工具的最大区别，jBooox不能保证每个模块都令人满意，所以不会将这些模块强行捆绑式打包提供。jBooox这种模块式架构设计与Spring系列工具类似，但是在易用性、源码的简洁性、包的大小、模块的独立性等方面是Spring系列无法比拟的。  

jBooox项目各模块简介:   
jSqlBox是一个基于DbUtils内核的持久层工具，支持多种SQL风格，设计目标是取代Hibernate、MyBatis之类持久层工具。  
jBeanBox是一个独立的IOC/AOP工具, 用来在项目中代替Spring-IOC/AOP内核。  
jWebBox是一个只有500行代码的后端页面布局工具，在本项目中客串充当Controller角色，身兼二职。  

另外jSqlBox模块本身由以下三个子模块组成:  
Apache-DbUtils是Apache旗下的持久层工具，对Jdbc进行了简单的封装。  
jTransactions是一个独立的声明式事务工具，自带一个微型的声明式事务实现TinyTx，并可配置成使用Spring-TX。  
jDialects是一个独立的数据库方言工具，支持70多种数据库方言的DDL生成、分页、函数变换。  

jBooox中的Dispatcher需要自已手工写，缺点是简陋，优点则是简单。jBooox项目不提供Bean字段自动填充、校验、i18n、表单内容保持、页面集成测试等功能，因为相对于MVC架构来说，这些杂项功能不是重点，而且可以用一些现成的第三方工具来实现。  

jBooox是个示例项目，没有太多文档，所有的设计意图体都现在示例代码中，请自行翻阅源代码或下载运行，体会它的架构设计。因为作者本人的水平就不高，各个子模块的源码都很短(或者说是简陋)，所以jBooox项目也适合初学者作为后端开发的入门材料来学习。  

### 备注:
在jSqlBox的demo目录下，还有另一个演示项目"jsqlbox-in-springs"，架构基于 jSqlBox + SpringTx + SpringIOC + SpringMVC,  演示了jSqlBox在传统Spring环境下的配置和使用，可以与本项目对比看一看。  