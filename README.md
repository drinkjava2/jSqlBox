(English version see "README-English.md") 
## jSqlBox (In Developing)
**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)  

jSqlBox是一个Java持久层工具,目标是用来代替目前常见的Hibernate/MyBatis/JdbcTemplate/DbUtils等持久层工具。jSqlBox的主要特点有:

1.模块化设计。jSqlBox将Jdbc工具、事务、方言和数据库脚本生成等功能分成子模块，由不同的子项目实现，每个子项目都可以脱离jSqlBox存在，甚至可供其它ORM工具使用，避免重复发明轮子。同时jSqlBox的各个子模块(除TinyNet外)在开发中大量借用了其它成熟项目，例如，方言模块(jDialects)抽取了Hibernate的70种方言，事务模块(jTranscations)在提供简化版声明式事务的同时兼容Spring事务，底层数据库访问工具(jDbPro)基于DbUtils，模板的引入则是受BeetlSql项目的启发。
2.与DbUtils兼容，基于DbUtils开发的项目可以无缝升级到jSqlBox，享受到jSqlBox提供的高级功能如模板支持、分页、JPA注解支持、数据库脚本生成、ActiveRecord等。
3.支持SQL写法最多。从最底层的JDBC到最新式的NoSQL式查询都有，是目前支持SQL写法最多的持久层工具。
4.源码最短。得益于模块化设计架构，jSqlBox项目的本体部分非常简单，仅由13个Java类组成。持久层的难点被分散到子模块中解决了。
5.多项技术创新，如Inline风格、动态配置、NoSQL自动越级查询及树结构查询等。

jSqlBox的开发目的是试图解决其它持久层的一些问题，主要有：
1.架构问题，其它持久层通常试图提供一篮子解决方案，但是不分模块的开发方式使得代码不可复用。例如Hibernate、MyBatis、jFinal、BeetlSql、JdbcTemplate等工具，都面临着如何处理跨数据库开发的问题，它们的做法要么就是自已从头到尾开始开发一套方言工具，要么就是干脆不支持数据库方言，没有借签其它工具的成果，也不考虑将自己的成果分享给其它工具。
2.不支持动态配置。从数据库表到Java对象，称为ORM,这需要进行配置。但这些常见的持久层工具，不支持动态配置，这对于需要动态生成或修改配置的场合是个重大缺陷。例如JPA注解或XML配置的实体Bean，在运行期很难更改外键关联关系、数据库表映射关系配置。 
3.过于偏执于某项技术，试图用一把锤子解决所有问题。例如Hibernate对象化设计过度，复杂臃肿。MyBatis的XML配置繁琐，没有提供CRUD方法。JdbcTemplate和DbUtils太偏重于SQL，开发效率低。BeetlSql项目偏重于模板。jFinal则偏好于链式写法和捆绑式提供服务，没有将持久层独立出来。

### 如何使用jSqlBox?
在项目的pom.xml中加入:
```
<dependency>
    <groupId>com.github.drinkjava2</groupId>
    <artifactId>jsqlbox</artifactId>
    <version>1.0.0</version> 
</dependency>
```

### jSqlBox的架构图解
在详细介绍jSqlBox之前，先看一下jSqlBox对各个子项目依赖关系，以及各个库的大小，对jSqlBox的架构有个粗略认识:

### jSqlBox快速入门

