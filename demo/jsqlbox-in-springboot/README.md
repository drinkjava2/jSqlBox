## jsqlbox-in-springboot

这是一个为了演示jSqlBox在springboot环境中使用而创建的项目，项目架构是  SpringBoot(用到了它的H2数据源支持、声明式事务、IOC/AOP内核、MVC) + jSqlBox  
编译及运行本项目需Java8或以上环境。

编译及运行：mvn spring-boot:run

然后在浏览器打开： 
   http://localhost:8080/insert  （每访问一次将插入一笔记录到数据库)   
   http://localhost:8080/tx      （演示声明式事务，异常发生时，事务自动回滚)