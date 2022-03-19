<p align="left">
  <a href="README_ENG.md">
	English instructions please see "README_ENG.md"
  </a>
</p>

<p align="center">
  <a href="https://github.com/drinkjava2/jsqlbox">
   <img alt="jsqlbox-logo" src="jsqlbox-logo.png">
  </a>
</p>

<p align="center"> 
  基于DbUtils内核的全功能数据库持久层工具
</p>

<p align="center">
  <a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.drinkjava2%22%20AND%20a%3A%22jsqlbox%22">
    <img alt="maven" src="https://img.shields.io/maven-central/v/com.github.drinkjava2/jsqlbox.svg?style=flat-square">
  </a>

  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
</p>

## 简介 | Intro
jSqlBox是一个全功能开源Java数据库持久层工具，在架构、功能、易用性等方面都不输于其它持久层工具，可以说，只要是与数据库操作相关的功能，jSqlBox都已具备，如DDL操作、分页、分库分表、声明式事务、分布式事务、关联映射查询等，所有这些功能都包含在一个1M大小的jar包中，不依赖任何第三方库。   

## 文档 | Documentation  
[中文](https://gitee.com/drinkjava2/jsqlbox/wikis/pages) | [English](https://github.com/drinkjava2/jsqlbox/wiki) | [JavaDoc](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jsqlbox%22) | [PDF](https://gitee.com/drinkjava2/jsqlbox/wikis/pages/export?type=pdf&info_id=92178)

## jSqlBox与其它持久层工具对比
请见[与其它DAO工具对比](https://gitee.com/drinkjava2/jsqlbox/wikis/pages?sort_id=1010925&doc_id=92178), 可以对jSqlBox的功能与特点有一个大概的了解。  

## 主要特点 | Advantages

### 配置简单，没有依赖任何第三方库
在pom.xml中加入以下依赖即可使用。 如果需要查看或修改源码，甚至可以直接将jSqlBox的源码拷到项目目录里就可以直接使用了。
```xml
<dependency>
   <groupId>com.github.drinkjava2</groupId>
   <artifactId>jsqlbox</artifactId>  
   <version>5.0.12.jre8</version> <!-- 或最新版 -->
</dependency> 
```

### 直接在Java里写SQL
jSqlBox的最大特点是直接在Java里SQL，它的整个架构都是建立在这个基础上，这是它与其它DAO工具最大的区别。在Java里直接写SQL的最大优点是学习成本低，只要会SQL就可以立即上手使用，降低了学习难度。使用jSqlBox并不表示要使用它的全部功能，很多时候使用SQL就能处理业务，没必要引入复杂的ORM。
```
DbContext db= new DbContext(dataSource);
db.exe("insert into users (", //
          " name ,", par("Sam"), //一个参数写一行，方便维护
          notNull("address,", user.getAddress()), //空值判断
	  when(age>10, "age,", par(user.getAge())), //根据条件动态添加SQL片段
          " address ", par("Canada"), //
          ") ", valuesQuestions()); //自动根据参数个数补上 values(?,?...?)片段
```
在Java里写SQL谁都会，但是象jSqlBox这样做到极致的DAO工具并不多，个人认为jSqlBox这种方式是最能发挥原生SQL的威力，同时也是易学性、灵活性、可护展性最好的SQL写法。

### 借助字符串常量或Q类，可以写出支持重构的SQL
```
 QTbPriceSetting p=QTbPriceSetting.instance;
 DB.exe("insert into ",p," (", //
	p.id, ",", par(1200), //
	p.price, ",", par(80), //
	p.currency, ",", par("USD"), //
	p.created_at, par("2019-09-17 04:07:55"), //
	")", valuesQuestions());
```
Q类或包含字符串常量的实体类源码，可以使用jSqlBox的源码生成功能，从数据库直接生成，另外jSqlBox也支持从实体类或数据库中导出Excel格式的数据库表结构，详见jDialect的[实体结构或数据库结构导出到Excel](https://gitee.com/drinkjava2/jdialects/wikis/%E7%94%A8%E6%88%B7%E6%89%8B%E5%86%8C/5.4%20%E5%AE%9E%E4%BD%93%E6%88%96%E6%95%B0%E6%8D%AE%E5%BA%93%E7%BB%93%E6%9E%84%E5%AF%BC%E5%87%BAExcel)

### 架构合理，模块式架构，各个子模块(jBeanBox,jDbPro,jDialects,jTransaction)都可以脱离jSqlBox单独存在。  
jSqlBox是源码包含模块式架构，目的是隔离功能点，并分享给其它工具重用。例如只想使用jDialcet数据库方言模块，可以在项目的pom.xml中加入:
```xml
<dependency>
    <groupId>com.github.drinkjava2</groupId>
    <artifactId>jdialects</artifactId>
    <version>5.0.11.jre8</version>
</dependency>
```

### 基于jDialects模块，支持80多种数据库的分页、DDl脚本生成、从数据库生成实体源码、函数变换、主键生成等功能。  
例如下面的SQL语句，就可以在多种数据库上使用而不需要更改源码，jSqlBox的jDialects模块自动处理与方言相关的DDL生成、分页、函数翻译。
```
DB.exe(DB.gctx().toCreateDDL(HelloWorld.class)); //根据实体生成DDL，创建数据库表
String sql=DB.trans("select concat('a','b','c'), current_time() from user_tb where age>0"); //根据方言对SQL函数翻译
DB.qryString(sql, " and price>100", pagin(1, 10));  //任意数据库分页只需要传入一个pagin对象
```
### 简洁的ActiveRecord模式
继承ActiveRRecord类，或只需要声明实现ActiveEntity接口，就可以实现ActiveRecord模式了：
```
public class User implements ActiveEntity{
 @UUID
 private String id;
 private String name;
 //getter &setter .....
}

//ActiveRecord模式
new User().loadById("张三").setUserAge(13).update(); 
```

### 强大的参数式设计。 拦截器、分页、模板、缓存、实体映射器等都可以当作参数拼接到SQL方法里去
例如SQL模板引擎可以当作参数传到SQL里，这样jSqlBox就具备了支持并以扩充各种SQL模板的功能：
```
SqlTemplateEngine TEMPLATE = BasicSqlTemplate.instance()
ctx2.exe(TEMPLATE, "update users set name=#{user.name}, address=:user.address", bind("user", tom));
```

再例如下面的SQL，只会执行一次，因为参数中有一个缓存拦截器，重复的SQL不再执行而是直接从缓存中取结果：
```
SimpleCacheHandler cache = new SimpleCacheHandler();
for (int i = 0; i < 10; i++)
   DB.qry(cache, new EntityListHandler(), DemoUser.class, "select u.* from DemoUser u where u.age>?", par(10));
```

### 为Java8及以下开发环境提供多行文本支持，方便利用IDE快速定位到多行SQL文本上
```
public static class InsertDemoSQL extends Text {
/*-  
insert into demo
      (id, name) 
values( ?,  ?)
*/
}
//使用：
DB.exe(InsertDemoSQL.class, par("1", "Foo"));
```
 
### 灵活的实体关联查询
例如下例，可以一次无递归查询树节点并装配成内存中对象树，其中的EntityNetHandler、alias, give等方法都是与实体关联映射相关的SQL参数：
```
Object[] targets = new Object[] { new EntityNetHandler(), TreeNode.class, TreeNode.class,
		alias("t", "p"), give("p", "t", "parent"), give("t", "p", "childs") };
EntityNet net = ctx.qry(targets, //深度树的海底捞算法
  "select t.**, t.pid as p_id from treenodetb t where t.line>=? and ",
      "t.line< (select min(line) from treenodetb where line>? and lvl<=?) ", par(line, line, lvl));
TreeNode node = net.pickOneEntity("t", d.getId());
```
不同于Hibernate和MyBatis复杂的配置，在jSqlBox中，实体关联查询只不过是一种参数略微复杂的SQL而已，随用随拼，不需要配置。

### 兼容主要JPA注解，支持在运行期动态更改配置
为了方便学习，jSqlBox兼容JPA实体类的以下主要注解:
```
@Entity, @Transient, @UniqueConstraint, @GenerationType, @Id, @Index, @SequenceGenerator, 
@GeneratedValue, @Table, @Column, @TableGenerator, @Version, @Enumerated, @Convert, @Temporal
```
jSql自带一些特殊实体注解如CreatedBy、LastModifiedBy、ShardTable、ShardDatabase、Snowflake等。
jSqlBox在运行期可动态更改实体关联配置，例如下面在运行期给一个pojo类动态配置UUID32主键，并更改它的name字段映射到address字段上:
```
TableModel m = TableModelUtils.entity2Model(PojoDemo.class);
m.column("id").pkey().uuid32();
m.column("name").setColumnName("address");
TableModelUtils.bindGlobalModel(PojoDemo.class, m);
```

### jSqlBox自带多租户、主从、分库分表功能
jSqlBox的主从和分库分表功能除了默认的用实体Sharding注解操作外，还支持将分库分表方法作为参数直接传到SQL中使用，精准控制每一条SQL的分库分表:
```
//实体的分库分表
for (i:=0;i<100;i++)
   new TheUser().put("databaseId", i).insert(); 
   
//SQL中的分库分表   
db[2].exe(User.class, "insert into ", shardTB(tbID), shardDB(dbID)," (id, name, databaseId) 
          values(?,?,?)", par(tbID, "u1", dbID), USE_BOTH);
```

多租户功能可以根据IP地址等进行分库分表，这个和根据实体字段内容分库分表是有区别的，实际多租户要配置TenantGetter实例：
```
 public static class CustomTenantGetter implements TenantGetter {
        @Override
        public ImprovedQueryRunner getTenant() {
            return DB.gctx(); //通常是根据IP地址等，从treadlocal中取一个DbContext实例：
        } 
    }
 //启动阶段
 ctx = new DbContext(); 
 ctx.setTenantGetter(new CustomTenantGetter());
```

### 自带声明式事务，也支持使用Spring的事务
jSqlBox内置IOC/AOP工具，自带声明式事务功能，详见“事务配置”一节。如果是在Spring环境中，单独或与其它工具比如Hibernate/MyBatis混用，配置也非常简单，如下：
```
DbContext ctx = new DbContext(ds);
ctx.setConnectionManager(SpringTxConnectionManager.instance());
DbContext.setGlobalDbBoxContext(ctx);// 设定静态全局上下文
```

### 自带分布式事务功能
详见“分布式事务”一节，jSqlBox的分布式事务原理和Seata项目类似，可以自动生成回滚SQL，但jSqlBox的源码远比Seata简洁，因为jSqlBox是基于实体的CRUD生成回滚SQL，所以不需要考虑SQL兼容性这个问题。

### 不重复发明轮子，避开反模式
jSqlBox不重新发明轮子，使用DbUtils作为内核，以源码内含的方式包含到项目中，DbUtils是一个成熟、简洁、高效的JDBC工具。
jSqlBox尽量避免反模式，反模式就是花很多时间做没有意义的事，比如作者认为实体一对多、多对一的关联配置是个反模式，带来的性能、学习、维护问题往往比它解决的问题还要多，所以在jSqlBox中不支持一对多、多对一的JPA注解支持。
jSqlBox中也不存在用Java方法代替SQL关键字的这种做法，认为它也是一种反模式，比如下面这种写法：
```
List<S1UserPojo> userPojoList = dslContext.select()
            .from(S1_USER)
            .where(S1_USER.ID.eq(1))
            .fetch(r -> r.into(S1UserPojo.class));
```
只适合简单的CRUD，当逻辑稍微一复杂可读性、可维护性就非常差，还不如直接手写SQL来得方便。


## 入门 | First Example
以下示例演示了jSqlBox的基本配置和使用:
```
pom.xml中引入：
    <dependency>
      <groupId>com.github.drinkjava2</groupId>
       <artifactId>jsqlbox</artifactId> 
       <version>5.0.10.jre8</version> <!-- 或最新版 -->
    </dependency>

    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId> <!--H2内存数据库用于演示-->
      <version>1.3.176</version>
    </dependency>

在IDE里输入以下源程序：

import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import static com.github.drinkjava2.jsqlbox.DB.*;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jsqlbox.ActiveEntity;
import com.github.drinkjava2.jsqlbox.DB;
import com.github.drinkjava2.jsqlbox.DbContext;

public class HelloWorld implements ActiveEntity<HelloWorld> {
  @Id
  @UUID25
  private String id;
  private String name;
  public String getId() {return id;}
  public void setId(String id) {this.id = id;}
  public String getName() {return name;}
  public HelloWorld setName(String name) {this.name = name;return this;}

    public static void main(String[] args) {
     DataSource ds = JdbcConnectionPool  //这个示例使用h2内存数据库
                     .create("jdbc:h2:mem:demo;MODE=MYSQL;TRACE_LEVEL_SYSTEM_OUT=0", "sa", "");
     DbContext ctx = new DbContext(ds);
     ctx.setAllowShowSQL(true);  //开启SQL日志输出
     DbContext.setGlobalDbContext(ctx);  //设定全局上下文
     ctx.quiteExecute(ctx.toDropAndCreateDDL(HelloWorld.class));  //生成DDL,建数据库表
     ctx.tx(() -> {  //开启事务
        HelloWorld h = new HelloWorld().setName("Foo").insert().putField("name", "Hello jSqlBox").update();
        System.out.println(DB.qryString("select name from HelloWorld where name like", que("H%"),
					" or name=", que("1"), " or name =", que("2")));
        h.delete();  //删除实体
     });
     ctx.executeDDL(ctx.toDropDDL(HelloWorld.class)); //删除数据库表
   }
}
```
上面这个演示包括了根据实体类生成DDL并执行、插入实体到数据库、执行更新、查询出结果、即打印出"Hello jSqlBox"、删除实体、删除数据库。
示例中的实体类只需要声明接口(限Java8版)。查询语句使用了jSqlBox独创的参数内嵌式SQL写法，可以自由拼接复杂的条件SQL，也不用考虑参数和问号对齐的问题了。  
上例中的事务语法也可以写成传统的方式：
```
        try {
            ctx.startTrans(); //开启事务
            HelloWorld h = new HelloWorld().setName("Foo").insert()；
            ctx.commitTrans(); //提交事务
        } catch (Exception e) { 
            ctx.rollbackTrans(); //回滚事务
        }
```
因为开启了日志输出，可以看到命令行打印出的SQL执行日志:
```
SQL: drop table HelloWorld if exists
PAR: []
SQL: create table HelloWorld ( id varchar(250),name varchar(250), primary key (id))
PAR: []
SQL: insert into HelloWorld (name, id)  values(?,?)
PAR: [Foo, emeai4bfdsciufuuteb9a7nmo]
SQL: update HelloWorld set name=?  where id=?
PAR: [Hello jSqlBox, emeai4bfdsciufuuteb9a7nmo]
SQL: select name from HelloWorld where name like? or name=? or name =?
PAR: [H%, 1, 2]
SQL: delete from HelloWorld where id=? 
PAR: [emeai4bfdsciufuuteb9a7nmo]
SQL: drop table HelloWorld if exists
PAR: []
```
更全面的功能点介绍和使用，请详见jSqlBox的[用户手册](https://gitee.com/drinkjava2/jsqlbox/wikis/pages)

## 范例 | Demo
以下范例位于jSqlBox的demo目录下：  
* [jbooox](../../tree/master/demo/jsqlbox-jbooox) 这是一个微型Web演示项目，基于三个开源项目jBeanBox、jSqlBox、jWebBox的整合。
* [jsqlbox-actframework](../../tree/master/demo/jsqlbox-actframework) 演示jSqlBox与ActFramework框架的整合，分别展示利用jBeanBox和Guice来实现声明式事务。
* [jsqlbox-jfinal](../../tree/master/demo/jsqlbox-jfinal) 演示jSqlBox与jFinal的整合，用jSqlBox替换掉jFinal自带的DAO工具。
* [jSqlBox-Spring](../../tree/master/demo/jsqlbox-spring) 演示jSqlBox在Spring+Tomcat环境下的配置和使用
* [jsqlbox-springboot](../../tree/master/demo/jsqlbox-springboot) 演示jSqlBox在SpringBoot环境下的配置和使用。  
* [jsqlbox-mybatis](../../tree/master/demo/jsqlbox-mybatis) 演示在SpringBoot环境下jSqlBox和MyBatis的混合使用。
* [jsqlbox-beetl](../../tree/master/demo/jsqlbox-beetl) 演示如何在jSqlBox中自定义SQL模板引擎，此演示使用Beetl作为SQL模板。
* [jsqlbox-qclass](../../tree/master/demo/jsqlbox-qclass) 演示如何利用静态变量或Q类来写出支持重构的SQL
 
## 相关开源项目 | Related Projects
- [数据库方言工具 jDialects](https://gitee.com/drinkjava2/jdialects)
- [独立的声明式事务工具 jTransactions](https://gitee.com/drinkjava2/jTransactions)
- [微型IOC/AOP工具 jBeanBox](https://gitee.com/drinkjava2/jBeanBox)
- [前端直接写SQL和业务逻辑 GoSqlGo](https://gitee.com/drinkjava2/gosqlgo)

## 期望 | Futures

欢迎发issue提出更好的意见或提交PR，帮助完善项目

## 版权 | License

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## 关注我 | About Me
[码云](https://gitee.com/drinkjava2)  
[Github](https://github.com/drinkjava2)  