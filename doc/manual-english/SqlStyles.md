jSqlBox inherits from the DbUtils kernel, the sessionless architecture design, making it support a variety of SQL writing, from the bottom of the manual JDBC operations to the top level ActiveRecord and NoSql-style object association query, from the SQL writing point of view, it can be said to be a "full stack" of persistence tools.
 
SQL writing 1 - with the Connection parameter, manually responsible for connection acquisition and closure, which is the SqlBoxContext inherited from the QueryRunner DbUtils method.
```Java
    Connection conn = null;
    Try {
      Conn = ctx.prepareConnection();
      Ctx.execute(conn,
         "insert into users (name,address) values(?,?)", "Sam", "Canada");
    } Catch (SQLException e) {
       doSomething(e);
    } finally {
      Try {
        Ctx.close(conn);
      } Catch (SQLException e) {
        doSomething(e);
      }
    }
```

SQL Writing 2 - No Connection parameter, also a QueryRunner that inherits from DbUtils. Do not need to care about connection acquisition and closure, use this way to pay attention to the SqlBoxContext generation must provide a dataSource constructor parameters.
```Java
    Try {
      Ctx.execute("insert into users (name,address) values(?,?)", "Sam", "Canada");
    } Catch (SQLException e) {
        doSomething(e);
    }
```

SQL method 3 - nXxxx () method, starting from the jDbPro module to join the new method, no longer catch SqlException exception. The SqlException exception is thrown as a RuntimeException exception, which is often used in conjunction with AOP tools that support declarative transactions such as Spring (see the jBooox and jsqlbox-spring demo examples in the demo for details).
```Java
    ctx.nExecute("insert into users (name,address) values(?,?)", "Sam", "Canada");
```
 
SQL writing 4 - iXxxx () method, SQL embedded parameters (Inline style), directly write parameters in SQL, parameters temporarily exist in Threadlocal, SQL automatically converted into preparedStatement, this method has the advantage of being assigned fields and The actual parameters can be written on the same line. When there are many fields, it is easy to maintain, and it is also convenient to dynamically assemble SQL according to uncertain conditions. This is a technical innovation that began with jSqlBox.
```Java
ctx.iExecute("insert into users (", //
  " name ,", param0("Sam"), //
  " address ", param("Canada"), //
  ") ", valuesQuesions());
```
 
SQL Writing 5 - tXxxx() method, template style, using templates to store SQL variables:
```Java
    Map<String, Object> params=new HashMap<String, Object>();
    User sam = new User("Sam", "Canada")
    Params.put("user", sam);
    ctx.tExecute(params,
       "insert into users (name, address) values(#{user.name},#{user.address})");
```
The jSqlBox template is replaceable. It comes with two templates, one that uses the #{} placeholder and one that uses: the placeholder. And there is also an example demo template that uses BeetlSql in the demo directory.
 
SQL writing 6 - xXxxx() method, a combination of template style and Inline style:
```Java
    Put0("user", new User("Sam"));
    ctx.xExecute("insert into users (name) values(#{user.name})");
```
 
SQL writing 7 - Data Mapper data mapping style, and Hibernate in the same way. From a performance perspective, jSqlBox suggests that entity classes inherit from the ActiveRecord class, but for pure POJO classes, jSqlBox is also supported.
```Java
    User user = new User("Sam", "Canada");
    Ctx.insert(user);
```
 
SQL writing 8 - ActiveRecord style, inherited from the ActiveRecord class entity class, automatically has CRUD methods such as insert/update/delete.
```
    User = new User();
    user.setName("Sam");
    user.useContext(ctx); // set a SqlBoxContext to entity
    User.insert();
```
 
The following is a variant of the ActiveRecord style. The premise of using this method of writing is that you must call the SqlBoxContext.setDefaultContext(ctx) method when the program starts to set a global default context, which is suitable for a single data source. The advantage of this type of writing is that the persistence layer's shadow cannot be seen in the business method at all:
```Java
    User user = new User("Sam", "Canada").insert();
```
 
SQL writing 9 - chain style, strictly speaking this style should still be categorized in ActiveRecord mode:
```Java
   One of the chain styles:
   New User().put("id","u1").put("userName","user1").insert();
   
   The chain style two:
   New Address().put("id","a1","addressName","address1","userId","u1").insert();
   
   Chain style three:
   New Email().putFields("id","emailName","userId");
   New Email().putValues("e1","email1","u1").insert();
   New Email().putValues("e2","email2","u1").insert();
```
 