#### - 第一个jSqlBox示例: 数据源设定、上下文生成、创建数据库表格、插入和读取内容
```
public class HelloWorld {
	private String name;
	public String getName() {return name;}
	public void setName(String name) {this.name = name;}

	@Test
	public void doText() {
		HikariDataSource ds = new HikariDataSource();//边接池设定
		ds.setJdbcUrl("jdbc:h2:mem:DBName;MODE=MYSQL;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=0");
		ds.setDriverClassName("org.h2.Driver");//利用H2内存数据库来进行单元测试
		ds.setUsername("sa");
		ds.setPassword(""); 

		SqlBoxContext ctx = new SqlBoxContext(ds); //jSqlBox上下文生成
		ctx.setAllowShowSQL(true);//打开jSqlBox的日志输出
		String[] ddls = ctx.getDialect().toCreateDDL(HelloWorld.class);//数据库DDL脚本生成
		for (String ddl : ddls)
			ctx.nExecute(ddl);//新建数据库表

		HelloWorld hello = new HelloWorld();
		hello.setName("Demo");
		ctx.insert(hello);//插入一行记录
		Assert.assertEquals("Demo", ctx.nQueryForObject("select name from helloworld"));
		ds.close();//关闭连接池
	}
}
```
运行一下单元测试，没有报错，说明数据库已成功插入了一行数据。
以上示例利用了jDialects的功能创建了数据库脚本并在jSqlBox中运行，创建了数据库表。jDialects还支持主要的JPA注解，详情请见jDialects项目。
在实际开发环境中，往往用Spring或jBeanBox之类的IOC工具配置连接池单例和SqlBoxContext单例, 例如:
``` 
	SqlBoxContext ctx = BeanBox.getBean(CtxBox.class);//由IOC工具来获取SqlBoxContext单例 
```
利用IOC工具对各种常见数据库进行连接池的配置可详见单元测试的DataSourceConfig.java示例，因为我是jBeanBox的作者，所以用jBeanBox来演示，如果换成用Spring效果也是一样的。


#### - 分页
jSqlBox利用了jDialects进行跨数据库的(物理)分页，在出现SQL的地方，以下几种不同的写法都可做到分页查询：
```
ctx.nQuery(new MapListHandler(), SqlBoxContext.pagin(3, 5) + "select * from users");
ctx.nQuery(new MapListHandler(), pagin(3, 5) + "select * from users");//SqlBoxContext静态引入
ctx.nQuery(new MapListHandler(), ctx.paginate(3, 5, "select * from users"));
``` 

#### - 动态配置
```
		User u=new User();
		u.tableModel().setTableName("user_tb2");//动态改变映射数据表名为"user_tb2"
		u.tableModel().fkey().columns("teamId").refs("team", "id");//动态给teamId字段添加外键约束
		u.columnModel("id").pkey();//动态设定数据库表中的id列为主键
		u.box().setContext(ctx2);//动态设定新的上下文
		u.box().
		...	
```		
以上示例中User继承于ActiveRecord类，拥有box()等方法，可以在运行期动态访问并改变User类对应的配置。具体可详见jDialects项目。

#### - 事务
jSqlBox项目本身不提供事务服务，但是推荐使用jTransactions项目进行事务管理，这是一个包含了TinyTx和SpringTx两个事务实现的独立的事务服务工具。
如果是从DbUtils移植过来的旧项目，保持原来的事务运作方式即可，因为jSqlBox是与DbUtils兼容的。

#### - 以上是对jSqlBox的配置和使用简介，下面是要介绍的重点: jSqlBox支持的10种SQL写法:

SQL写法1 - DbUtils对JDBC的包装，手工负责连接的获取和关闭,获取连接用prepareConnection()方法，关闭连接用close(conn)方法。
           因为SqlBoxContext是DbUtils的QueryRunner类的子类，所以它拥有DbUtils的所有方法。
```
 		Connection conn = null;
		try {
			conn = ctx.prepareConnection();
			ctx.execute(conn, "insert into users (name,address) values(?,?)", "Sam", "Canada");
			ctx.execute(conn, "update users set name=?, address=?", "Tom", "China");
			Assert.assertEquals(1L,
					ctx.queryForObject(conn, "select count(*) from users where name=? and address=?", "Tom", "China"));
			ctx.execute(conn, "delete from users where name=? or address=?", "Tom", "China");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				ctx.close(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
```

SQL写法2 - DbUtils对JDBC的包装之二，无需关心连接的获取和关闭，使用这种方式要注意SqlBoxContext的生成必须提供一个dataSource构造参数。
```
		try {
			ctx.execute("insert into users (name,address) values(?,?)", "Sam", "Canada");
			ctx.execute("update users set name=?, address=?", "Tom", "China");
			Assert.assertEquals(1L,
					ctx.queryForObject("select count(*) from users where name=? and address=?", "Tom", "China"));
			ctx.execute("delete from users where name=? or address=?", "Tom", "China");
		} catch (SQLException e) {
			e.printStackTrace();
		}
```

