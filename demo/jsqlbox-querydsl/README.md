## jSqlBox-querydsl
This is a demo project to show how to use querydsl-maven-plugin to generate QClass and used in jSqlBox  

这是一个演示项目，演示如何利用querydsl-maven-plugin插件生成QClass并在jSqlBox中使用  
1. 先运行maven_eclipse_eclipse.bat批处理，生成QClass, 如果已存在QClass类，必须先运行maven_eclipse_clean.bat清除已有的设置  
2. jSqlBox的DbContext初始化时，设定一个定制的SqlItemHandler  
3. 在jSqlBox的qry系列方法中，可以直接使用QClass的属性作为SQL的一部分，运行期间会自动将这个属性转化为对应数据库表的列名来拼接SQL  


备注：另一种推荐的写出可重构的SQL的方式是直接利用jSqlBox自带的源码生成工具，可根据数据库内容直接生成类似于QClass的列名常量辅助类，详见jSqlBox的wiki中“可重构的SQL”一节介绍。  
  