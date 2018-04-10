#### SQL写法： DataMapper风格

DataMapper风格类似Hibernate的session.save(pojo)这种写法，POJO类本身不带CRUD方法，而是由一个Manager类来对这个POJO进行操作。在jSqlBox中，这个Manager类就是SqlBoxContext, 有insert/update/delete/load等方法，可以根据POJO类的配置(见jDialects项目)创建出插入/更新/删除等SQL并执行。从易用性方面考虑，jSqlBox建议实体类继承于ActiveRecord类，但是对于纯粹的POJO类，jSqlBox也是支持的，因为有时候POJO类因为某些原因，不能继承于ActiveRecord类，这时候DataMapper式写法就可以用上了：
```Java
    User user = new User("Sam", "Canada");
    ctx.insert(user);
    user.setAddress("China");
    ctx.update(user);
    User user2 = ctx.load(User.class, "Sam");
    ctx.delete(user2);
```
SqlBoxContext是无会话(Sessionless)的轻量级对象，实体Bean总是无状态的，不分什么VO、PO、DTO之类的，从View层传来的Form Bean可以直接用insert方法存到数据库，从数据库load出来的实体bean可以直接传递到view层。  

DataMapper风格的缺点是只能针对单个记录进行CRUD操作。