## jSqlBox-BeetlSql
This is a demo project to show how to develop customized Sql template engine in jSqlBox.

这是一个演示项目，演示在jSqlBox中用使用BeetlSql作为模板引擎。  
编译及运行本项目需Java8, Tomcat7以上环境。  
因为BeetlSql开发组可能没有想到有人要抽取它的模板引擎，所以模板功能与持久层功能结合太密，不太容易剥离开，采用了一些非常规手段，而且有些小问题，例如要提供一个数据源给BeetlSQL初始化，对于一个模板功能来说这是没必要的。  