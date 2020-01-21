项目启动步骤

1. 启动jFinal: 双击批处理文件 maven start jfinal.bat  
2. 在浏览器查看效果： http://localhost  

注意数据的第一行演示删不掉，目的是为了演示jSqlBox使用jFinal的声明式事务，删除第一行会抛出一个Div/0错误，事务会自动回滚。  
 
注意：如果要导入Eclipse中运行，必须：  
1）命令行下运行maven_eclipse:eclipse，生成Eclipse的配置文件.classpath和.project，导入Eclipse  
2) 修改源代码中DemoConfig.java中的path="webapp" 为path="target/jsqlbox-in-jfinal-1.0"  
3）运行"maven_clean_package.bat"批处理文件，重新编译项目(以后每次更改源码后都需要)  
4) 选中DemoConfig.java，选择“Run as” ->"Java Application"  
 
 
注意：
Tomcat下运行项目需要先删除jetty-server-xxx.jar或改变pom.xml中此项scope为"privided",否则可能会有冲突。  
本演示项目需Java1.8或更高版本。  
