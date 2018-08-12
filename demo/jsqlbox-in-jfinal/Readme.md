项目启动步骤

1：使用 blog.sql 中的 sql 语句创建数据库与数据库表
2: 修改 res/a_little_config.txt 文件，填入正确的数据库连接用户名、密码
3. 运行"maven start jfinal.bat"批处理文件，启动jFinal
4. 打开浏览器输入http://localhost 即可查看运行效果
 
注意：如果导入Eclipse中运行，必须：
1) 修改源代码中DemoConfig.java中的path="webapp" 为path="target/jsqlbox-in-jfinal-1.0"; 
2）运行"maven_clean_package.bat"批处理文件，重新编译项目
3）运行"maven_eclipse_eclipse.bat"批处理文件，生成Eclipse的配置文件.classpath和.project
4) 导入Eclipse, 打开DemoConfig.java，选择“Run as” ->"Java Application"
 
 
注意： 请确保您安装了 JavaSE 1.6 或更高版本，tomcat下运行项目需要先删除 jetty-server-xxx.jar，否则可能会有冲突

