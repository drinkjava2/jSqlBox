## jsqlbox-in-spring

这是一个为了演示jSqlBox的配置和使用而创建的Web项目，项目架构来源于https://github.com/Fruzenshtein/spr-mvc-hib 演示项目，原项目架构是Hibernate + SpringTx + SpringIOC + SpringMVC + MySql，本项目将其中的Hibernate用jSqlBox替换掉，MySql用H2内存数据库替换掉(以避免配置数据库的麻烦)，其余部分不变。  
编译及运行本项目需Java8, Tomcat7以上环境。

备注:
在jSqlBox的demo目录下，还有另一个演示项目"jbooox"，架构基于jbeanbox + jwebbox + jSqlBox, 有兴趣的可以将这两个项目对比看一下。  