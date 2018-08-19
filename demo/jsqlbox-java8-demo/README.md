## jSqlBox-Java8
This is a demo project to show use jSqlBox in Java8 environment.

这是一个演示项目，演示jSqlBox在Java8环境下的应用(注意jSqlBox本身是在Java6环境下编译的，但是在Java8下也能工作)
编译及运行本项目需Java8或以上环境, 并且在pom.xml中引入jsqlbox-java8，而不是jsqlBox，详见用户手册。 

运行方式： maven test

本项目演示内容：  
1)利用Java8的Lambda语法写出支持重构的SQL，而且不重新发明SQL语法。 一个示例如下：
```
 User u = createAliasProxy(User.class);
 List<?> list1 = giQueryForMapList( //
    "select "//
    , (ALIAS) u::getId//
    , (C_ALIAS) u::getAddress //
    , (C_ALIAS) u::getName //
    , " from ", table(u), " where "//
    , (COL) u::getName, ">=?", param("Foo90") //
    , " and ", (COL) u::getAge, ">?", param(1) //
 );
```

2)顺便给出了一种在Java6环境下，也是有可能写出支持重构的SQL，示例如下：		
```
 User u = createAliasProxy(User.class);
 List<Map<String, Object>> list = giQueryForMapList(clean(), //
    "select "//
    , alias(u.getId())//
    , c_alias(u.getAddress())//
    , c_alias(u.getName())//
    , " from ", table(u), " where "//
    , col(u.getName()), ">=?", param("Foo90") //
    , " and ", col(u.getAge()), ">?", param(1) //
 );
```
这种方法没有Java8的Lambda方法好，因为它必须每次先调用一个clean()方法清除(上次操作留下来的)线程局部变量的内容。

3)利用Java8接口的默认方法，使得实体只需要声明ActiveRecordJava8接口(不需要实现任何方法)即可成为ActiveRecord实体，这种设计不占用Java宝贵的单继承，可以减少对业务建模的入侵。 
例如下面这个类继承了单元测试基类，以继承基类的变量和方法进行一些单元测试，但同时它也是一个ActiveRecord类，因为它声明了ActiveRecordJava8接口:
``` 
public class UserTest extends TestBase implements ActiveEntity<UserTest> { 
      ...方法体... 
}
```
