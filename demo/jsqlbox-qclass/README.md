## jSqlBox-querydsl
This is a demo project to show how to use querydsl-maven-plugin to generate QClass and used in jSqlBox  

Q类可用于写出可重构的SQL, 这个演示项目演示Q类的两种生成方式:  

1.利用querydsl-maven-plugin插件生成QClass并在jSqlBox中使用  
 a. 先运行maven_eclipse_eclipse.bat批处理，生成QClass, 如果已存在QClass类，必须先运行maven_eclipse_clean.bat清除已有的设置  
 b. jSqlBox的DbContext初始化时，设定一个定制的SqlItemHandler  
 c. 在jSqlBox的qry系列方法中，可以直接使用QClass的属性作为SQL的一部分，运行期间会自动将这个属性转化为对应数据库表的列名来拼接SQL  


2.利用jSqlBox自带的根据数据库生成源码功能，直接从数据库生成Q类源代码，这种方式可以自定义类名，示例中自定义为P类  
  用法示例：TableModelUtils.db2QClassSrcFiles(ctx.getDataSource(), ctx.getDialect(), "c:/temp", "com.github.drinkjava2.jsqlboxdemo", "P");  
  jSqlBox自带功能生成的源码示例如下：  
```
   package com.github.drinkjava2.jsqlboxdemo;

   public class PUserDemo {
	public static final PUserDemo userDemo = new PUserDemo();

	public String toString(){
		return "user_demo";
	}

	public final String id = "id";

	public final String userAge = "user_age";

	public final String userName = "user_name";

    }
```



  