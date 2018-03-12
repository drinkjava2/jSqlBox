## jBooox 

这是一个WebApp演示项目，主要架构基于 jbeanbox + jwebbox + jsqlbox，因为三个开源项目都以Box结尾, 所以项目简称jBooox。  
编译及运行本项目需Java8, Tomcat7以上环境。

jBooox项目的架构特点是各个模块之间完全独立，每个模块都可以单独抽取出来使用，在Maven中央库都有发布。这是它与jFinal、Nutz之类提供一篮子整体解决方案工具的最大区别，jBooox不保证每个模块都令人满意，所以不会将这些模块强行捆绑打包提供。jBooox这种模块式架构设计与Spring系列工具类似，但是代码更短小、架构更松散，每个子模块都与业界最好的同类工具竟争，如果不满意，那就换掉它，甚至连IOC内核、声明式事务这些模块都不是主角，而是随时都可以被替换掉。 

jBooox项目各模块简介:   
* jSqlBox是一个持久层工具，支持多种SQL写法。  
* jBeanBox是一个IOC/AOP工具, 用于在项目中代替Spring-IOC内核。  
* jWebBox是一个只有500行源码的后端页面布局工具，用于替代Apache-Tiles的布局功能，在本项目中客串充当MVC中的Controller角色。  

另外jSqlBox模块本身还依赖于以下四个子模块:  
* Apache-DbUtils是Apache旗下的持久层工具，对Jdbc进行了简单的封装, jSqlBox建立在DbUtils这个微内核上，虽然这个内核功能不象MyBatis那样功能多，但优点是更贴近底层，适应面广，可二次开发的余地大。  
* jDbPro对DbUtils进行了增强，改进了事务处理方式，添加了参数内嵌、模板支持等多种SQL风格。
* jTransactions是一个独立的声明式事务工具，自带一个微型的声明式事务实现TinyTx，并可配置成使用Spring事务。  
* jDialects是一个独立的数据库方言工具，支持70多种数据库方言的DDL生成、分页、函数变换。  

jBooox中的Dispatcher需要程序员自已写，虽然麻烦一点，但是有了更多的自主控制。jBooox项目不提供Bean字段填充、表单内容保持、校验、i18n、页面集成测试等功能，因为相对于MVC架构来说，这些杂项功能其实不是重点，而且可以找到一些现成的第三方工具来实现。  

jBooox的Controller由WebBox来允当，因为WebBox不是线程安全的，所以在Dispatcher中必须每次new一个新的WebBox实例作为Controller，或用IOC/AOP工具(当用到事务时)每次创建一个非单例实例。  

jBooox中的事务入口既可以用@TX之类的注解配置在Service类的方法上，也可以配置在Controller类的方法上。事务入口配置在Service方法上性能高，因为通常Service是单例。事务入口配置在Controller上的优点则是可以省去Service层，结合jSqlBox的ActiveRecord与SqlMapper合体的特点，一些简单的持久层访问或是只读类型的访问完全可以省略Service层，提高开发效率。  

Spring系列的Java配置方式，包括IOC、Controller等通常采用Java方法作为配置，而jBooox系列则普遍采用静态类 public static class XxxxBox{ ... }这种方式作为配置，这是jBooox系列工具的一个特点，可以充分利用Java原汁原味的语法，而无需引入满天飞的注解和代理模式(例如Spring中@Configuration注解的类是一个代理类，不再是普通的Java类)。  

jBooox是个示例项目，除了本文外没有更多文档，设计意图都体现在示例代码中，请自行翻阅源代码。因为作者本人的水平不高，各个子模块的源码都很短(或者说是简陋)，所以jBooox项目也适合Java初学者作为后端开发的入门材料来学习。  

jBooox系列工具本身皆于JDK6版本编译发布，以达到支持最多开发环境的目的(Java向下兼容)，短期内不考虑发布更高JDK版本，如有需要请自行在高版本下编译。jBooox示例项目中的pom.xml配置总是反映当前各模块的最新发布版本。  

 
### 备注:
在jSqlBox的demo目录下，还有另一个演示项目"jsqlbox-in-springs"，架构基于 jSqlBox + SpringIOC + SpringTx + SpringMVC,  演示了jSqlBox在传统Spring环境下的配置和使用，可以与本项目对比看一看。  
