这是一个为了演示jSqlBox在Spring环境中的配置和使用而创建的Web项目，项目架构来源于https://github.com/Fruzenshtein/spr-mvc-hib演示项目，原项目架构是Hibernate + SpringTx + SpringIOC + SpringMVC + MySql，本项目将其中的Hibernate用jSqlBox替换掉，MySql用H2内存数据库替换掉(以避免配置数据库的麻烦)，其余部分不变。  

编译及运行此项目需Java8, Tomcat7以上环境。  

此项目位于jSqlBox的demo目录下，见[这里](../tree/master/demo/jsqlbox-in-spring)