SQL写法3 - nXxxx()方法，从jDbPro项目开始新加入的方法，不用再捕捉SqlException异常。SqlException转化为运行时异常抛出，这种方式通常与支持声明式事务的AOP工具如Spring联用(下同)。
```
		ctx.nExecute("insert into users (name,address) values(?,?)", "Sam", "Canada");
		ctx.nExecute("update users set name=?, address=?", "Tom", "China");
		Assert.assertEquals(1L,
				ctx.nQueryForObject("select count(*) from users where name=? and address=?", "Tom", "China"));
		ctx.nExecute("delete from users where name=? or address=?", "Tom", "China");
```

SQL写法4 - iXxxx()方法，Inline风格，在SQL里直接写参数,参数暂存在Threadlocal中,SQL执行时自动转化为preparedStatement,这种方式的优点是被赋值的字段和实际参数可以写在同一行上,便于维护，也利于根据不确定的条件动态拼接SQL。这是属于jSqlBox项目的创新。
```
			ctx.iExecute("insert into users (", //
					" name ,", param0("Sam"), //
					" address ", param("Canada"), //
					") ", valuesQuesions());
			param0("Tom", "China");
			ctx.iExecute("update users set name=?,address=?");
			Assert.assertEquals(1L, ctx
					.iQueryForObject("select count(*) from users where name=? and address=?" + param0("Tom", "China")));
			ctx.iExecute("delete from users where name=", question0("Tom"), " or address=", question("China"));
```
   以下是一种Inline风格的变种写法，实用性不大，但是挺有意思的:
```
		User user = new User("Sam", "Canada");
		user.setName("Sam");
		user.setAddress("Canada");
		ctx.iExecute("insert into users (", inline0(user, "", ", ") + ") ", valuesQuesions());
		user.setAddress("China");
		ctx.iExecute("update users set ", inline0(user, "=?", ", "));
		Assert.assertEquals(1L, ctx.iQueryForObject("select count(*) from users where ", inline0(user, "=?", " and ")));
		ctx.iExecute(param0(), "delete from users where ", inline(user, "=?", " or "));
```

SQL写法5 - tXxxx()方法, 模板风格，利用模板来存放SQL变量
```
		Map<String, Object> params=new HashMap<String, Object>();
		User sam = new User("Sam", "Canada");
		User tom = new User("Tom", "China");
		params.put("user", sam);
		ctx.tExecute(params,"insert into users (name, address) values(#{user.name},#{user.address})");
		params.put("user", tom); 
		ctx.tExecute(params,"update users set name=#{user.name}, address=#{user.address}");
		params.clear();
		params.put("name", "Tom");
		params.put("addr", "China");
		Assert.assertEquals(1L,
				ctx.tQueryForObject(params,"select count(*) from users where name=#{name} and address=#{addr}")); 
		params.put("u", tom);
		ctx.tExecute(params, "delete from users where name=#{u.name} or address=#{u.address}");
```

SQL写法6 - tXxxx()方法，模板风格和Inline风格的结合使用，可以看出来代码量明显减少
```
		user = new User("Sam", "Canada");
		put0("user", user);
		ctx.tExecute("insert into users (name, address) values(#{user.name},#{user.address})");
		user.setAddress("China");
		ctx.tExecute("update users set name=#{user.name}, address=#{user.address}" + put0("user", user));
		Assert.assertEquals(1L,
				ctx.tQueryForObject("select count(*) from users where ${col}=#{name} and address=#{addr}",
						put0("name", "Sam"), put("addr", "China"), replace("col", "name")));
		ctx.tExecute("delete from users where name=#{u.name} or address=#{u.address}", put0("u", user));
```

SQL写法7 - tXxxx()方法，依然是模板风格，但是切换成使用另一个模板"NamedParamSqlTemplate"，采用冒号来作参数分界符。
         jSqlBox是一种开放式架构设计，它对事务、日志、模板都有缺省实现，但是也支持在运行期动态切换成其它实现。
```
		user = new User("Sam", "Canada");
		ctx.setSqlTemplateEngine(NamedParamSqlTemplate.instance());
		put0("user", user);
		ctx.tExecute("insert into users (name, address) values(:user.name,:user.address)");
		user.setAddress("China");
		ctx.tExecute("update users set name=:user.name, address=:user.address" + put0("user", user));
		Assert.assertEquals(1L, ctx.tQueryForObject("select count(*) from users where ${col}=:name and address=:addr",
				put0("name", "Sam"), put("addr", "China"), replace("col", "name")));
		ctx.tExecute("delete from users where name=:u.name or address=:u.address", put0("u", user));
```

