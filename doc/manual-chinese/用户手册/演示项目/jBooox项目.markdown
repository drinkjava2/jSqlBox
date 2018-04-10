这是一个WebApp演示项目，主要架构基于 jbeanbox + jwebbox + jsqlbox，因为三个开源项目都以Box结尾, 所以项目简称jBooox。

编译及运行本项目需Java8, Tomcat7以上环境。

jBooox项目的架构特点是各个模块之间完全独立，每个模块都可以单独抽取出来使用，在Maven中央库都有发布。这是它与jFinal、Nutz之类提供一篮子整体解决方案工具的最大区别，jBooox不保证每个模块都令人满意，所以不会将这些模块强行捆绑打包提供。jBooox这种模块式架构设计与Spring系列工具类似，但是代码更短小、架构更松散，每个子模块都与业界最好的同类工具竟争，如果不满意，那就换掉它，甚至连IOC内核、声明式事务这些模块都不是主角，而是随时都可以被替换掉。

jBooox项目各模块简介:

jSqlBox是一个持久层工具，支持多种SQL写法。jBeanBox是一个IOC/AOP工具, 用于在项目中代替Spring-IOC内核。jWebBox是一个只有500行源码的后端页面布局工具，用于替代Apache-Tiles的布局功能，在本项目中客串充当MVC中的Controller角色。

jBooox项目可以在jSqlBox的demo目录下找到，另外在码云上也作为一个单独的项目存在:[jBooox主页](../tree/master/demo/jbooox)