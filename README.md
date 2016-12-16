##jSQLBox (In Developing)
**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)  

jSQLBox是一个微型的、易学易用的、支持基本的透明持久化的工具，目标是用来代替功能强大但过于复杂的Hibernate以及一些简单的但功能不足的持久层工具如JDBC/JDBCTemplate/MyBatis/DButils/EBean/OpenJPA/jFinal/jActiveRecord/ActiveJDBC/JOOQ等。 目前jSQLBox项目正在开发阶段，欢迎有兴趣者加入。

一张对比图显示jSQLBox项目的定位和开发目标：(打分仅为个人看法，5分制，X表示不及格, 空着的表示不了解，不好打分）
![image](jsqlbox.png)

jSQLBox优点:  
*提供CRUD方法，CRUD占持久层日常~70%的工作量，对一个持久层工具来说自动生成CRUD方法是必须提供的基本功能。  
*基于ActiveRecord模式，无DAO显式注入。  
*不重新发明SQL,直接使用原生SQL。  
*对JDBC的改进，SQLHelper类支持字符串拼接的SQL自动包装成preparedStatement，防止SQL注入，提高性能，提高可读性和可维护性。  
*低侵入，PO类不继承其它类，不实现接口(见缺点项)  
*无配置，PO可由代码工具根据数据库自动生成，且无任何配置文件。  
*可配置，当数据库表字段与PO不一致时，或需要存取个别字段或按SQL存取时，可用配置的方式来解决，配置为纯Java类，类似jBeanBox项目。  
*多配置和动态配置，同一个PO可采用不同的配置以进行多种方式的存取,配置可以继承重用已有的配置，配置可以动态生成和修改。  
*一级缓存，与Hibernate类似，提供以ID为主键的一级缓存，一级缓存在跨越多个方法的同一事务中有效。  
*多主键支持，一级缓存支持多主键，适于开发使用了业务主键的（遗留）数据库。  
*脏检查(透明持久化)，与Hibernate类似，对PO代理实例的存取不再重复访问数据库，除低数据库压力。  
*提供PO原型和PO代理两种方式，对不需要透明持久化代理的简单操作可以直接使用原型PO以简化操作。  
*跨数据库, CRUD方法在主流数据库上通用。  
*没有对象关联、LazyLoad带来的OpenSessionInView问题和N+1问题, PO类可以直接传递到View层(JSP)，PO自带Dao属性, 在View层事务不存在的情况下，依然可以继续存取数据库(通常只读)。  
*易学易维护易扩充，借用成熟工具(内部借用了Spring的JdbcTemplate，外部的事务可借用Spring的声明式事务)   

jSQLBox缺点:  
*比较新，缺少测试，缺少开发者（如果看好这个项目，请伸出您的援助之手，任何建议和批评都会促使它不断改进）  
*虽然不占用JAVA的单继承，但是通过直接在每个PO类源码中添加属性和方法来实现ActiveRecord模式，这与实现接口等效，造成PO对jSQLBox类的依赖。  
*没有一对多，多对一概念，实体类无关联，无级联更新等功能，需手工维护。(但也因此而降低了复杂性和学习成本)  
*暂无版本检查和二级缓存功能，可能在后续版本中加入  
*无分页、全文检索等高级功能，需手工实现。  

jSQLBox is a micro scale persistence tool for Java7+.

Other persistence tools' problem:  
Hibernate: It's a ORM tool, too complicated, the XML or Annotation configurations are fixed, fields definations, "One to Many", "Many to One" relationships are fixed, hard to create/modify/re-use configuations at runtime. For example, can not temporally change configuration at runtime to exclude some fields to avoid lazy loading.  
Other DB tools: Have no a balance between powerfulness and simpliness.  

Feature of jSQLBox:  
1) Simple, very few source code, No XML, easy to learn and use.   
2) The Java-Based Bean configuration can be created/modified dynamically at runtime(similar like jBeanBox project).  
3) The configuration and Bean classes can be created by source code generation tool because it's a simple map of database.  
4) jSQLBox is based on Active Record design mode but entity class do not extends from any base class.  
5) There is no "many to one", "One to Many", "Cascade update" concept in jSQLBox, need maintence relationship by hand.  
6) jSQLBox support transparent persistence based on a transaction scope 1st level cache (supported by AOP tool like jBeanBox).  
7) jSQLBox offers basic CURD methods, but encourage mixed use with raw SQL, it's safe to mix use CRUD methods and sql.  
8) Current version jSQLBox only tested for MySQL and Oracle, in future version will support more databases. 
9) To make it simple, jSQLBox project only focus on basic CRUD method + SQL + transparent persistence, do not offer complex functions like paginnation, lazy load.  

How to use jSQLBox?  
To be released into Maven central repository

How to import jSQLBox project into Eclipse(for developer)?  
1)install JDK1.7+, Git bash, Maven3.3.9+, on command mode, run:
2)git clone https://github.com/drinkjava2/jSQLBox
3)cd jsqlbox
4)mvn eclipse:eclipse
5)mvn test
6)Open Eclipse, "import"->"Existing Projects into Workspace", select jsqlbox folder, there are 2 module projects "jsqlbox" and "jsqlbox-test" will be imported in Eclipse. 

A basic introduction of jSQLBox:
---
Example 1 - Hello word (入门示例)
```
	public static void main(String[] args) {
		ComboPooledDataSource ds = new ComboPooledDataSource();//c3p0池设定
		ds.setUser("root");
		ds.setPassword("root888");
		ds.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useSSL=false");
		ds.setDriverClass("com.mysql.jdbc.Driver"); 

		SqlBoxContext ctx = new SqlBoxContext(ds);//生成SqlBoxContext上下文实例
		User u = ctx.create(User.class); //根据类名创建PO实例
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setAge(10);
		u.dao().save(); //保存到数据库 
	} 
```