SQL写法8 - Data Mapper数据映射风格，与Hibernate中的写法一样。jSqlBox虽然鼓励实体类继承于ActiveRecord类，但对于纯POJO类也是支持的，即以下写法： 
```
		User user = new User("Sam", "Canada");
		ctx.insert(user);
		user.setAddress("China");
		ctx.update(user);
		User user2 = ctx.load(User.class, "Sam");
		ctx.delete(user2);
```

SQL写法9 - ActiveRecord风格，继承于ActiveRecord类的实体类，自动拥有insert/update/delete等CRUD方法和一个box()方法可用来在运行期更改配置。 
```
		System.out.println("=== ActiveRecord style  ===");
		user = new User("Sam", "Canada");
		user.box().setContext(ctx);// set a SqlBoxContext to entity
		user.insert();
		user.setAddress("China");
		user.update();
		user2 = user.load("Sam");
		user2.delete();
```
 以下是ActiveRecord风格的一个变种,使用这种写法的前提是必须在程序启动时调用SqlBoxContext.setDefaultContext(ctx)方法设定一个全局缺省上下文: 
```
		user = new User("Sam", "Canada").insert();
		user.setAddress("China");
		user.update();
		user2 = user.load("Sam");
		user2.delete();
```

SQL写法10 - 链式风格, 严格来说这种风格还是应该归类于ActiveRecord模式。
```
     链式风格之一：
	 new User().put("id","u1").put("userName","user1").insert(); 
	 
	 链式风格之二：
	 new Address().put("id","a1","addressName","address1","userId","u1").insert();
	 
	 链式风格之三：
	 new Email().putFields("id","emailName","userId");
	 new Email().putValues("e1","email1","u1").insert();
	 new Email().putValues("e2","email2","u1").insert();	 
```
如果大量运行插入、更新语句，可以在开始前调用ctx.nBatchBegin()方法，结束后调用ctx.nBatchEnd方法，则插入方法将自动汇总成批量操作，不过使用此功能的前提是数据库要支持批量操作功能。

以上是常规的一些SQL写法，下面要介绍的是jSqlBox项目支持的NoSql式查询方法，对于下图的一组关系数据库表格，如果用SQL来进行多表关联查询将比较麻烦，jSqlBox中包含了一个TinyNet子项目，专门用于将关系数据库转化为内存中的图结构，从而可以运用NoSql式查询方法，在节点间浏览查询。

NoSql查询示例1 一 查找u1和u2两个用户所具有的权限
```
		TinyNet net = ctx.netLoad(new User(), new Role(), Privilege.class, UserRole.class, RolePrivilege.class);
		Set<Privilege> privileges = net.findEntitySet(Privilege.class,
				new Path("S-", User.class).where("id=? or id=?","u1","u2").nextPath("C-", UserRole.class, "userId")
						.nextPath("P-", Role.class, "rid").nextPath("C-", RolePrivilege.class, "rid")
						.nextPath("P+", Privilege.class, "pid"));
		for (Privilege privilege : privileges)
			System.out.print(privilege.getId()+" ");
			
	  输出结果：p1 p2 p3 
```
说明:
netLoad方法将根据所给参数，调入所在表的所有内容，在内存中拼成TinyNet实例代表的图状结构。注意TinyNet不是线程安全类，要小心使用。  
与netLoad方法类似的一个方法是netLoadSketch方法，不是将全表加载，而是只加载所有主键、外键字段，从而提高性能和节约内存。 	 
Path用于定义一个查找路径，第一个参数由两个字母组字，首字母S表示查找当前节点，P表示查找父节点，C表示查找子节点。第二个字母"-"表示查找的中间结果不放入输出，"+"则放入输出, "*"表示递归查找所有节点。第二个参数定义查找对象类或对象数据库表，第三个参数为可变参数，定义两个表之间的关联字段，例如User和UserRole之间的关联字段为"userId"。  
Path的where()方法定义一个表达式，用于判断节点是否可以选中，表达式支持的关键字有: 当前实体的所有字段名和 > < = >= <= + - * / not null equals contains 等常见字符串函数。  
Path的nextPath()方法用于定义下一个查找路径。

