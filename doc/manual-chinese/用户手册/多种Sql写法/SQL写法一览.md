jSqlBox继承于DbUtils内核、无会话的架构设计，使得它支持SQL的写法多种多样，从最底层的手工JDBC操作到最顶层的ActiveRecord和NoSql式对象关联查询都有，从SQL写法这个角度来看，可以说是一个“全栈” 的持久层工具了。

以下为各种SQL写法的快速一览，具体每种写法的深入介绍请点击菜单介绍。

SQL写法1 - 带Connection参数，手工负责连接的获取和关闭, 这是SqlBoxContext继承于DbUtils的QueryRunner的方法。
```Java
    Connection conn = null;
    try {
      conn = ctx.prepareConnection();
      ctx.execute(conn, 
         "insert into users (name,address) values(?,?)", "Sam", "Canada");
    } catch (SQLException e) {
       doSomething(e);
    } finally {
      try {
        ctx.close(conn);
      } catch (SQLException e) {
        doSomething(e);
      }
    }
```
　　  
　　  
SQL写法2 - 无Connection参数, 也是继承于DbUtils的QueryRunner。无需关心连接的获取和关闭，使用这种方式要注意SqlBoxContext的生成必须提供一个dataSource构造参数。
```Java
    try {
      ctx.execute("insert into users (name,address) values(?,?)", "Sam", "Canada");
    } catch (SQLException e) {
        doSomething(e);
    }
```
　　  
　　  
SQL写法3 - nXxxx()方法，从jDbPro模块开始新加入的方法，不用再捕捉SqlException异常。SqlException异常转化为RuntimeException异常抛出，这种方式通常与支持声明式事务的AOP工具如Spring联用(详见demo中的jBooox和jsqlbox-spring两个示例项目)。
```Java
    ctx.nExecute("insert into users (name,address) values(?,?)", "Sam", "Canada");
```
　　  
　　  
SQL写法4 - iXxxx()方法，SQL内嵌参数(Inline风格)，在SQL里直接写参数，参数暂存在Threadlocal中,SQL执行时自动转化为preparedStatement，这种方式的优点是被赋值的字段和实际参数可以写在同一行上,字段很多时利于维护，也方便根据不确定的条件动态拼接SQL，这是始于jSqlBox的技术创新。
```Java
ctx.iExecute("insert into users (", //
  " name ,", param0("Sam"), //
  " address ", param("Canada"), //
  ") ", valuesQuesions());
```
　　  
　　  
SQL写法5 - tXxxx()方法, 模板风格，利用模板来存放SQL变量:
```Java
    Map<String, Object> params=new HashMap<String, Object>();
    User sam = new User("Sam", "Canada") 
    params.put("user", sam);
    ctx.tExecute(params,
       "insert into users (name, address) values(#{user.name},#{user.address})");
```
jSqlBox的模板是可以更换的，它自带两种模板，缺省的模板允许使用“#{}”占位符和“:”冒号占位符混用。并且在demo目录下也有一个演示jSqlBox利用自定义模板来使用BeetlSql的模板的例子。  
　　  
SQL写法6 - xXxxx()方法，模板风格和Inline风格的结合使用：
```Java
    put0("user", new User("Sam"));
    ctx.xExecute("insert into users (name) values(#{user.name})");
```
　　  
　　  
SQL写法7 - Data Mapper数据映射风格，与Hibernate中的写法一样。从性能和功能方面考虑，jSqlBox建议实体类继承于ActiveRecord类，但是对于纯粹的POJO类，jSqlBox也是支持的。  
```Java
    User user = new User("Sam", "Canada");
    ctx.insert(user);
```
　　  
　　  
SQL写法8 - ActiveRecord风格，继承于ActiveRecord类的实体类，自动拥有insert/update/delete等CRUD方法。
```
    user = new User();
    user.setName("Sam");
    user.useContext(ctx); // set a SqlBoxContext to entity
    user.insert();
```
　　  
　　  
以下是ActiveRecord风格的一个变种，使用这种写法的前提是必须在程序启动时调用SqlBoxContext.setDefaultContext(ctx)方法设定一个全局缺省上下文，适用于单数据源场合。这种写法的优点是业务方法里完全看不到持久层工具的影子: 
```Java
    User user = new User("Sam", "Canada").insert();
```
　　  
　　  
SQL写法9 - 链式风格，严格来说这种风格还是应该归类于ActiveRecord模式：
```Java
   链式风格之一：
   new User().put("id","u1").put("userName","user1").insert(); 
   
   链式风格之二：
   new Address().put("id","a1","addressName","address1","userId","u1").insert();
   
   链式风格之三：
   new Email().putFields("id","emailName","userId");
   new Email().putValues("e1","email1","u1").insert();
   new Email().putValues("e2","email2","u1").insert();   
``` 
　　  
　　  
SQL写法10 - SqlMapper风格  
SqlMapper风格之一: 使用SQL注解  
```
@Handler(MapListHandler.class)
@Sql("select * from users where name=? and address=?")
public List<Map<String, Object>> selectUsers(String name, String address) {
    return guess(name, address);
};
//主程序中：
List<Map<String, Object>> users = new User().selectUsers("Tom", "China");
```
guess方法表示交给jSqlBox来根据@SQL和@Handler注解等来自动运行和装配出所需要返回的对象。
　　  
SqlMapper风格之二: 使用Java多行文本
```
public List<User> selectUsersByText2(String name, String address) {
   return ctx().nQuery(new EntityListHandler(User.class), guessSQL(), name, address);
}
	/*-
	   select u.** 
	   from 
	   users u
	      where 
	         u.name=? and address=?
	 */
//主程序中：
List<User> users2 = new User().selectUsersByText2("Tom", "China");
```
利用Java的/*-注释来实现多行文本支持，这是一个黑科技，付出的代阶是需要将Java实体类源码放在Resource目录而不是src目录，并在Maven中配置。详见"多行SQL文本"菜单链接中的介绍。guessSQL()方法自动判断出这个方法的SQL文本，是Sql注解还是多行文本。用Java实现多行文本支持的优点是可以利用IDE可以快速定位到SQL文本。

SqlMapper风格之三: 抽象类实例化
```
public abstract List<User> selectUsersByText(String name, String address);
	/*-
	   select u.** 
	   from 
	   users u
	      where 
	         u.name=? and address=?
	 */
