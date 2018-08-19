## jBooox 
This is a simple web demo project use jbeanbox + jwebbox + jsqlbox.

这是一个传统WebApp演示项目，主要架构基于 jbeanbox + jwebbox + jsqlbox，因为三个开源项目都以Box结尾, 所以项目简称jBooox。  
编译及运行本项目需Java8, Tomcat7以上环境，必须发布到Tomcat目录下运行。 

jBooox项目的架构特点是各个模块之间完全独立，每个模块都在Maven中央库都有单独发布。 

jBooox项目各模块简介:   
* jSqlBox是一个持久层工具，支持多种SQL写法，自带声明式事务(内含jTransactions模块)。  
* jBeanBox是一个IOC/AOP工具, 与它等效的工具是Spring内核、Guice等。  
* jWebBox是一个只有500行源码的后端页面布局工具，用于替代Apache-Tiles的布局功能，在本项目中客串充当MVC中的Controller角色，目前只支持在Servlet环境下的JSP和FreeMaker模板。  
 
jBooox是个示例项目，除了本文外没有更多文档，设计意图都体现在示例代码中，请自行翻阅源代码。 
 
 
运行方式：修改deploy_tomcat.bat批处理文本,指向本机的Tomcat路径，双击运行  
查看：浏览器输入 http://localhost   
单元测试：运行mvn test