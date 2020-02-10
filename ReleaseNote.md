** 关于jSqlBox版本release说明 


从4.0.0版本起，jSqlBox所有发布包都不含第三方依赖，它只有一个单个jar包即可完成所有DAO层的开发工作。所有外部依赖如jBeanBox、CgLib、ASM、Apache-commons-DbUtils等都用源码内嵌的方式整合到jSqlBox中去了。  

从4.0.0版本起，jSqlBox通过在版本号后加jre8或jre6来区分是用于Java8还是Java6环境，见下：  


*** Java8版本发布
本项目core目录基于Java8环境开发，发布到maven时在JDK8下按正常过程release即可，最后生成pom为以下内容的发布库：  

```
    <dependency>
      <groupId>com.github.drinkjava2</groupId>
      <artifactId>jsqlbox</artifactId>
      <version>4.0.0.jre8</version><!-- 或其它版本号,以jre8结尾 -->
    </dependency>
```

  
*** Java6版本发布
为了方便一些遗留项目使用，jSqlBox还发布一个Java6版本，发布后的pom格式如下：

```
    <dependency>
      <groupId>com.github.drinkjava2</groupId>
      <artifactId>jsqlbox</artifactId>
      <version>4.0.0.jre6</version><!-- 或其它版本号,以jre6结尾 -->
    </dependency>
```
具体Java6版本的发布过程如下：   
1. 修改pom.xml中的4.x.x.jre8版本号为4.x.x.jre6, 并将<version.java>1.8</version.java>中的1.8改为1.6   
2. 使用IDE将所有源码中的/*- JAVA8_BEGIN */替换成 /*- JAVA8_BEGIN ，将所有源码中的/* JAVA8_END */替换成 JAVA8_END */  
3. 在IDE里测试通过后, 在纯JDK1.6环境下发布即可  
JAVA里没有条件编译功能，以上做法是通过文本替换方式来进行代码与注释转换。  