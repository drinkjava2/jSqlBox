** 关于jSqlBox版本release说明 


从4.0.0版本起，jSqlBox所有发布包都不含第三方依赖，它只有一个单个jar包即可完成所有DAO层的开发工作。所有外部依赖如jBeanBox、CgLib、ASM、Apache-commons-DbUtils等都用源码内嵌的方式整合到jSqlBox中去了。  

从4.0.0版本起，jSqlBox通过在版本号后加jre8或jre7来区分是用于Java8还是Java7环境，见下：  


*** Java8版本发布
本项目core目录基于Java8环境开发，发布到maven时在JDK8下按正常过程release即可，最后生成pom为以下内容的发布库：  

```
    <dependency>
      <groupId>com.github.drinkjava2</groupId>
      <artifactId>jsqlbox</artifactId>
      <version>4.0.0.jre8</version><!-- 或其它新版本号 -->
    </dependency>
```

  
*** Java7版本发布
为了方便一些遗留项目使用，jSqlBox还发布一个Java7版本，发布后的pom格式如下：

```
    <dependency>
      <groupId>com.github.drinkjava2</groupId>
      <artifactId>jsqlbox</artifactId>
      <version>4.0.0.jre7</version><!-- 或其它新版本号 -->
    </dependency>
```
具体Java7版本的发布过程如下：   
1. 拷贝项目到一个临时目录   
2. 删除其中用到Java8特性的源程序，主要是以下文件，并删除所有依赖它们的单元测试类。  
```  
  ActiveEntity.java
  LambdSqlItem.java
  Java8DateUtils.java
  Java8DateUtilsTest.java 
  Java8EampleTest.java
  Java8DateTimeTest.java

```
3. 删除源码中的cglib、asm、jBeanBox三个目录，并从jBeanBox项目将其core目录下除Test目录的所有源码拷贝进来，这是Java7环境的jBeanBox源码    
4. 修改pom.xml中的4.x.x.jre8版本号为4.x.x.jre7, 并将其中<version.java>1.8</version.java>的1.8改为1.7  
5. 在IDE里测试通过, 在JDK1.6环境下发布即可。