#### SQL写法: ActiveRecord风格

继承于ActiveRecord类或实现了ActiveRecordSupport接口的实体类，将拥有insert/update/delete等CRUD方法。ActiveRecord模式的优点是基于实体类操作数据库, 支持实体字段名重构，但它的缺点是只能对一行数据记录进行CRUD操作，不能实现复杂的逻辑。  
当调用insert方法时，将根据实体的主键生成方式自动创建主键，jSqlBox不去判断数据库中是否已经有同值的主键存在。
当调用update方法时，将根据实体的主键(可以是复合主键)，更新数据库中对应记录的其它列内容。
当调用delete方法时，将根据实体的主键(可以是复合主键)，删除数据库中对应的记录。  
```
User user = new User(); //User类继承于ActiveRecord类
user.useContext(ctx); // 设定一个SqlBoxContent
user.setName("Sam");
user.setAddress("Canada");
user.insert();
user.setAddress("China");
user.update();
User user2 = user.load("Sam");
user2.delete();
```


对于继承于ActiveRecord的User类来说，要使用ActiveRecord的insert等方法，还必须通过user.useContext()方法来设定一个SqlBoxContent，这有点麻烦，所以通常的做法是如下:  

在程序启动时调用静态方法SqlBoxContext.setGlobalSqlBoxContext(ctx)方法设定一个全局缺省上下文，当一个ActiveRecord实体没有设定任何SqlBoxnContext实例时，就默认使用这个全局缺省上下文，这样一来在业务方法里就完全看不到持久层工具的影子了：
```Java
    user = new User("Sam", "Canada").insert();
    user.setAddress("China");
    user.update();
    user2 = user.load("Sam");
    user2.delete();
```
这种用法是ActiveRecord模式的特例，适用于单数据源的场合，当用到多个数据源在同一个项目中时，例如同一个对象要存储到不同的数据库里，还是必须用entity.useContext(ctx)方法在运行期动态切换它的SqlBoxContext。

