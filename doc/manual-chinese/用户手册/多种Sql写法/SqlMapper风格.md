#### SQL写法: SqlMapper风格 

与MyBatis等工具将@SQL注解放在接口的抽象方法上不同，在jSqlBox中，SqlMapper是与ActiveRecord合体的，只能在ActiveRecord的子类（或抽象子类）上实现SqlMapper配置，这种做法可以将继承来的简单的CRUD方法、SqlMapper方法和其它业务方法统一到同一个实体类上去，便于实现传说中的充血模型。
```
public class User extends ActiveRecord{
  //getters &setters

  @Handler(MapListHandler.class)
  @Sql("select * from users where name=? and address=?")
  public List<Map<String, Object>> selectUsers(String name, String address) {
      return guess(name, address);
  };
}
//主程序中：
List<Map<String, Object>> users = new User().selectUsers("Tom", "China");
```
上面的guess方法是由ActiveRecord提供的，用来根据当前方法的参数、SQL配置、Handler类配置，自动运行SQL并装配成需要的结果返回。 
　　  
　　  
SqlMapper风格之二: 使用Java多行文本
```
public class User extends ActiveRecord{ 
 public List<User> selectUsers(String name, String address) {
    return guess(name, address);
 }
	/*-
	   select u.** 
	   from 
	   users u
	      where 
	         u.name=? and address=?
	 */

  public PreparedSQL updateUserPreSql(String name, String address) {
     return this.guessPreparedSQL(name, address);
  };
	/*- 
	 update users 
	      set name=?, address=?
	*/
}
//主程序中：
List<User> user = new User().selectUsers("Tom", "China");
user.ctx().nUpdate(user.updateAllUserPreSql("Tom", "China"));
```
上例中如果不用guess方法，也可以手工执行SQL并返回结果，如用下行来代替效果是一样的：  
  return this.ctx().nQuery(new EntityListHandler(User.class), guessSQL(), name, address);  
ctx方法是ActiveRecord自带的，用来获取当前ActiveRecord的SqlBoxContext实例。  
guessSQL方法可以用来获取当前方法的SQL文本，可以是@Sql注解中的或多行文本中的，如果两个都有，以@Sql中的为准，这个方法返回的仅仅是一个字符串文本，不包含参数。当然也可以利用它来放一些非SQL的多行文本如XML等。  
guessPreparedSQL方法用来返回一个将当前方法的SQL文本、参数、Handler类打包好的PreparedSQL对象。
　　  
利用Java的/*- */注释来实现多行文本支持，是个黑科技，详细介始请见[这里](https://my.oschina.net/drinkjava2/blog/1611028)，优点是可以利用IDE快速定位到SQL文本。 缺点是需要将Java实体类源码放在Resources目录而不是src目录，并在Maven的<build><plugins>节点中添加如下配置:
```
     <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>build-helper-maven-plugin</artifactId>
        <version>1.12</version>
        <executions> 
          <execution>
            <id>add-source</id>
            <phase>generate-sources</phase>
            <goals>
              <goal>add-source</goal>
            </goals>
            <configuration>
              <sources>
                <source>src/main/resources</source>
              </sources>
            </configuration>
          </execution>  
          <execution>
              <id>add-test-source</id>
              <phase>generate-sources</phase>
              <goals>
                <goal>add-test-source</goal>
              </goals>
              <configuration>
                <sources>
                    <source>src/test/resources</source>
              </sources>
              </configuration>
            </execution> 
        </executions>
      </plugin> 
```
以上只是针对Maven+Eclipse开发环境，如果使用Gradle或其它开发环境的，请自行想办法配置，如果配置不出来，就用笨办法，在源码目录和Resources目录下各放一个实体类源程序。
　　  
　　  
SqlMapper风格之三: 抽象类实例化
```
public abstract class AbstractUser extends User{//这里User是个ActiverRecord子类

 public abstract PreparedSQL updateUserPreparedSQL(String name, String address);
	/*- 
	   update users 
	      set name=?, address=?
	*/

  public abstract List<AbstractUser> selectUsersByText(String name, String address);
	/*-
	   select u.** 
	   from 
	   users u
	      where 
	         u.name=? and address=?
	 */
}
//主程序中：
AbstractUser user = ActiveRecord.create(AbstractUser.class);
user.ctx().nUpdate(user.updateUserPreparedSQL("Tom", "China"));
List<AbstractUser> users = user.selectUsersByText("Tom", "China");
```
这更是个试验性质大于实用性的黑科技了，如果说多行文本支持还比较麻烦的话，这个甚至不能保证可以运行了，不光需要将Java抽象类的源码放在Resource目录下，而且还用到了动态编译技术，不一定能在所有的Java环境下都能运行(仅在Eclipse开发环境/Tomcat/WebLogic下测试通过)，用ActiveRecord.create(Class)方法或ActiveRecord.create(SqlBoxContext, Class)方法创建抽象类实例，后一种方法注入一个SqlBoxContext到生成的抽象类实例中(以支持多数据源)。
这种方式的优点是省略了方法体，少打了几个字，生成的实例是抽象类的子类，抽象方法可以随时转为实际方法，缺点是使用了有兼容问题的黑科技。   
　　  
　　  
SqlMapper风格之四: 接口类实例化(正在考虑中)
```
public interface UserMapper{ 
public List<User> selectUsersByText(String name, String address);
	/*-
	   select u.** 
	   from 
	   users u
	      where 
	         u.name=? and address=?
	 */
//主程序中：
UserMapper userMappper= ctx.create(UserMapper.class);
List<User> users= userMappper.selectUsersByText("Tom", "China");
```
这个目前还在考虑是否要添加，从技术角度来说没有问题，MyBatis早已实现，而且不需要用到什么黑科技，只需要Java动态代理。但是这种方式个人不是太喜欢，因为它打破了ActiverRecord和SqlMapper合体模式，虽然省略了方法体，少打了几个字，但是当遇到复杂的映射时，光用注解是解决不了问题的，不象MyBatis一样可以用XML配置来解决复杂问题，当映射复杂时，jSqlBox推荐在方法体里自已动手查询、装配对象(例如利用NoSql查询)，而接口是不能有方法体的，只有类和抽象类才允许方法体(先不谈Java8)。