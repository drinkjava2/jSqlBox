### NoSQL查询

jSqlBox中包含了一个名为EntityNet的小模块，专门用来进行对象间的关联查询，它先将关系数据库用普通的SQL查询调入内存中，然后转化为对象间互相关联的图结构，从而可以运用NoSQL式查询方法，在节点间浏览查询。  

对于下图的一组关系数据库表格，User与Role是多对多关系，Role与Privilege是多对多关系，UserRole和RolePrivilege是两个中间表，用来连接多对多关系。如果用普通的SQL进行多表关联查询比较麻烦，而用NoSQL式查询则非常简单明了。  
![image](https://gitee.com/drinkjava2/jSqlBox/raw/master/orm.png "orm")  

#### NoSQL查询示例1 一 自动越级查询  
查找u1和u2两个用户所具有的权限:  
```Java
EntityNet net = ctx.netLoad(new User(), new Role(), Privilege.class,
       UserRole.class, RolePrivilege.class);
Set<Privilege> privileges = net.findEntitySet(Privilege.class,
       new Path(User.class).where("id='u1' or id='u2'").autoPath(Privilege.class));
for (Privilege privilege : privileges)
      System.out.print(privilege.getId() + " ");
 //输出结果：p1 p2 p3 
```
给定一个起点new Path(User.class)，给定一个终点autoPath(Privilege.class)，EntityNet将自动越级查找出所有关联的终点节点。注意autoPath方法仅适用于查找路径唯一的情况，如果从User到Privilege有多个不同路径通达，就不能应用autoPath方法，而必须写出全路径。 

#### NoSQL查询示例2 一 按全路径查询  
和上例一样，查找u1和u2两个用户所具有的权限，但这次手工给出搜索全路径：
```Java
    EntityNet net = ctx.netLoad(new User(), new Role(), Privilege.class, UserRole.class, RolePrivilege.class);
    Set<Privilege> privileges = net.findEntitySet(Privilege.class,
        new Path("S-", User.class).where("id=? or id=?","u1","u2").nextPath("C-", UserRole.class, "userId")
            .nextPath("P-", Role.class, "rid").nextPath("C-", RolePrivilege.class, "rid")
            .nextPath("P+", Privilege.class, "pid"));
    for (Privilege privilege : privileges)
      System.out.print(privilege.getId());
```
详细说明:
* netLoad方法将根据所给参数，调入所在表的所有内容，在内存中拼成EntityNet实例代表的图状结构。因为netLoad方法是全表加载，当表行数多时，不应当使用这个方法，而应该手工利用SQL查询细粒度地选择性加载以提高性能，见下面手工加载一节。注意EntityNet不是线程安全类，在多线程环境下不同的线程修改同一个EntityNet实例时要小心。  
* 与netLoad方法类似的一个方法是netLoadSketch方法，不是加载所有字段，而是仅加载整个表的主键和外键字段，这种懒加载可以提高性能。  
* Path用于定义一个查找路径，构造器的第一个参数由两个字母组成，首字母S表示查找当前节点，P表示查找父节点，C表示查找子节点。第二个字母"-"表示查找的中间结果不放入查询结果，"+"则放入查询结果，"*"表示递归当前路径查找所有节点并放入查询结果。第二个参数定义目标对象类、类实例或数据库表名，第三个参数为可变参数，为两个目标之间的关联属性或关联列(即单外键或复合外键的列名，或实体类的字段名也可)，例如User和UserRole之间的关联字段为"userId"。第一个参数如果省略的话默认为"S-"类型。  
* Path的where()方法定义一个表达式，用于判断节点是否可以选中，表达式支持的关键字有: 当前实体的所有字段名、>、 <、 =、 >、=、 <=、 +、 -、 *、/、null、()、not 以及以下字符串函数：equals、equalsIgnoreCase、contains、containsIgnoreCase、startWith 、startWithIgnoreCase、endWith、endWithIgnoreCase，以及一个问号占位符，用于防止类似SQL注入之类的入侵。  
* Path的nextPath()方法用于定义下一个查找路径。 

#### NoSQL查询示例3 一 Java原生方法判断。  
用Java原生方法进行节点的判断，可以利用熟悉的Java语言而且支持实体字段的重构。以下示例查询Email "e1"和"e5"所关联的权限:
```Java
  @Test
  public void testAutoPath2() {
    insertDemoData();
    EntityNet net = ctx.netLoad(new Email(), new User(), new Role(), Privilege.class, UserRole.class,
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

#### NoSQL示例4 - 手工加载  
目前jSqlBox还做不到直接根据Path在数据库中查询，而是需要用netLoad方法进行全表载入之后再查询，这是非常占用资源的，为了提高效率，EntityNet的加载可以手工按SQL查询进行按需加载。SQL可以是多表关联查询，也可以一个表一个表地加载，例如下例运行两个SQL，分两次查询数据库并将结果合并成一个EntityNet实例，EntityNet会自动根据数据库的主键、外键值将这些记录拼成内存中的图结构：
```Java
List<Map<String, Object>> mapList1 = ctx.nQuery(new EntitySqlMapListHandler(User.class, Address.class),
	 "select u.**, a.** from usertb u, addresstb a where a.userId=u.id");

 EntityNet net = ctx.netCreate(mapList1);
 Assert.assertEquals(10, net.size());

 Email e = new Email();
 e.alias("e");
 List<Map<String, Object>> mapList2 = ctx.nQuery(new EntitySqlMapListHandler(e),
				"select e.id as e_id from emailtb as e");
 ctx.netJoinList(net, mapList2);
 Assert.assertEquals(15, net.size());
```
上例中两个星号这种写法是jSqlBox项目中唯一打破标准SQL写法的地方，它表示查询User对象所有非transient属性所对应的数据库表字段。如果不使用两个星号这种非标字符，也可以手工写这样的SQL: "select u.id as u_id, u.name as u_name ..."，效果是一样的，用手写可以精准控制哪些字段加载，哪些不加载，只不过要打很多字，麻烦了一点。  
netCreate和netJoinList方法接收一个List<Map<String, Object>>类型的参数，用来转化为EntityNet实例。对于并非从数据库查询来的List<Map<String, Object>>类型的数据集也同样接受，但后者必须额外在方法参数里提供实体类、实体或TableModel实例作为配置参数。  

#### NoSQL查询示例5 一 树结构的查询  
对于Adjacency List模式存储的树结构数据表，利用NoSQL查询非常方便，例如如下数据库表格：  
![image](https://gitee.com/drinkjava2/jSqlBox/raw/master/tree.png "tree")  
查询B节点和D节点的所有子节点(含B和D节点本身)：
```Java
    EntityNet net = ctx.netLoad(TreeNode.class);
    Set<TreeNode> TreeNodes = net.findEntitySet(TreeNode.class,
        new Path("S+", TreeNode.class).where("id=? or id=?", "B", "D").nextPath("C*", TreeNode.class, "pid"));
    for (TreeNode node : TreeNodes)
      System.out.print(node.getId() + " ");
    输出: B D E F H I J K L 
``` 

查询F节点和K节点的所有父节点(不含F和K节点本身)：  
```Java
    EntityNet net = ctx.netLoad(TreeNode.class);
    Set<TreeNode> TreeNodes = net.findEntitySet(TreeNode.class,
        new Path("S-", TreeNode.class).where("id='F' or id='K'").nextPath("P*", TreeNode.class, "pid"));
    for (TreeNode node : TreeNodes)
      System.out.print(node.getId() + " ");
    输出: B H A D 
```
对于海量数据的大树，用netLoad方法调入全表到内存再用NoSQL方法查询是不现实的，这时可利用数据库的递归查询功能将几棵子树查找到内存中再用NoSQL方法查询。如果数据库不支持递归或有性能问题，可考虑采用本人发明的无限深度树存储方案，可以仅用一条SQL高效查询出所需要的子树调入内存中: [见这里](https://my.oschina.net/drinkjava2/blog/828781)。

#### NoSQL查询示例6 - 返回不同的类型  
见下例，使用findEntityMap方法，可以一次查询出不同类型的实例返回:
```
EntityNet net = ctx.netLoad(new User(), new Email());
Map<Class<?>, Set<Object>> result = net
  .findEntityMap(new Path("S+", Email.class).nextPath("P+", User.class, "userId"));
System.out.println("User selected:" + result.get(User.class).size());
System.out.println("Email selected:" + result.get(Email.class).size());
```
注意上例中的+号表示查询出的结果要放在返回Map中，结果放在一个Map结构中，键为类的类型，这个结构可能不令人满意。（目前正在开发在Path上设定将查出来的值绑定到指定上级对象的指定属性上，这个功能正在开发中，预计在下一个提交实现。)  

#### NoSQL查询示例7 - 开启EntityNet查询缓存 
```
int sampleSize = 30;
int queyrTimes = 100;
for (int i = 0; i < sampleSize; i++) {
    new User().put("id", "usr" + i).put("userName", "user" + i).insert();
    for (int j = 0; j < sampleSize; j++)
        new Email().put("id", "email" + i + "_" + j, "userId", "usr" + i).insert();
}
EntityNet net = ctx.netLoad(new User(), Email.class);
net.setAllowQueryCache(true);//开启EntityNet查询缓存
Map<Class<?>, Set<Node>> result = null;
for (int i = 0; i < queyrTimes; i++)
    result = net.findNodeMapByEntities(new Path("S+", User.class).nextPath("C+", Email.class, "userId"));
}
System.out.println("user selected2:" + result.get(User.class).size());
System.out.println("email selected2:" + result.get(Email.class).size());
```
加载数据库内容并在内存中构造一个EntityNet是一个比较耗资源的工作，如果能够用普通SQL查询解决的问题，尽量不要滥用NoSQL查询，只有当对象关联关系非常复杂的时候，才需要考虑引入NoSQL查询以简化编程。对于经常用到的EntityNet，可以考虑将其作为全局缓存以避免频繁读取数据库，并开启jSqlBox的查询缓存或EntityNet的查询缓存以提高性能(这两种查询缓存是不一样的，jSqlBox的查询缓存是在运行SQL之前判断是否同样的SQL查询已经在缓存里保存了结果，是针对SQL的，而EntityNet的查询缓存用于判断相同的节点遍历查询条件下的查询结果是否已有缓存，是针对节点的)。EntityNet的查询缓存非常占用内存，而且只要是缓存就会有脏数据现象，所以EntityNet的查询缓存默认是关闭的，必须手工用setAllowQueryCache方法为每个EntityNet打开缓存功能。即使打开了查询缓存，在细粒度上对于每一层搜索路径，也可以用setCacheable(false)去禁用查询缓存，如以下语句等同于没有打开缓存功能：  
net.findNodeMapByEntities(new Path("S+", User.class).setCacheable(false).nextPath("C+", Email.class, "userId"));  

#### 关于NoSQL配置　　  
对了，可能有人已经注意到，说了这么多例子，在其它ORM工具中常见的什么一对多、多对一、多对多之类的配置在哪里? 答案是：没有。  
开个玩笑，还是有一些的，如果你打开jSqlBox的单元测试源码，可以看到UserRole类的代码是这样子的:
```
@Table(name = "userroletb")
public class UserRole extends ActiveRecord {
	@Id
	@UUID25
	String id;

