#### SQL写法： nXxxx方法

nXxxx是个统称，指的是n小写字母开头的一系列SQL方法，如nQuery、nExecute、nInsert等等方法，这是从jDbPro模块开始新加入的方法，不用再捕捉SqlException异常。"n"表示"No"或"New"的意思。 运行期如果发生SqlException异常将转化为运行时异常(RuntimeException)抛出，通常程序员不必去捕捉这些运行时异常，而是通过配置一个支持声明式事务的AOP工具如Spring等来捕获运行时异常(见jBooox和jsqlbox-spring示例项目)，自动回滚事务。这种用法的优点是程序员不必进行繁琐的异常捕获处理，可以将精力集中于业务代码。使用nXxxx系列方法的前提是在构造SqlBoxContext时要提供一个DataSource参数。
```Java
ctx.nExecute("insert into users (name,address) values(?,?)", "Sam", "Canada");
ctx.nExecute("update users set name=?, address=?", "Tom", "China");
Assert.assertEquals(1L,
   ctx.nQueryForObject("select count(*) from users where name=? and address=?", "Tom", "China"));
ctx.nExecute("delete from users where name=? or address=?", "Tom", "China");
```
注意：当没有对SqlBoxContext进行事务配置时，每一个nXxxx方法如nExecute、nQuery等都默认工作在自动提交模式，也就是说，各个SQL方法是互相独立的，没有被事务保护，适用于只读或不重要的场合。当要求各个方法工作在同一个事务中时，必须在构造SqlBoxContext实例的同时进行它的事务配置，详见右侧的“事务配置"链接。(以后章节的iXxxx、tXxxx、xXxx系列方法以及ActiveRecord等，其工作方式都与此类似,就不再重复加这段备注了。)