## jsqlbox-in-spring 
This is a demo project to show use jSqlBox in Spring.

这是一个传统WebApp演示项目，必须发布到Tomcat或WebLogic下运行，已有点过时，推荐使用内嵌Web服务模式，见jsqlbox-in-springboot目录下示例。

本演示项目主体部分来源于https://github.com/Fruzenshtein/spr-mvc-hib 演示项目，原项目架构是Hibernate + SpringTx + SpringIOC + SpringMVC + MySql，本项目将其中的Hibernate用jSqlBox替换掉，MySql用H2内存数据库替换掉(以避免配置数据库的麻烦)，其余部分不变。   
编译及运行本项目需Java8, Tomcat7以上环境，必须发布到Tomcat目录下运行。  

运行方式：修改deploy_tomcat.bat批处理文本,指向本机的Tomcat路径，双击运行  
查看：浏览器输入 http://localhost  
单元测试：运行 mvn test  
 