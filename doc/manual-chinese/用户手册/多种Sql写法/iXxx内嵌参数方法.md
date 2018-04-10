#### SQL写法： iXxxx()方法

SQL内嵌参数(Inline风格)，在SQL里直接写参数，参数暂存在Threadlocal中,SQL执行时自动转化为preparedStatement，这种方式的优点是被赋值的字段和实际参数可以写在同一行上,字段多时利于维护，也方便根据不确定的条件动态拼接SQL。这是始于jSqlBox的技术创新。很多场合业务逻辑不复杂，但是字段很多，SQL写得很长，当要添加、修改一个字段时，光是找到这个字段和它对应的是哪一个参数就很麻烦(用模板是一种方案，但模板占位符要多打几个字，模板本身的快速定位查找也是个问题。)，利用SQL内嵌参数这种写法，可以方便地增加、删除字段，因为每一个字段和它对应的实参都写在了同一行上。 
```Java
  ctx.iExecute("insert into users (", //
    " name,", param0("Sam"), //在类开头用import static SqlBoxContext.*来静态引入param0等方法
    " age, ", param(10), //
    " address ", param("Canada"), //
    ") ", valuesQuesions());
  param0("Tom", "China");
  ctx.iExecute("update users set name=?,address=?");
  Assert.assertEquals(1L, ctx
    .iQueryForObject("select count(*) from users where name=? and address=?" + param0("Tom", "China")));
  ctx.iExecute("delete from users where name=", question0("Tom"), " or address=", question("China"));
```
虽然jSqlBox在每个方法执行结束后自动清除所有线程局部变量，但为了以防万一，每次使用iXxxx系列方法时第一个参数必须调用param0或question0方法来清除之前的所有旧线程局部变量参数，另外也不允许方法嵌套调用，这是使用它时必须注意的一个坑，但是只要注意了上述两点，它带来的便利还是不少的，例如：

当需要根据复杂的条利来动态拼接SQL时，如果不想使用模板语言，利用Inline风格配合Java本身的语法，也是一种简单明了的解决方案:
```
ctx.iQueryForLongValue("Select count(*) from users where ", param0(),
                (condition == 1 || condition == 3) ? "userName=? and address=?" + param(u.getName(), u.getAddress())
                        : "1=1 ",
                (condition == 2) ? " and userName=" + param(u.getName()) : "",
                (condition == 3) ? " and age=" + param(u.getAge()) : "");
```

写出支持重构的SQL:
```
ctx.iExecute("insert into ", USER, " ( ", //USER、NAME是在User类中定义的常量，静态引入
	NAME, ",", param0("Sam"), //
	ADDRESS, " ", param("Canada"), //
	") ", valuesQuesions());
```
支持重构的优点不光是可以方便地变更字段名，更重要的是提高可维护性，例如一个类有30个字段，在程序中出现10个SQL调用，现在想在某个字段后添加一个新字段怎么办，只需要将某个字段注释掉，在IDE中将显示出这10个SQL中某字段的位置（出错了)，在10个SQL处分别添加完新字段后再将注释掉的字段回原即可。

对于逻辑非常复杂的大段SQL文本，建议用模板或Java多行文本来存放SQL(下面会讲到)，以提高SQL的可读性，方便DBA优化; 对于逻辑相对简单，但是插入、更新字段非常多的SQL，建议用Inline风格写法，并且每个字段和参数占同一行，以提高可维护性。对于没有逻辑，只是单纯插入、更新的场合，建议用ActiveRecord模式，以获取可重构性。