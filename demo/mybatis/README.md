## jsqlbox-in-springboot-mybatis
This is a demo project to show mixed use jSqlBox and MyBatis in SpringBoot.

这是一个为了演示jSqlBox和MyBatis在springboot环境中混用而创建的项目，项目架构是SpringBoot(用到了它的H2数据源支持、声明式事务、IOC/AOP、MVC) + jSqlBox + MyBatis
编译及运行本项目需Java8或以上环境。 

编译及运行：mvn spring-boot:run
在浏览器查看: http://localhost 

备注: 另外一种混用MyBatis和jSqlBoxr的方式是使用MyFat插件，它的原理是对MyBatis的SqlSession和Mapper进行了增强，将jSqlBox的功能织入到MyBatis中去，使用起来更方便，详见[MyFat](https://gitee.com/drinkjava2/myfat)项目介绍。