	@SingleFKey(refs = { "usertb", "id" })
	String userId;
```
看到@SingleFKey注解没有? 这个就是多对一配置了(还有一个FKey注解也是干同样的事，但是功能更强，支持复合外键)，表示UserRole的userId字段有一个数据库外键，参考usertb数据库表的id主键字段。外键就是关系，这是为什么关系数据库被称为关系数据库，关系数据库只存在"多对一"这种唯一的关联，所以jSqlBox中没有什么多对多、一对多的配置，而是完全借用了数据库的主键、外键配置。jSqlBox支持复合主键和复合外键(配置方法详见jDialects项目)。jSqlBox中的NoSQL查询是数据库一比一的映射，不存在一对多这种数据结构，如果业务需求要求返回一个对象的属性是一个集合，这种情况必须手工在Path上指定查出来的值绑定到哪个上级对象的哪个属性上(还未实现)。
  
jSqlBox支持动态配置，例如可以在运行期动态地添加或删除实体的外键约束配置(每个实体实例的配置都是实体类的配置的副本，只要不调用toCreateDDL方法生成脚本并执行，对实际数据库没影响)，也就是等同于创建/删除对象间的关联，然后就可以用NoSQL方式查询了，例如，如果在运行期调用如下语句：  
userRole.columnModel("userId").singleFKey("emailtb","id");   
就建立起了userRole的userId字段和Email的id字段的的外键约束关系了，也就等同于实体间的关联关系了，然后就可以将userRole或userRole.tableModel()作为参数去加载EntityNet了：  
EntityNet net =  ctx.netLoad(userRole, Email.class);  
netLoad方法参数和EntityNetHandler的构造参数允许接收实体类、实体实例、TableModel实例作为参数，用于告诉EntityNet实体间的关联关系。EntityNet一旦被创建好了，就与jSqlBox没有关系了，它是一个内存中的独立的图结构对象，有自已的一套查询、修改方法。  

注解配置是一种固定配置，因为它在程序未运行前就已经存在了。如果只是想配置实体间的关系，而不想在输出DDL时真的生成外键约束脚本，可以在注解@SingleFKey或@FKey中设置ddl=false即可，或是实体上不配置外键，而是每次运行期动态创建一个外键约束，显然第一种方法更方便一点，而第二种方法更显活一点。