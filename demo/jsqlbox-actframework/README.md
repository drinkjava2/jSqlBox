### Transaction App Demo
This is a demo project to show use jSqlBox in ActFramework.
 
A simple Transaction Application built on top of ActFramework and jSqlBox 
To run this application: 
1. Inside the demo project folder jsqlbox-in-actframework, type `mvn clean compile exec:exec`
2. Go to your browser and type `http://localhost`. 
This demo use H2 memory database do test.

这个项目用来演示jSqlBox在ActFramework中的配置和使用。因为ActFramework目前不支持AOP联盟标准接口，为了使用jSqlBox的声明式事务，这个例子里使用了独立的第三方IOC/AOP工具Guice和jBeanBox两种演示。

命令行下输入:  mvn clean compile exec:exec  
浏览器下查看： http://localhost  

(注意这个示例可能有个问题，DOS窗口关闭后,后台的Web服务不能自动退出，并占用Web端口，必须在windows下打开任务管理器手工杀掉一个Java.exe进程，这与ActFramework的Web服务有关，已向作者提交issue)