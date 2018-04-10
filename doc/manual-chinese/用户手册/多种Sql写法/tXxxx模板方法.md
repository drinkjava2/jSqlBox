#### SQL写法： tXxxx()模板方法

tXxxx()系列方法，利用模板来写SQL，参数放在Map里传入:
```Java
    Map<String, Object> params=new HashMap<String, Object>();
    params.put("user", new User("Sam", "Canada"));
    ctx.tExecute(params,"insert into users (name, address) values(#{user.name},#{user.address})");
    params.put("user", new User("Tom", "China")); 
    ctx.tExecute(params,"update users set name=#{user.name}, address=#{user.address}");
    params.clear();
    params.put("name", "Tom");
    params.put("addr", "China");
    Assert.assertEquals(1L,
        ctx.tQueryForObject(params,"select count(*) from users where name=#{name} and address=#{addr}")); 
    params.put("u", tom);
    ctx.tExecute(params, "delete from users where name=#{u.name} or address=#{u.address}");
```
jSqlBox自带的模板引擎比较简单，没有什么语法支持，缺省的模型引擎采用“#{变量名}”或“:变量名”作为占位符，并可以混用，运行时简单地将占位符所在的位置解析为问号。jSqlBox可以配置采用不同的模型引擎,详见右侧"模板配置"菜单链接。 在demo目录下有一个名为jsqlbox-beetlsql的演示项目，演示了jSqlBox利用BeetlSql来作为自已的模板引擎。  

示例中的模板SQL是直接写在方法体里，实际项目开发时也可以存在某个文本文件中，然用在方法体里用getSQL(sqlId)之类的静态方法调用，或是由模板引擎来决定查找方式。

用模板来存放SQL的优点是解决了问号和实参配对问题，可以集中统一管理SQL，尤其是非常长的多行SQL，并且某些模板引擎可以加入一些逻辑如空值判断等。缺点是模板为纯文本，不支持字段重构，IDE对模板文本的快速定位不够友好。

 