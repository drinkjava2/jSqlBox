Transaction App Demo
======================
 
A simple Transaction Application built on top of ActFramework and jSqlBox 
To run this application: 
1. Inside the demo project folder jsqlbox-in-actframework, type `mvn clean compile exec:exec`
2. Go to your browser and type `http://localhost:5460`. 
This demo use H2 memory database do test.

这个项目用来演示jSqlBox在ActFramework中的配置和使用。因为ActFramework不支持AOP切面编程，所以这里用了独立的第三方IOC/AOP工具jBeanBox。
如果不满意jBeanBox，也可以切换成其它IOC/AOP工具如Guice或Spring等，具体配置可见其它Demo示例，都大同小异，都是生成一个代理类来处理声明式事务。

命令行下输入 mvn clean compile exec:exec 运行
浏览器下输入 http://localhost:5460 查看