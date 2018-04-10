#### SQL写法： 带Connection参数  

这是DbUtils对JDBC的包装，手工负责连接的获取和关闭，在exceute、query等方法中要给出一个Connection参数，SqlBoxContext继承于DbUtils的QueryRunner的方法, 是DbUtils的QueryRunner类的子类，所以可以直接使用DbUtils的所有方法。
用法示例
```
    Connection conn = null;
    try {
      conn = ctx.prepareConnection();//或conn=dataSource.getConnection();
      ctx.execute(conn, "insert into users (name,address) values(?,?)", "Sam", "Canada");
      ctx.execute(conn, "update users set name=?, address=?", "Tom", "China");
      Assert.assertEquals(1L,
          ctx.queryForObject(conn, "select count(*) from users where name=? and address=?", "Tom", "China"));
      ctx.execute(conn, "delete from users where name=? or address=?", "Tom", "China");
    } catch (SQLException e) {
      doSomeThing(e);
    } finally {
      try {
        ctx.close(conn);//或conn.close();
      } catch (SQLException e) {
        doSomeThing(e);
      }
    }
```
如果在始初化SqlBoxContext时没有给定DataSource参数，则Connection必须要用dataSource.getConnection()等方法手工获得。
如果在始初化SqlBoxContext时给了一个DataSource实例作为参数，在运行期也可以用prepareConnection()方法来获取一个Connection实例。

DbUtils对Jdbc的包装还比较原始，这种用法有可能抛出SQLException异常，这是一个必须捕捉的Checked异常。