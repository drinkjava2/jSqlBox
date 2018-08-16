项目启动步骤

1. 运行"maven start jfinal.bat"批处理文件，启动jFinal  
2. 打开浏览器输入 http://localhost  查看运行效果  
注意第一行删不掉，它演示jFinal的声明式事务，删除了之后紧接着一个除0错误，事务自动回滚。  
 
注意：如果要导入Eclipse中运行，必须：  
1）运行"maven_eclipse_eclipse.bat"批处理文件，生成Eclipse的配置文件.classpath和.project，导入Eclipse  
2) 修改源代码中DemoConfig.java中的path="webapp" 为path="target/jsqlbox-in-jfinal-1.0"  
3）运行"maven_clean_package.bat"批处理文件，重新编译项目(以后每次更改源码后都需要)  
4) 选中DemoConfig.java，选择“Run as” ->"Java Application"  
 
 
注意：Tomcat下运行项目需要先删除jetty-server-xxx.jar或改变pom.xml中此项scope为"privided",否则可能会有冲突  
注意：本项目需Java1.8或更高版本  



