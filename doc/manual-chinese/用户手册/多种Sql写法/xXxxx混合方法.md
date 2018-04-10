#### SQL写法： xXxxx()模板方法

xXxxx()系列方法，Inline风格和模板风格的混用，x开头表示"mix"的意思。这种用法不需要创建一个Map，而是直接利用ThreadLocal技术将模板参数暂存到线程局部变量里，可以少打几个字，代码更精简。缺点是同iXxxx系列方法一样，必须先调用put0()或put0("key",value)方法，将旧的线程局部变量清空，防止出现线程局部变量打架的问题。
```Java
User user = new User("Sam", "Canada");
User tom = new User("Tom", "China");
put0("user", user);
ctx.xExecute("insert into users (name, address) values(#{user.name},:user.address)");
put0("user", tom);
ctx.xExecute("update users set name=#{user.name}, address=:user.address");
Assert.assertEquals(1L,ctx.xQueryForObject("select count(*) from users where ${col}=#{name} and address=#{addr}",
   put0("name", "Tom"), put("addr", "China"), replace("col", "name")));
ctx.xExecute("delete from users where name=#{u.name} or address=#{u.address}", put0("u", tom));
```
上例中replace方法是对模板中的${变量名}进行直接替换，这是为了防止打字错误造成SQL注入，特别设计的方法。参数必须用put或put0方法注入，直接替换的字符串值必须用replace(或replace0)方法。