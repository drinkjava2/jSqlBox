## jsqlbox-in-spring

这是一个为了演示jSqlBox的事务配置和在Web环境中使用而创建的项目，项目架构来源于https://github.com/Fruzenshtein/spr-mvc-hib 演示项目，原项目架构是Hibernate + SpringTx + SpringIOC + SpringMVC + MySql，本项目将其中的Hibernate用jSqlBox替换掉，MySql用H2内存数据库替换掉(以避免配置数据库的麻烦)，其余部分不变。  
编译及运行本项目需Java8, Tomcat7以上环境。
这个项目需要打包成war在Tomcat中运行，已经有点过时。如果使用SpringBoot，请参见Demo目录下另一个示例jsqlbox-in-springboot。