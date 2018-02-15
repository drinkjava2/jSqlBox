## jBooox
=========

这是一个WebApp演示项目，主要架构基于 jbeanbox + jwebbox + jsqlbox，因为这三个开源项目都以Box结尾, 所以项目简称jBooox。  

jBooox项目的架构特点是各个模块之间完全独立，每个模块都可以单独抽取出来使用，在Maven中央库都有发布。这是它与jFinal之类提供一篮子整体解决方案工具的最大区别，jBooox不保证每个模块都令人满意，所以不会将这些模块强行捆绑打包提供。jBooox这种模块式架构设计与Spring系列工具类似，但是代码更精简，架构更松散。包括IOC内核、声明式事务这些模块都不再是主角，而是随时可以用第三方工具来替换掉。

jBooox项目各模块简介:   
jSqlBox是一个持久层工具，支持多种SQL风格，设计目标是取代Hibernate、MyBatis之类持久层工具。  
jBeanBox是一个独立的IOC/AOP工具, 目标是用来在项目中代替Spring-IOC内核。  
jWebBox是一个只有500行代码的后端页面布局工具，在本项目中客串充当Controller角色，身兼二职。  

另外jSqlBox模块本身由以下三个子模块组成:  
Apache-DbUtils是Apache旗下的持久层工具，对Jdbc进行了简单的封装, jSqlBox利用它作为内核，节省了一些底层开发工作量。  
jTransactions是一个独立的声明式事务工具，自带一个微型的声明式事务实现TinyTx，并可配置成使用Spring事务。  
jDialects是一个独立的数据库方言工具，支持70多种数据库方言的DDL生成、分页、函数变换。  

jBooox中的Dispatcher需要程序员自已写，虽然麻烦一点，但是有了更多的发挥空间。jBooox项目不提供Bean字段填充、表单内容保持、校验、i18n、页面集成测试等功能，因为相对于MVC架构来说，这些杂项功能其实不是重点，而且可以找到一些现成的第三方工具来实现。  

Spring系列的Java配置方式，包括IOC、Controller等通常采用Java方法作为配置，而jBooox系列则普遍采用静态类(public static class XxxxBox{ ... })作为配置，这是jBooox系列的一个主要特点，可以充分利用Java原汁原味的继承特性，而无需引入过多的注解。

jBooox是个示例项目，没有太多文档，所有的设计意图都体现在示例代码中，请自行翻阅源代码。因为作者本人的水平不高，各个子模块的源码都很短(或者说是简陋)，所以jBooox项目也适合初学者作为后端开发的入门材料来学习。  

### 备注:
在jSqlBox的demo目录下，还有另一个演示项目"jsqlbox-in-springs"，架构基于 jSqlBox + SpringTx + SpringIOC + SpringMVC,  演示了jSqlBox在传统Spring环境下的配置和使用，可以与本项目对比看一看。  