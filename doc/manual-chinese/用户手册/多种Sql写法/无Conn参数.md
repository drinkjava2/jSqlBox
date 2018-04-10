#### SQL写法： 无Connection参数

这种用法也是继承于DbUtils的QueryRunner, 在execute、query等方法中不放入Connection参数，使用这种用法时必须在构造SqlBoxContext实例时提供一个DataSource参数，
```Java
    DataSource dataSource=....;
    SqlBoxContext ctx=new SqlBoxContext(dataSource);
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
这种用法程序员必须手工捕捉SQLException并处理, 根据业务要求来决定是否要重新抛出一个运行期或业务类异常。

注意：当没有对SqlBoxContext进行事务配置时，每一个SQL方法如execute、query等都工作在自动提交模式，也就是说，各个SQL方法是互相独立的，没有被事务保护，适用于只读或不重要的场合。当要求各个方法工作在同一个事务中时，必须在创建SqlBoxContext实例时进行它的声明式事务配置，详见右侧的“事务配置"链接。