//主程序中：
AbstractUser user = ActiveRecord.create(AbstractUser.class);
List<User> users2 = user.selectUsersByText("Tom", "China");
```
这纯粹是个试验大于实用的黑科技了，如果说多行文本还比较麻烦的话，这个甚至不能保证能运行了。不光需要将Java抽象类的源码放在Resource目录下，而且还用到了动态编译技术，不保证所有Java环境下都能编译通过(仅在Tomcat/WebLogic上测试过)。生成的user实例是AbstractUser的一个子类，而不是一个代理类。 

SqlMapper风格之四: 接口类实例化, 定义一个接口，返回一个代理类(正在考虑中)
```
public List<User> selectUsersByText(String name, String address);
	/*-
	   select u.** 
	   from 
	   users u
	      where 
	         u.name=? and address=?
	 */
//主程序中：
User user = ActiveRecord.create(AbstractUser.class);
List<User> users2 = user.selectUsersByText("Tom", "China");
```
这个功能在jSqlBox中未实现，从技术角度来说没有问题，Spring和MyBatis早已实现，但对于我来说，一是因为太忙了，二是本人不是太喜欢，这种写法的唯一优点就是省略了方法体 "return guess(参数);",  少打了几个字，带来的问题却不少，打破了"ActiveRecord"与"SqlMapper"合体的理念，当遇到复杂的映射时，注解配置或XML配置都有隔靴搔痒之嫌，最简单的办法就是赤膊上阵，在方法体里自已装配对象，但问题是接口是不支持方法体的。

SQL写法11 - NoSQL式查询  
NoSQL适合于查询对象关联复杂、普通SQL很难基至无法查询的场合(例如对树结构的查询)。  
对于下图的一组关系数据库表格，User与Role是多对多关系，Role与Privilege是多对多关系，UserRole和RolePrivilege是两个中间表，用来连接多对多关系。如果用普通的SQL进行多表关联查询比较麻烦，jSqlBox中包含了一个TinyNet子项目，专门用于将关系数据库转化为内存中的图结构，从而可以运用NoSql式查询方法，在节点间浏览查询。  
![image](https://gitee.com/drinkjava2/jSqlBox/raw/master/orm.png "图片")  
例如查询u1和u2对应有哪些Privilege,用以下两行代码即可：  
```Java
TinyNet net = ctx.netLoad(User.class, Role.class, Privilege.class, UserRole.class,
     RolePrivilege.class);
Set<Privilege> privileges = net.findEntitySet(Privilege.class,
     new Path(User.class).where("id='u1' or id='u2'").autoPath(Privilege.class));
```
jSqlBox中没有通常的一对一、一对多之类的关联映射配置，而是利用数据库外键配置结合NoSQL查询来实现对象间的游历查询，而且是只读的。关于NoSQL查询的内容比较多，请详见右侧"NoSQL查询"一节。