NoSql查询示例2 一 自动越级查询。示例1的写法比较啰嗦，下面是等价写法，用autoPath方法可以自动帮我们计算出搜索路径：
```
		TinyNet net = ctx.netLoad(new User(), new Role(), Privilege.class, UserRole.class, RolePrivilege.class);
		Set<Privilege> privileges = net.findEntitySet(Privilege.class,
				new Path(User.class).where("id='u1' or id='u2'").autoPath(Privilege.class));
		for (Privilege privilege : privileges)
			System.out.print(privilege.getId()+" ");
```
注意autoPath方法仅适用于查找路径唯一的情况，如果从User到Privilege有多个不同路径通达，就不能应用autoPath方法，而必须写出全路径。

NoSql查询示例3 一 Java原生方法判断。以下示例查询Email "e1"和"e5"所具有的权限:
      用Java原生方法进行节点的判断，优点是可以采用熟悉的Java语言，而且支持实体字段的重构。
```
	@Test
	public void testAutoPath2() {
		insertDemoData();
		TinyNet net = ctx.netLoad(new Email(), new User(), new Role(), Privilege.class, UserRole.class,
				RolePrivilege.class);
		Set<Privilege> privileges = net.findEntitySet(Privilege.class,
				new Path(Email.class).setValidator(new EmailValidator()).autoPath(Privilege.class));
		for (Privilege privilege : privileges)
			System.out.println(privilege.getId());
		Assert.assertEquals(1, privileges.size());
	}

	public static class EmailValidator extends DefaultNodeValidator {
		@Override
		public boolean validateBean(Object entity) {
			Email e = (Email) entity;
			return ("e1".equals(e.getId()) || "e5".equals(e.getId())) ? true : false;
		}
	}
```

NoSql示例4 一 手工加载。 TinyNet的加载也可以手工进行，而不是用netLoad方法全表调入，例如下表运行两个SQL，分两次加载数据库并合并成一个TinyNet实例：
```
		List<Map<String, Object>> mapList1 = ctx.nQuery(new MapListHandler(netProcessor(User.class, Address.class)),
				"select u.**, a.** from usertb u, addresstb a where a.userId=u.id");
		TinyNet net = ctx.netCreate(mapList1);
		Assert.assertEquals(10, net.size());

		List<Map<String, Object>> mapList2 = ctx.nQuery(new MapListHandler(),
				netConfig(Email.class) + "select e.** from emailtb as e");
		ctx.netJoinList(net, mapList2);
		Assert.assertEquals(15, net.size());
```	

NoSql查询示例5 一 树结构的查询，对于Adjacency List模式存储的树结构数据表，利用NoSQL查询非常方便，例如如下数据库表格：

查询B节点和D节点的所有子节点(含B和D节点本身)：
```
		TinyNet net = ctx.netLoad(TreeNode.class);
		Set<TreeNode> TreeNodes = net.findEntitySet(TreeNode.class,
				new Path("S+", TreeNode.class).where("id=? or id=?", "B", "D").nextPath("C*", TreeNode.class, "pid"));
		for (TreeNode node : TreeNodes)
			System.out.print(node.getId() + " ");
		输出: B D E F H I J K L 
```

查询F节点和K节点的所有父节点(不含F和K节点本身)：
```
		TinyNet net = ctx.netLoad(TreeNode.class);
		Set<TreeNode> TreeNodes = net.findEntitySet(TreeNode.class,
				new Path("S-", TreeNode.class).where("id='F' or id='K'").nextPath("P*", TreeNode.class, "pid"));
		for (TreeNode node : TreeNodes)
			System.out.print(node.getId() + " ");
		输出: B H A D 
```
对于海量数据的大树，用netLoad方法调入全表到内存再用NoSQL方法查询是不现实的，这时可利用数据库的递归查询功能将一棵子树查找到内存中再进行NoSQL方法查询。
如果不支持递归，也可参照本人发明的无限深度树方案的第三种方案（见http://drinkjava2.iteye.com/blog/2353983），用一条SQL调入所需要的子树后再进行查询。  
 
 以上即为jSqlBox的初步介绍，欢迎大家试用和挑错。 详细的PDF版使用手册正在制作中，如有兴趣的话，可参考本介绍和单元测试代码来学习和了解本项目。