Example 2 - Use IOC tool like Spring or jBeanBox to inject Datasouce instance 
	 (用IOC工具jBeanBox或Spring来注入上下文实例， 关于jBeanBox项目的中文说明见https://git.oschina.net/drinkjava2/jBeanBox)
```
	public static void main(String[] args) {
		SqlBoxContext ctx = BeanBox.getBean(CtxBox.class);//注入SqlBoxContext实例
		User u = ctx.create(User.class); //create an entity instance
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setAge(10);
		u.dao().save(); 
	 
	} 
```

Example 3 - Use default SqlBoxContext (利用缺省上下文简化编程)
```
	public static void main(String[] args) {
		SqlBoxContext.DEFAULT_SQLBOX_CONTEXT.setDataSource((DataSource) BeanBox.getBean(DataSourceBox.class));
		User u = new user();
		u.setUserName("User1");
		u.setAddress("Address1");
		u.setPhoneNumber("111");
		u.setAge(10);
		u.dao().save(); 	 
	}
 
```

Example 4 - Use JDBC (对SQL的完美支持，参数暂存在Threadlocal中，SQL执行时自动转化为preparedStatement)
```
    Dao.dao.execute("update users set username=?,address=? ", e("John", "Shanghai"), " where age=", q(30));  
    Dao.dao.execute("update users set", //  
            " username=?", e("Peter"), //  
            ",address=? ", e("Nanjing"), //  
            " where age=?", e(40));  
    Dao.dao.execute("update users set", //  
            " username=", q("Jeffery"), //  
            ",address=", q("Tianjing"), //  
            " where age=", q(50));  
    User.dao().execute("insert into users ", //  
            " (username", e("Andy"), //  
            ", address", e("Guanzhou"), //  
            ", age)", e("60"), //  
             SqlHelper.questionMarks());  
```

Example 5 - Batch insert (批量插入10000行数据，耗时~5秒,同样sql自动转为preparedStatement,不存在Sql注入问题)
```
    public void tx_batchInsertDemo() {  
        for (int i = 6; i < 100000; i++)  
            Dao.dao.cacheSQL("insert user (username,age) values(",q("user"+i)+",",q(60),")");  
        Dao.dao.executeCatchedSQLs();  
    }  
```

Example 5 - JDBC condition query (简洁地实现动态拼接SQL进行条件查询，不存在Sql注入安全问题)
```
public int conditionQuery(int condition, Object parameter) {//动态拼接SQL查询   
            User u = new User();   
            String sql = "Select count(*) from " + u.Table() + " where ";   
            if (condition == 1 || condition == 3)   
                sql = sql + u.UserName() + "=" + q(parameter) + " and " + u.Address() + "=" + q("Address1");   
       
            if (condition == 2)   
                sql = sql + u.UserName() + "=" + q(parameter);   
       
            if (condition == 3)   
                sql = sql + " or " + u.Age() + "=" + q(parameter);   
       
            return Dao.dao.queryForInteger(sql);   
        }   
       
        @Test   
        public void doJdbcConditionQuery() {   
            Assert.assertEquals(1, conditionQuery(1, "User1"));   
            Assert.assertEquals(0, conditionQuery(2, "User does not exist"));   
            Assert.assertEquals(1, conditionQuery(3, 10));   
            Assert.assertEquals(0, conditionQuery(3, 20));   
        }    
```

Example 6 - Transaction (事务支持, 利用了Spring的事明式事务)
```
	public void tx_insertUser() {
		User u = new User(); 
		u.setUserName("User2");
		u.setAddress("Address2");		 
		u.setAlive(true); 
		u.dao().save(); 
	} 

	public static void main(String[] args) {
		InsertTest t = BeanBox.getBean(InsertTest.class); //获取代理实例
		t.tx_insertUser(); //tx_开头的方法将包装在事务中
	}

```

Example 7 - Entity class 
           (来看一下实体类的写法，没有JPA注解，不继承任何基类，但是每个实体类中必须设一个dao属性,字段名必须是驼峰式写法，userName对应的数据库字段可为USER_NAME或username,自动匹配，无须手工调整)
```
public class User { 
	private Dao dao;

	public Dao dao() {
		if (dao == null)
			dao = Dao.defaultDao(this);
		return dao;
	} 

	public static final String Table = "users";

	public String Table() {
		return dao().tableName();
	}

	public static String Id = "id";

	private Integer id;

	public String Id() {
		return dao().columnName("id");
	}

	private String userName;
	public static final String UserName = "userName";

	public String UserName() {
		return dao().columnName(User.UserName);
	}

	public static final String PhoneNumber = "phoneNumber";
	private String phoneNumber;

	public String PhoneNumber() {
		return dao().columnName(PhoneNumber);
	}
	
	......
}

```

Example 8 - Configuration (User类数据库的表名和字段是可配置的，只要在User类同目录下放一个名为UserBox的类即可，配置实例可在运行期调用dao().getSqlBox获得并更改，这称为动态配置，与jBeanBox项目类似)
```
public class UserBOX extends SqlBox {
	{
		this.configTableName("usertable2");
		this.configColumnName(User.UserName, "User_Name_");
		this.configColumnName(User.Address, "Address2");
		this.configColumnName(User.PhoneNumber, "Phone");
	}	
}

```

更多功能如实体查询、关联查询、代理持久类生成、脏检查（透明持久化）、缓存等尚未完成，正在努力编码中，有兴趣的也欢迎加入。























 