SQL Writing 10 - SqlMapper style
One of the SqlMapper styles: Using SQL annotations
```
@Handler(MapListHandler.class)
@Sql("select * from users where name=? and address=?")
Public List<Map<String, Object>> selectUsers(String name, String address) {
    Return guess(name, address);
};
//In the main program:
List<Map<String, Object>> users = new User().selectUsers("Tom", "China");
```
The guess method is passed to jSqlBox to automatically run and assemble the objects that need to be returned according to the @SQL and @Handler annotations.
 
SqlMapper style two: use Java multi-line text
```
Public List<User> selectUsersByText2(String name, String address) {
   Return ctx().nQuery(new EntityListHandler(User.class), guessSQL(), name, address);
}
/*-
Select u.**
From
Users u
Where
U.name=? and address=?
*/
//In the main program:
List<User> users2 = new User().selectUsersByText2("Tom", "China");
```
Using Java's /*- annotations to implement multi-line text support, this is a black technology, and the order of the generation is to put the Java entity class source code in the Resource directory instead of the src directory and configure it in Maven. The guessSQL() method automatically determines the SQL text of this method, whether it is a Sql annotation or multiple lines of text. The advantage of implementing multiline text support in Java is that you can use the IDE to quickly locate SQL text.

SqlMapper style: abstract class instantiation
```
Public abstract List<User> selectUsersByText(String name, String address);
/*-
Select u.**
From
Users u
Where
U.name=? and address=?
*/
//In the main program:
AbstractUser user = ActiveRecord.create(AbstractUser.class);
List<User> users2 = user.selectUsersByText("Tom", "China");
```
This is purely a test of black technology that is more than practical. If it is more troublesome to say that there are many lines of text, this is not even guaranteed to work. It is not only necessary to place the source code of the Java abstract class in the Resource directory, but also to use dynamic compilation techniques. It is not guaranteed that all Java environments can be compiled (tested only on Tomcat/WebLogic). The generated user instance is a subclass of AbstractUser, not a proxy class.

SqlMapper style four: interface class instantiation, define an interface, return a proxy class (under consideration)
```
Public List<User> selectUsersByText(String name, String address);
/*-
Select u.**
From
Users u
Where
U.name=? and address=?
*/
//In the main program:
User user = ActiveRecord.create(AbstractUser.class);
List<User> users2 = user.selectUsersByText("Tom", "China");
```

This function is not implemented in jSqlBox. There is no problem from a technical point of view. Spring and MyBatis have already been implemented, but for me, one is because they are too busy. Second, I don't like it too much. The only advantage of this method is that it is omitted. The method body "return guess (parameter);", with a few words, brought a lot of problems, breaking the concept of combining "ActiveRecord" with "SqlMapper", when dealing with complicated mapping, annotation configuration Or XML configuration has allegedly plagued itchy, the easiest way is to go shirtless, in the body of the method to assemble the object, but the problem is that the interface does not support method body.

SQL Writing 11 - NoSQL Query
NoSQL is suitable for queries where the object association is complex and where ordinary SQL is difficult to query (for example, querying a tree structure).
For a group of relational database tables in the figure below, User and Role are in a many-to-many relationship. Role and Privilege are in a many-to-many relationship. UserRole and RolePrivilege are two intermediate tables used to connect many-to-many relationships. If using ordinary SQL for multi-table association query is too much trouble, jSqlBox contains a TinyNet subproject, which is specially used to convert the relational database into the in-memory graph structure, so that the NoSql-type query method can be used to browse queries between nodes.  
![image](../blob/master/orm.png?raw=true)  
For example, to find out which Privileges u1 and u2 correspond to, use the following two lines of code:
```Java
TinyNet net = ctx.netLoad(User.class, Role.class, Privilege.class, UserRole.class,
     RolePrivilege.class);
Set<Privilege> privileges = net.findEntitySet(Privilege.class,
     New Path(User.class).where("id='u1' or id='u2'").autoPath(Privilege.class));
```
jSqlBox does not have the usual one-to-one, one-to-many association mapping configuration, but the use of database foreign key configuration combined with NoSQL query to achieve the query between the objects of the tour, and is read-only. 